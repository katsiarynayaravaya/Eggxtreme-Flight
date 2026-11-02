package com.example.eggxtremeflight;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShadowPlaybackTest {

    @Test
    public void staysOnSampleUntilIntervalElapses() {
        ShadowPlayback playback = new ShadowPlayback(Arrays.asList(0.2, 0.4, 0.6), 100);

        playback.advance(0.05f);

        assertFalse(playback.isFinished());
        assertEquals(0.2, playback.getNormalizedY(), 0.0001);
    }

    @Test
    public void advancesOneSamplePerInterval() {
        ShadowPlayback playback = new ShadowPlayback(Arrays.asList(0.2, 0.4, 0.6), 100);

        playback.advance(0.1f);
        assertEquals(0.4, playback.getNormalizedY(), 0.0001);

        playback.advance(0.1f);
        assertEquals(0.6, playback.getNormalizedY(), 0.0001);
        assertFalse(playback.isFinished());
    }

    @Test
    public void finishesAfterTheLastSample() {
        ShadowPlayback playback = new ShadowPlayback(Arrays.asList(0.2, 0.4), 100);

        playback.advance(0.2f);

        assertTrue(playback.isFinished());
        assertEquals(0.4, playback.getNormalizedY(), 0.0001);
    }

    @Test
    public void emptyTrajectoryFinishesImmediately() {
        ShadowPlayback playback = new ShadowPlayback(Collections.emptyList(), 100);

        playback.advance(0.016f);

        assertTrue(playback.isFinished());
        assertEquals(0.5, playback.getNormalizedY(), 0.0001);
    }
}
