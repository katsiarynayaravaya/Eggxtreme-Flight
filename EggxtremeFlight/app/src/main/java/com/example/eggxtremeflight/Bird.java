package com.example.eggxtremeflight;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Bird {
    private Bitmap skinUp, skinMid, skinDown;
    private Bitmap currentSkinFrame;
    private float birdX, birdY;
    private float velocityY = 0f;
    private final float GRAVITY = 1.2f;
    private final float JUMP = -18f;
    private int animationTick = 0;
    private int currentFrameIndex = 0;
    private final int size;

    public Bird(Context context, int screenWidth, int screenHeight){
        this.size = screenHeight / 12;
        this.birdX = screenWidth / 4f;
        this.birdY = screenHeight / 2f;

        SharedPreferences sharedPreferences = context.getSharedPreferences("GameSharedPreferences", Context.MODE_PRIVATE);
        String activeSkin = sharedPreferences.getString("selected_skin", "skin1");

        int resUp, resMid, resDown;
        if (activeSkin.equals("skin2")) {
            resUp = R.drawable.skin2_egg_up;
            resMid = R.drawable.skin2_egg_mid;
            resDown = R.drawable.skin2_egg_down;
        } else if (activeSkin.equals("skin3")) {
            resUp = R.drawable.skin3_nagget_up;
            resMid = R.drawable.skin3_nagget_mid;
            resDown = R.drawable.skin3_nagget_down;
        } else {
            resUp = R.drawable.skin1_chicken_up;
            resMid = R.drawable.skin1_chicken_mid;
            resDown = R.drawable.skin1_chicken_down;
        }

        Bitmap rawUp = BitmapFactory.decodeResource(context.getResources(), resUp);
        Bitmap rawMid = BitmapFactory.decodeResource(context.getResources(), resMid);
        Bitmap rawDown = BitmapFactory.decodeResource(context.getResources(), resDown);

        skinUp = Bitmap.createScaledBitmap(rawUp, size, size, true);
        skinMid = Bitmap.createScaledBitmap(rawMid, size, size, true);
        skinDown = Bitmap.createScaledBitmap(rawDown, size, size, true);

        rawUp.recycle();
        rawMid.recycle();
        rawDown.recycle();
        currentSkinFrame = skinMid;
    }

    public void update(){
        animationTick++;
        if (animationTick >= 6) {
            animationTick = 0;
            currentFrameIndex = (currentFrameIndex + 1) % 4;
            switch (currentFrameIndex) {
                case 0: case 2:
                    currentSkinFrame = skinMid;
                    break;
                case 1:
                    currentSkinFrame = skinUp;
                    break;
                case 3:
                    currentSkinFrame = skinDown;
                    break;
            }
        }

        velocityY += GRAVITY;
        birdY += velocityY;
    }

    public void jump() {
        velocityY = JUMP;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(currentSkinFrame, birdX, birdY, null);
    }

    public boolean isOutOfBounds(int screenHeight) {
        return birdY <= 0 || birdY >= screenHeight - size;
    }

    public int getSize() {
        return size;
    }

}
