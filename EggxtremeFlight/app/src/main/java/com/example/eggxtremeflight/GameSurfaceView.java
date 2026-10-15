package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class GameSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Bitmap gameMap;
    private final SurfaceHolder holder;
    private float backgroundX1 = 0f;
    private float backgroundX2 = 0f;
    private int mapWidth;
    private final float SPEED = 5f;
    private Thread thread;
    private volatile boolean isRunning = false;

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void run() {
        while (isRunning) {

            backgroundX1 -= SPEED;
            backgroundX2 = backgroundX1 + mapWidth;

            if (backgroundX1 <= -mapWidth) {
                backgroundX1 += mapWidth;
                backgroundX2 = backgroundX1 + mapWidth;
            }

            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawARGB(255, 0, 0, 0);
                    canvas.drawBitmap(gameMap, backgroundX1, 0f, null);
                    canvas.drawBitmap(gameMap, backgroundX2, 0f, null);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameMap == null) {
            Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.game_background_for_loop);

            int scaledHeight = getHeight();
            int scaledWidth = Math.round(rawBitmap.getWidth() * ((float) getHeight() / rawBitmap.getHeight()));

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, scaledWidth, scaledHeight, true);

            int cropOffset = 6;
            gameMap = Bitmap.createBitmap(scaledBitmap, cropOffset, 0,
                    scaledBitmap.getWidth() - (cropOffset * 2), scaledBitmap.getHeight());

            scaledBitmap.recycle();
            mapWidth = gameMap.getWidth();
            backgroundX2 = mapWidth;
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
