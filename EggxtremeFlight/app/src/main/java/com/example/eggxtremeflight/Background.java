package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Background {
    private Bitmap gameMap;
    private float backgroundX1 = 0f;
    private float backgroundX2 = 0f;
    private int mapWidth;
    private final float speed;

    public Background(Context context, int screenHeight, float speed) {
        this.speed = speed;
        Bitmap rawBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_background_for_loop);
        int scaledWidth = Math.round(rawBitmap.getWidth() * ((float) screenHeight / rawBitmap.getHeight()));

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, scaledWidth, screenHeight, true);
        int cropOffset = 6;
        gameMap = Bitmap.createBitmap(scaledBitmap, cropOffset, 0,scaledBitmap.getWidth() - (cropOffset * 2), scaledBitmap.getHeight());

        scaledBitmap.recycle();
        mapWidth = gameMap.getWidth();
        backgroundX2 = mapWidth;
    }

    public void update(float deltaTime){
        backgroundX1 -= speed * deltaTime;
        backgroundX2 -= speed * deltaTime;

        if (backgroundX1 <= -mapWidth) backgroundX1 = backgroundX2 + mapWidth;
        if (backgroundX2 <= -mapWidth) backgroundX2 = backgroundX1 + mapWidth;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(gameMap, (int) backgroundX1, 0f, null);
        canvas.drawBitmap(gameMap, (int) backgroundX2, 0f, null);
    }
}
