package com.example.eggxtremeflight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class Matchmaking {
    private static final int BOT_SCORE = 3;
    private static final int BOT_SAMPLE_INTERVAL_MS = 100;
    private static final String BOT_SKIN_ID = "skin1";
    private static final float BIRD_X_FRACTION = 0.25f;
    private static final double BIRD_HEIGHT_FRACTION = 1.0 / 12.0;
    private static final double GAP_FRACTION = BIRD_HEIGHT_FRACTION * 2.0;
    private static final double MIN_TOP_FRACTION = 1.0 / 6.0;

    private Matchmaking() {
    }

    public static final class Opponent {
        public final List<Double> trajectory;
        public final List<Double> pipeTops;
        public final int score;
        public final String skinId;
        public final int sampleIntervalMs;
        public final int worldWidth;
        public final float scrollSpeed;
        public final float spawnInterval;
        public final String uid;
        public final String nickname;
        public final boolean bot;

        public Opponent(List<Double> trajectory, int score, String skinId,
                        int sampleIntervalMs, boolean bot) {
            this(trajectory, score, skinId, sampleIntervalMs, bot, null, null, null, 0);
        }

        public Opponent(List<Double> trajectory, int score, String skinId, int sampleIntervalMs,
                        boolean bot, String uid, String nickname, List<Double> pipeTops, int worldWidth) {
            this(trajectory, score, skinId, sampleIntervalMs, bot, uid, nickname, pipeTops, worldWidth, 0f, 0f);
        }

        public Opponent(List<Double> trajectory, int score, String skinId, int sampleIntervalMs,
                        boolean bot, String uid, String nickname, List<Double> pipeTops, int worldWidth,
                        float scrollSpeed, float spawnInterval) {
            List<Double> trajectoryCopy = trajectory == null ? new ArrayList<>() : new ArrayList<>(trajectory);
            List<Double> pipeCopy = pipeTops == null ? new ArrayList<>() : new ArrayList<>(pipeTops);
            this.trajectory = Collections.unmodifiableList(trajectoryCopy);
            this.pipeTops = Collections.unmodifiableList(pipeCopy);
            this.score = score;
            this.skinId = skinId == null || skinId.isEmpty() ? BOT_SKIN_ID : skinId;
            this.sampleIntervalMs = sampleIntervalMs > 0 ? sampleIntervalMs : BOT_SAMPLE_INTERVAL_MS;
            this.bot = bot;
            this.uid = uid;
            this.nickname = nickname;
            this.worldWidth = Math.max(worldWidth, 0);
            this.scrollSpeed = scrollSpeed > 0f ? scrollSpeed : 0f;
            this.spawnInterval = spawnInterval > 0f ? spawnInterval : 0f;
        }

        public Opponent withNickname(String nickname) {
            return new Opponent(trajectory, score, skinId, sampleIntervalMs, bot, uid, nickname,
                    pipeTops, worldWidth, scrollSpeed, spawnInterval);
        }
    }

    public static final class SkillBand {
        public final int floor;
        public final int ceiling;
        public final int average;

        public SkillBand(int floor, int ceiling, int average) {
            this.floor = floor;
            this.ceiling = ceiling;
            this.average = average;
        }
    }

    public static Opponent defaultBot() {
        return new Opponent(buildBotTrajectory(), BOT_SCORE, BOT_SKIN_ID, BOT_SAMPLE_INTERVAL_MS, true);
    }

    public static SkillBand skillBand(List<Integer> scores, int bestScore) {
        int sum = 0;
        int count = 0;
        int best = Math.max(0, bestScore);
        if (scores != null) {
            for (Integer score : scores) {
                if (score == null || score <= 0) continue;
                sum += score;
                count++;
                best = Math.max(best, score);
            }
        }
        if (count == 0 && best <= 0) {
            return new SkillBand(1, 4, 0);
        }
        int average = count == 0 ? best : Math.round(sum / (float) count);
        int reference = Math.max(average, 1);
        int floor = Math.max(1, Math.round(reference * 0.65f));
        int reachable = Math.max(best, average);
        int ceiling = Math.max(floor, reachable + Math.max(2, reachable / 2));
        return new SkillBand(floor, ceiling, average);
    }

    public static List<Opponent> inRange(List<Opponent> candidates, int floor, int ceiling) {
        List<Opponent> kept = new ArrayList<>();
        if (candidates == null) return kept;
        for (Opponent candidate : candidates) {
            if (candidate == null || candidate.bot || candidate.trajectory.isEmpty()) continue;
            if (candidate.score < floor || candidate.score > ceiling) continue;
            kept.add(candidate);
        }
        return kept;
    }

    public static Opponent pickRandom(List<Opponent> candidates, String ownUid, Random random) {
        return pickRandom(candidates, ownUid, random, null);
    }

    public static Opponent pickRandom(List<Opponent> candidates, String ownUid, Random random, Opponent avoid) {
        List<Opponent> others = new ArrayList<>();
        List<Opponent> own = new ArrayList<>();
        if (candidates != null) {
            for (Opponent candidate : candidates) {
                if (candidate == null || candidate.bot || candidate.trajectory.isEmpty()) {
                    continue;
                }
                if (ownUid != null && ownUid.equals(candidate.uid)) {
                    own.add(candidate);
                } else {
                    others.add(candidate);
                }
            }
        }
        List<Opponent> pool = others.isEmpty() ? own : others;
        if (pool.isEmpty()) {
            return defaultBot();
        }
        List<Opponent> fresh = new ArrayList<>();
        for (Opponent candidate : pool) {
            if (!sameRun(candidate, avoid)) {
                fresh.add(candidate);
            }
        }
        List<Opponent> choice = fresh.isEmpty() ? pool : fresh;
        return choice.get(random.nextInt(choice.size()));
    }

    public static boolean sameRun(Opponent first, Opponent second) {
        if (first == null || second == null) return false;
        if (first.bot || second.bot) return first.bot && second.bot;
        return first.score == second.score
                && first.trajectory.size() == second.trajectory.size()
                && first.pipeTops.size() == second.pipeTops.size()
                && Objects.equals(first.uid, second.uid);
    }

    public static Opponent fromFields(int score, List<?> rawTrajectory, String skinId, Integer sampleIntervalMs) {
        return fromFields(score, rawTrajectory, skinId, sampleIntervalMs, null, null, null, null);
    }

    public static Opponent fromFields(int score, List<?> rawTrajectory, String skinId, Integer sampleIntervalMs,
                                      String uid, String nickname, List<?> rawPipeTops, Integer worldWidth) {
        return fromFields(score, rawTrajectory, skinId, sampleIntervalMs, uid, nickname, rawPipeTops, worldWidth, 0f, 0f);
    }

    public static Opponent fromFields(int score, List<?> rawTrajectory, String skinId, Integer sampleIntervalMs,
                                      String uid, String nickname, List<?> rawPipeTops, Integer worldWidth,
                                      float scrollSpeed, float spawnInterval) {
        int interval = sampleIntervalMs == null ? BOT_SAMPLE_INTERVAL_MS : sampleIntervalMs;
        int width = worldWidth == null ? 0 : worldWidth;
        return new Opponent(readNumbers(rawTrajectory), score, skinId, interval, false,
                uid, nickname, readNumbers(rawPipeTops), width, scrollSpeed, spawnInterval);
    }

    public static List<Double> gapsForTrajectory(List<Double> trajectory, int sampleIntervalMs,
                                                 int screenWidth, float speed, float spawnIntervalSeconds) {
        List<Double> gaps = new ArrayList<>();
        if (trajectory == null || trajectory.isEmpty() || sampleIntervalMs <= 0
                || screenWidth <= 0 || speed <= 0f || spawnIntervalSeconds <= 0f) {
            return gaps;
        }
        double travelSeconds = (screenWidth * (1.0 - BIRD_X_FRACTION)) / speed;
        for (int index = 0; index < 500; index++) {
            double arrival = (index + 1) * spawnIntervalSeconds + travelSeconds;
            int sample = (int) (arrival * 1000.0 / sampleIntervalMs);
            if (sample < 0 || sample >= trajectory.size()) {
                break;
            }
            double top = trajectory.get(sample) + BIRD_HEIGHT_FRACTION / 2.0 - GAP_FRACTION / 2.0;
            double maxTop = 1.0 - GAP_FRACTION - MIN_TOP_FRACTION;
            if (top < MIN_TOP_FRACTION) top = MIN_TOP_FRACTION;
            if (top > maxTop) top = maxTop;
            gaps.add(round3(top));
        }
        return gaps;
    }

    private static List<Double> readNumbers(List<?> raw) {
        List<Double> values = new ArrayList<>();
        if (raw == null) return values;
        for (Object point : raw) {
            if (point instanceof Number) {
                values.add(((Number) point).doubleValue());
            }
        }
        return values;
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
