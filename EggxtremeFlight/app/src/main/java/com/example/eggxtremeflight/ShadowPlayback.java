package com.example.eggxtremeflight;

import java.util.ArrayList;
import java.util.List;

public final class ShadowPlayback {
    private final List<Double> samples;
    private final int sampleIntervalMs;
    private float elapsedMs;
    private boolean finished;

    public ShadowPlayback(List<Double> samples, int sampleIntervalMs) {
        this.samples = samples == null ? new ArrayList<>() : new ArrayList<>(samples);
        this.sampleIntervalMs = sampleIntervalMs > 0 ? sampleIntervalMs : 100;
    }

    public void advance(float deltaSeconds) {
        if (finished) return;
        if (samples.isEmpty()) {
            finished = true;
            return;
        }
        if (deltaSeconds > 0f) {
            elapsedMs += deltaSeconds * 1000f;
        }
        if ((int) (elapsedMs / sampleIntervalMs) >= samples.size()) {
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public double getNormalizedY() {
        if (samples.isEmpty()) return 0.5;
        if (finished) return samples.get(samples.size() - 1);
        float exact = elapsedMs / sampleIntervalMs;
        int index = (int) exact;
        if (index < 0) index = 0;
        if (index >= samples.size() - 1) return samples.get(samples.size() - 1);
        float fraction = exact - index;
        double from = samples.get(index);
        double to = samples.get(index + 1);
        return from + (to - from) * fraction;
    }
}
