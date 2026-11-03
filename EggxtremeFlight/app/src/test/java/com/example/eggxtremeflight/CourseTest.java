package com.example.eggxtremeflight;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CourseTest {

    @Test
    public void reactionTimeDoesNotShrinkOnANarrowScreen() {
        float wide = 1080f / Course.scrollSpeed(1080);
        float narrow = 480f / Course.scrollSpeed(480);
        assertEquals(wide, narrow, 0.01f);
    }

    @Test
    public void widePipesOnANarrowScreenAreSpreadOut() {
        float speed = Course.scrollSpeed(720);
        int pipeWidth = 180;
        float interval = Course.spawnInterval(720, pipeWidth, speed);

        assertTrue(interval > Course.LEGACY_SPAWN_INTERVAL);
        assertTrue(interval * speed >= pipeWidth * 3.6f - 0.1f);
    }
}
