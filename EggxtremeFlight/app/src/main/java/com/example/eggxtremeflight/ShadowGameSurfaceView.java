package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Duel field for Shadowboxing. Single play keeps using {@link GameSurfaceView}.
 */
public class ShadowGameSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    public interface OnGameOverListener {
        void onGameOver(int score, String skinId, List<Double> trajectory, int sampleIntervalMs,
                        List<Double> pipeTops, int worldWidth);
    }

    public static final int SAMPLE_INTERVAL_MS = 100;
    private static final float SAMPLE_INTERVAL_S = SAMPLE_INTERVAL_MS / 1000f;
    private static final int MAX_SAMPLES = 6000;

    private Bitmap pipeBody, pipeTop, pipeBottom;
    private final ArrayList<Pipe> pipes = new ArrayList<>();
    private float pipeSpawnTimer = 0f;
    private final float pipeSpawnInterval = 2.5f;
    private Bird bird;
    private Background background;
    private static final float BASE_SPEED = 200f;
    private float playSpeed = BASE_SPEED;
    private int score = 0;
    private Paint scorePaint;
    private Paint shadowHudPaint;
    private String shadowHudName = "";
    private final SurfaceHolder holder;
    private Thread thread;
    private volatile boolean isRunning = false;
    private boolean surfaceReady = false;

    private final ArrayList<Double> trajectory = new ArrayList<>();
    private final ArrayList<Double> pipeTops = new ArrayList<>();
    private List<Double> replayPipeTops = Collections.emptyList();
    private int replayPipeIndex = 0;
    private Random continuationRandom = new Random();
    private int worldWidth = 0;
    private float sampleTimer = 0f;
    private volatile boolean gameOver = false;
    private OnGameOverListener gameOverListener;
    private Matchmaking.Opponent opponent;
    private ShadowBird shadowBird;

    public ShadowGameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setOpponent(Matchmaking.Opponent opponent) {
        if (this.opponent != null) return;
        this.opponent = opponent == null ? Matchmaking.defaultBot() : opponent;
        if (surfaceReady) {
            startLoop();
        }
    }

    public int getOpponentScore() {
        return opponent == null ? -1 : opponent.score;
    }

    public String getOpponentName() {
        if (opponent == null || opponent.bot || opponent.nickname == null || opponent.nickname.isEmpty()) {
            return getContext().getString(R.string.shadow_bot_name);
        }
        return opponent.nickname;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (isRunning) {
            long now = System.nanoTime();
            float deltaTime = (now - lastTime) / 1000000000f;
            lastTime = now;
            if (deltaTime > 0.1f) deltaTime = 0.1f;

            background.update(deltaTime);
            bird.update(deltaTime);
            if (shadowBird != null) {
                shadowBird.update(deltaTime);
            }

            sampleTimer += deltaTime;
            if (sampleTimer >= SAMPLE_INTERVAL_S) {
                sampleTimer -= SAMPLE_INTERVAL_S;
                recordSample();
            }

            pipeSpawnTimer += deltaTime;
            if (pipeSpawnTimer >= pipeSpawnInterval) {
                pipeSpawnTimer = 0f;
                spawnPipe();
            }
            for (int i = pipes.size() - 1; i >= 0; i--) {
                Pipe pipe = pipes.get(i);
                pipe.update(deltaTime);

                if (pipe.checkCollision(bird.getX(), bird.getY(), bird.getSize())) {
                    gameOver = true;
                    isRunning = false;
                    break;
                }

                if (!pipe.isPassed() && bird.getX() > pipe.getX()) {
                    pipe.setPassed(true);
                    score++;
                }

                if (pipe.isOutOfScreen()) {
                    pipes.remove(i);
                }
            }

            if (bird.isOutOfBounds(getHeight())) {
                gameOver = true;
                isRunning = false;
            }

            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawARGB(255, 0, 0, 0);
                    background.draw(canvas);
                    for (Pipe pipe : pipes) {
                        pipe.draw(canvas);
                    }
                    if (shadowBird != null) {
                        shadowBird.draw(canvas);
                    }
                    bird.draw(canvas);
                    canvas.drawText(String.valueOf(score), getWidth() / 2f, bird.getSize() * 1.5f, scorePaint);
                    if (opponent != null && shadowHudPaint != null) {
                        float hudX = bird.getSize() * 0.35f;
                        float hudY = bird.getSize() * 0.9f;
                        canvas.drawText(shadowHudName, hudX, hudY, shadowHudPaint);
                        canvas.drawText(String.valueOf(opponent.score), hudX, hudY + shadowHudPaint.getTextSize() * 1.15f, shadowHudPaint);
                    }
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            Thread.yield();
        }

        if (gameOver) {
            recordSample();
            reportGameOver();
        }
    }

    private void recordSample() {
        int height = getHeight();
        if (bird == null || height == 0 || trajectory.size() >= MAX_SAMPLES) return;
        float normalized = Math.max(0f, Math.min(1f, bird.getY() / height));
        trajectory.add(Math.round(normalized * 1000) / 1000.0);
    }

    private void reportGameOver() {
        final OnGameOverListener listener = gameOverListener;
        if (listener == null) return;
        final int finalScore = score;
        final String skinId = bird.getSkinId();
        final List<Double> trajectoryCopy = new ArrayList<>(trajectory);
        final List<Double> pipeCopy = new ArrayList<>(pipeTops);
        final int width = worldWidth;
        new Handler(Looper.getMainLooper()).post(() ->
                listener.onGameOver(finalScore, skinId, trajectoryCopy, SAMPLE_INTERVAL_MS, pipeCopy, width));
    }

    private void spawnPipe() {
        int height = getHeight();
        int gapSize = bird.getSize() * 2;
        Pipe pipe;
        if (replayPipeIndex < replayPipeTops.size()) {
            float top = (float) (replayPipeTops.get(replayPipeIndex) * height);
            float minTop = height / 6f;
            float maxTop = height - gapSize - minTop;
            if (maxTop < minTop) maxTop = minTop;
            top = Math.max(minTop, Math.min(maxTop, top));
            float bottom = height - gapSize - top;
            pipe = new Pipe(pipeBody, pipeTop, pipeBottom, gapSize, height, getWidth(), playSpeed, top, bottom);
            replayPipeIndex++;
        } else {
            pipe = new Pipe(pipeBody, pipeTop, pipeBottom, gapSize, height, getWidth(), playSpeed, continuationRandom);
        }
        pipes.add(pipe);
        if (height > 0) {
            pipeTops.add(Math.round(pipe.getTopPipe() * 1000.0 / height) / 1000.0);
        }
    }

    private void resetGame() {
        pipes.clear();
        pipeSpawnTimer = 0f;
        score = 0;
        gameOver = false;
        sampleTimer = 0f;
        trajectory.clear();
        pipeTops.clear();
        worldWidth = getWidth();
        recordSample();
    }

    private void prepareCourse() {
        playSpeed = BASE_SPEED;
        int width = getWidth();
        if (opponent != null && opponent.worldWidth > 0 && width > 0) {
            playSpeed = BASE_SPEED * width / (float) opponent.worldWidth;
        }
        if (opponent != null && !opponent.pipeTops.isEmpty()) {
            replayPipeTops = opponent.pipeTops;
        } else if (opponent != null) {
            replayPipeTops = Matchmaking.gapsForTrajectory(
                    opponent.trajectory,
                    opponent.sampleIntervalMs,
                    opponent.worldWidth > 0 ? opponent.worldWidth : width,
                    BASE_SPEED,
                    pipeSpawnInterval);
        } else {
            replayPipeTops = Collections.emptyList();
        }
        replayPipeIndex = 0;
        continuationRandom = new Random(courseSeed(opponent));
    }

    private static long courseSeed(Matchmaking.Opponent opponent) {
        if (opponent == null) return 1L;
        long seed = 17L;
        seed = seed * 31 + opponent.score;
        seed = seed * 31 + opponent.trajectory.size();
        seed = seed * 31 + opponent.pipeTops.size();
        if (opponent.uid != null) seed = seed * 31 + opponent.uid.hashCode();
        if (opponent.nickname != null) seed = seed * 31 + opponent.nickname.hashCode();
        return seed;
    }

    private void startLoop() {
        if (thread != null && thread.isAlive()) return;
        prepareCourse();
        if (opponent != null) {
            shadowHudName = getOpponentName();
            shadowBird = new ShadowBird(getContext(), getWidth(), getHeight(), opponent, shadowHudName);
            shadowBird.start();
        }
        resetGame();
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    private void initWorld() {
        background = new Background(getContext(), getHeight(), BASE_SPEED);
        bird = new Bird(getContext(), getWidth(), getHeight());

        if (scorePaint == null) {
            scorePaint = new Paint();
            scorePaint.setColor(android.graphics.Color.parseColor("#0f0002"));
            scorePaint.setTextSize(bird.getSize() * 0.8f);
            android.graphics.Typeface gameTypeface = androidx.core.content.res.ResourcesCompat.getFont(getContext(), R.font.game_font);
            scorePaint.setTypeface(gameTypeface);
            scorePaint.setAntiAlias(true);
            scorePaint.setFakeBoldText(true);
            scorePaint.setTextAlign(Paint.Align.CENTER);

            shadowHudPaint = new Paint(scorePaint);
            shadowHudPaint.setTextAlign(Paint.Align.LEFT);
            shadowHudPaint.setTextSize(bird.getSize() * 0.42f);
            shadowHudPaint.setColor(android.graphics.Color.WHITE);
            shadowHudPaint.setAlpha(190);
            shadowHudPaint.setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK);
        }

        if (pipeBody == null) {
            int pipeWidth = (int) (bird.getSize() * 1.3f);

            Bitmap rawBody = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_body);
            Bitmap rawTop = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_cap_top);
            Bitmap rawBottom = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_cap_bottom);

            pipeBody = Bitmap.createScaledBitmap(rawBody, pipeWidth, rawBody.getHeight(), true);
            pipeTop = Bitmap.createScaledBitmap(rawTop, pipeWidth, bird.getSize() / 2, true);
            pipeBottom = Bitmap.createScaledBitmap(rawBottom, pipeWidth, bird.getSize() / 2, true);
            rawBody.recycle();
            rawTop.recycle();
            rawBottom.recycle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && bird != null) {
            bird.jump();
        }
        return true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        initWorld();
        surfaceReady = true;
        if (opponent != null) {
            startLoop();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false;
        surfaceReady = false;
        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}