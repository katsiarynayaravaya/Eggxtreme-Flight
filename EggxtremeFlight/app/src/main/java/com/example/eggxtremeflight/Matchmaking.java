package com.example.eggxtremeflight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Matchmaking {
    private static final int BOT_SCORE = 3;
    private static final int BOT_SAMPLE_INTERVAL_MS = 100;
    private static final String BOT_SKIN_ID = "skin1";

    private Matchmaking() {
    }

    public static final class Opponent {
        public final List<Double> trajectory;
        public final int score;
        public final String skinId;
        public final int sampleIntervalMs;
        public final boolean bot;

        public Opponent(List<Double> trajectory, int score, String skinId,
                        int sampleIntervalMs, boolean bot) {
            List<Double> copy = trajectory == null ? new ArrayList<>() : new ArrayList<>(trajectory);
            this.trajectory = Collections.unmodifiableList(copy);
            this.score = score;
            this.skinId = skinId == null || skinId.isEmpty() ? BOT_SKIN_ID : skinId;
            this.sampleIntervalMs = sampleIntervalMs > 0 ? sampleIntervalMs : BOT_SAMPLE_INTERVAL_MS;
            this.bot = bot;
        }
    }

    public static Opponent defaultBot() {
        return new Opponent(buildBotTrajectory(), BOT_SCORE, BOT_SKIN_ID, BOT_SAMPLE_INTERVAL_MS, true);
    }

    public static Opponent pickClosest(List<Opponent> candidates, int targetScore) {
        Opponent best = null;
        int bestDiff = Integer.MAX_VALUE;
        if (candidates != null) {
            for (Opponent candidate : candidates) {
                if (candidate == null || candidate.bot || candidate.trajectory.isEmpty()) {
                    continue;
                }
                int diff = Math.abs(candidate.score - targetScore);
                boolean closer = best == null || diff < bestDiff;
                boolean sameDistanceButHarder = diff == bestDiff && best != null && candidate.score > best.score;
                if (closer || sameDistanceButHarder) {
                    best = candidate;
                    bestDiff = diff;
                }
            }
        }
        return best != null ? best : defaultBot();
    }

    public static Opponent fromFields(int score, List<?> rawTrajectory, String skinId, Integer sampleIntervalMs) {
        List<Double> trajectory = new ArrayList<>();
        if (rawTrajectory != null) {
            for (Object point : rawTrajectory) {
                if (point instanceof Number) {
                    trajectory.add(((Number) point).doubleValue());
                }
            }
        }
        int interval = sampleIntervalMs == null ? BOT_SAMPLE_INTERVAL_MS : sampleIntervalMs;
        return new Opponent(trajectory, score, skinId, interval, false);
    }

    private static List<Double> buildBotTrajectory() {
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            points.add(round3(0.42 + 0.15 * Math.sin(i * 0.55)));
        }
        double y = points.get(points.size() - 1);
        for (int step = 1; step <= 12; step++) {
            y = Math.min(0.98, y + 0.04 * step);
            points.add(round3(y));
        }
        return points;
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
