package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Random;

public class Pipe {
    private final Bitmap pipeBody, pipeTop, pipeBottom;
    private final int gap;
    private final int minHeight;
    private final int maxHeight;
    private final int topPipe;
    private final int bottomPipe;
    private float x;
    private final float speed;
    private boolean isPassed = false;
    private boolean isShadowPassed = false;

    public Pipe (Bitmap body, Bitmap top, Bitmap bottom, int gap, int screenHeight, int screenWidth, float speed){
        pipeBody = body;
        pipeTop = top;
        pipeBottom = bottom;

        this.gap = gap;
        minHeight = screenHeight / 6;
        maxHeight = screenHeight - gap - minHeight;
        x = screenWidth;
        this.speed = speed;

        Random random = new Random();
        topPipe = minHeight + random.nextInt(maxHeight - minHeight);
        bottomPipe = screenHeight - gap - topPipe;
    }

    public Pipe (Bitmap body, Bitmap top, Bitmap bottom, int gap, int screenHeight, int screenWidth, float speed, float topPipe, float bottomPipe){
        pipeBody = body;
        pipeTop = top;
        pipeBottom = bottom;

        this.gap = gap;
        minHeight = screenHeight / 6;
        maxHeight = screenHeight - gap - minHeight;
        x = screenWidth;
        this.speed = speed;
        this.topPipe = (int) topPipe;
        this.bottomPipe = (int) bottomPipe;
    }

    public void update(float deltaTime){
        x -= speed * deltaTime;
    }

    public void draw(Canvas canvas){
        float finalTopCapY = topPipe - pipeTop.getHeight();
        float y = finalTopCapY;
        while(y > -pipeBody.getHeight()){
            y -= pipeBody.getHeight();
            canvas.drawBitmap(pipeBody, x, y, null);

        }
        canvas.drawBitmap(pipeTop, x, finalTopCapY - 10f, null);

        float bottomLimit = canvas.getHeight() - bottomPipe;
        y = bottomLimit + pipeBottom.getHeight();
        while (y < canvas.getHeight()) {
            canvas.drawBitmap(pipeBody, x, y, null);
            y += pipeBody.getHeight();
        }
        canvas.drawBitmap(pipeBottom, x, bottomLimit + 10f, null);
    }

    public boolean isOutOfScreen(){
        return x + pipeBody.getWidth() < 0;
    }

    public float getX(){
        return x + pipeTop.getWidth();
    }

    public float getTopPipe() {
        return topPipe;
    }

    public float getBottomPipe() {
        return bottomPipe;
    }

    public boolean isPassed() {
        return isPassed;
    }
    public void setPassed(boolean passed) {
        this.isPassed = passed;
    }

    public boolean isShadowPassed() {
        return isShadowPassed;
    }
    public void setShadowPassed(boolean passed) {
        this.isShadowPassed = passed;
    }

    public boolean checkCollision(float birdX, float birdY, int birdSize) {
        RectF birdRect = new RectF(birdX, birdY, birdX + birdSize, birdY + birdSize);

        float pipeWidth = pipeTop.getWidth();
        float tolerance = 30f;

        float hitboxLeft = x + tolerance;
        float hitboxRight = x + pipeWidth - tolerance;
        float hitboxTopBottom = topPipe - 10f - tolerance;
        float hitboxBottomTop = topPipe + gap + 10f + tolerance;

        RectF topColumnRect = new RectF(hitboxLeft, 0, hitboxRight, hitboxTopBottom);
        RectF bottomColumnRect = new RectF(hitboxLeft, hitboxBottomTop, hitboxRight, 10000f);

        return RectF.intersects(birdRect, topColumnRect) || RectF.intersects(birdRect, bottomColumnRect);
    }
}
