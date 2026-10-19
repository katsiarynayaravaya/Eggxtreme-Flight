package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class GameSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Bitmap pipeBody, pipeTop, pipeBottom;
    private final ArrayList<Pipe> pipes = new ArrayList<>();
    private float pipeSpawnTimer = 0f;
    private final float pipeSpawnInterval = 2.5f;
    private Bird bird;
    private Background background;
    private final float speed = 200f;
    private int score = 0;
    private Paint scorePaint;
    private final SurfaceHolder holder;
    private Thread thread;
    private volatile boolean isRunning = false;

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
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

            pipeSpawnTimer += deltaTime;
            if (pipeSpawnTimer >= pipeSpawnInterval) {
                pipeSpawnTimer = 0f;
                int gapSize = bird.getSize() * 2;
                pipes.add(new Pipe(pipeBody, pipeTop, pipeBottom, gapSize, getHeight(), getWidth(), speed));
            }
            for (int i=pipes.size()- 1; i>=0; i--){
                Pipe pipe = pipes.get(i);
                pipe.update(deltaTime);

                if(pipe.checkCollision(bird.getX(), bird.getY(), bird.getSize())){
                    isRunning = false;
                }

                if(!pipe.isPassed() && bird.getX() > pipe.getX()){
                    pipe.setPassed(true);
                    score++;
                }

                if(pipe.isOutOfScreen()){
                    pipes.remove(i);
                }
            }

            if (bird.isOutOfBounds(getHeight())) {
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
                    bird.draw(canvas);
                    canvas.drawText(String.valueOf(score), getWidth() / 2f, bird.getSize() * 1.5f, scorePaint);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            Thread.yield();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            bird.jump();
        }
        return true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        background = new Background(getContext(), getHeight(), speed);
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

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false;
        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
