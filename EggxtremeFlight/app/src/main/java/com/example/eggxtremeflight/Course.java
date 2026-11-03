package com.example.eggxtremeflight;

/**
 * Scroll speed and pipe spacing for the current screen.
 * A fixed pixel speed packs fat pipes together on a tall narrow phone
 * and gives a short reaction time on a small width.
 */
public final class Course {
    public static final float BASE_SPEED = 300f;
    public static final float LEGACY_SPAWN_INTERVAL = 2.5f;
    private static final float REFERENCE_WIDTH = 1080f;
    private static final float MIN_INTERVAL = 2.2f;

    private Course() {
    }

    public static float scrollSpeed(int screenWidth) {
        if (screenWidth <= 0) return BASE_SPEED;
        return BASE_SPEED * screenWidth / REFERENCE_WIDTH;
    }

    /**
     * Seconds between pipes. The gap is at least a share of the screen
     * and several pipe widths, so a tall narrow layout does not glue columns together.
     */
    public static float spawnInterval(int screenWidth, int pipeWidth, float speed) {
        float pace = speed > 0f ? speed : BASE_SPEED;
        float width = Math.max(screenWidth, 1);
        float column = Math.max(pipeWidth, 1);
        float spacing = Math.max(width * 0.72f, column * 3.6f);
        return Math.max(MIN_INTERVAL, spacing / pace);
    }
}
