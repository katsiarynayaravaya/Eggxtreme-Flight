package com.example.eggxtremeflight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ShadowBird {
    private static final int SHADOW_ALPHA = 128;
    private static final float FALL_GRAVITY = 1400f;

    private final Bitmap skin;
    private final int size;
    private final int screenHeight;
    private final ShadowPlayback playback;
    private final Paint paint;
    private float birdX;
    private float birdY;
    private float fallVelocity;
    private boolean falling;
    private boolean hidden;

    public ShadowBird(Context context, int screenWidth, int screenHeight, Matchmaking.Opponent opponent) {
        this.screenHeight = screenHeight;
        this.size = Math.max(1, screenHeight / 12);
        this.playback = new ShadowPlayback(opponent.trajectory, opponent.sampleIntervalMs);
        this.birdX = Math.max(0f, screenWidth / 4f - size * 0.75f);
        this.birdY = screenHeight / 2f;

        SkinItem skinItem = SkinCatalog.findById(opponent.skinId);
        Bitmap raw = BitmapFactory.decodeResource(context.getResources(), skinItem.midRes);
        Bitmap scaled = Bitmap.createScaledBitmap(raw, size, size, true);
        if (scaled != raw) {
            raw.recycle();
        }
        skin = scaled;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAlpha(SHADOW_ALPHA);
    }

    public void start() {
        hidden = false;
        falling = false;
        fallVelocity = 0f;
        birdY = (float) (playback.getNormalizedY() * screenHeight);
    }

    public void update(float deltaTime) {
        if (hidden) return;
        if (!falling) {
            playback.advance(deltaTime);
            birdY = (float) (playback.getNormalizedY() * screenHeight);
            if (playback.isFinished()) {
                falling = true;
            }
            return;
        }
        fallVelocity += FALL_GRAVITY * deltaTime;
        birdY += fallVelocity * deltaTime;
        if (birdY > screenHeight) {
            hidden = true;
        }
    }

    public void draw(Canvas canvas) {
        if (hidden || skin == null) return;
        canvas.drawBitmap(skin, birdX, birdY, paint);
    }
}
