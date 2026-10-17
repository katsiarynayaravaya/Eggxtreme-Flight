package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

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

    public void update(){
        x -= speed;
    }

    public void draw(Canvas canvas){
        float y = 0f;
        while(y < topPipe - pipeTop.getHeight()){
            canvas.drawBitmap(pipeBody, x, y, null);
            y += pipeBody.getHeight();
        }
        canvas.drawBitmap(pipeTop, x, y - 10f, null);
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

}
