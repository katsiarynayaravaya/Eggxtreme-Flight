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

    public Pipe (Bitmap body, Bitmap top, Bitmap bottom, int gap, int screenHeight, int screenWidth, float speed){
        this(body, top, bottom, gap, screenHeight, screenWidth, speed, new Random());
    }

    public Pipe (Bitmap body, Bitmap top, Bitmap bottom, int gap, int screenHeight, int screenWidth, float speed, Random random){
        pipeBody = body;
        pipeTop = top;
        pipeBottom = bottom;

        this.gap = gap;
        minHeight = screenHeight / 6;
        maxHeight = screenHeight - gap - minHeight;
        x = screenWidth;
        this.speed = speed;

        int span = Math.max(1, maxHeight - minHeight);
        topPipe = minHeight + random.nextInt(span);
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
        float slop = capSlop();
        float finalTopCapY = topPipe - pipeTop.getHeight();
        float y = finalTopCapY;
        while(y > -pipeBody.getHeight()){
            y -= pipeBody.getHeight();
            canvas.drawBitmap(pipeBody, x, y, null);

        }
        canvas.drawBitmap(pipeTop, x, finalTopCapY - slop, null);

        float bottomLimit = canvas.getHeight() - bottomPipe;
        y = bottomLimit + pipeBottom.getHeight();
        while (y < canvas.getHeight()) {
            canvas.drawBitmap(pipeBody, x, y, null);
            y += pipeBody.getHeight();
        }
        canvas.drawBitmap(pipeBottom, x, bottomLimit + slop, null);
    }

    public boolean isOutOfScreen(){
        return x + pipeBody.getWidth() < 0;
    }

    public float getX(){
        return x + pipeTop.getWidth();
    }

    public int getTopPipe() {
        return topPipe;
    }

    public boolean isPassed() {
        return isPassed;
    }
    public void setPassed(boolean passed) {
        this.isPassed = passed;
    }

    public boolean checkCollision(float birdX, float birdY, int birdSize) {
        RectF birdRect = new RectF(birdX, birdY, birdX + birdSize, birdY + birdSize);

        float pipeWidth = pipeTop.getWidth();
        float tolerance = Math.max(6f, pipeWidth * 0.1f);
        float slop = capSlop();

        float hitboxLeft = x + tolerance;
        float hitboxRight = x + pipeWidth - tolerance;
        float hitboxTopBottom = topPipe - slop - tolerance;
        float hitboxBottomTop = topPipe + gap + slop + tolerance;

        RectF topColumnRect = new RectF(hitboxLeft, 0, hitboxRight, hitboxTopBottom);
        RectF bottomColumnRect = new RectF(hitboxLeft, hitboxBottomTop, hitboxRight, 10000f);

        return RectF.intersects(birdRect, topColumnRect) || RectF.intersects(birdRect, bottomColumnRect);
    }

    private float capSlop() {
        int cap = pipeTop == null ? 0 : pipeTop.getHeight();
        return Math.max(4f, cap * 0.2f);
    }
}
