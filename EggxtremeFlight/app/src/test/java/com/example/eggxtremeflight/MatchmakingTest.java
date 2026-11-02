package com.example.eggxtremeflight;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchmakingTest {

    @Test
    public void emptyDatabaseUsesBotTrajectory() {
        Matchmaking.Opponent opponent = Matchmaking.pickClosest(Collections.emptyList(), 10);

        assertTrue(opponent.bot);
        assertEquals("skin1", opponent.skinId);
        assertEquals(100, opponent.sampleIntervalMs);
        assertFalse(opponent.trajectory.isEmpty());
        double first = opponent.trajectory.get(0);
        double last = opponent.trajectory.get(opponent.trajectory.size() - 1);
        assertTrue(last > first);
        for (double y : opponent.trajectory) {
            assertTrue(y >= 0.0 && y <= 1.0);
        }
    }

    @Test
    public void picksScoreClosestToBest() {
        Matchmaking.Opponent chosen = Matchmaking.pickClosest(Arrays.asList(
                session(2, 0.4),
                session(40, 0.3),
                session(9, 0.5)
        ), 10);

        assertFalse(chosen.bot);
        assertEquals(9, chosen.score);
        assertEquals("skin2", chosen.skinId);
    }

    @Test
    public void equalDistancePrefersHigherScore() {
        Matchmaking.Opponent chosen = Matchmaking.pickClosest(Arrays.asList(
                session(8, 0.4),
                session(12, 0.4)
        ), 10);

        assertEquals(12, chosen.score);
    }

    @Test
    public void skipsSessionWithoutTrajectory() {
        Matchmaking.Opponent empty = Matchmaking.fromFields(10, Collections.emptyList(), "skin2", 100);
        Matchmaking.Opponent playable = session(30, 0.2);

        Matchmaking.Opponent chosen = Matchmaking.pickClosest(Arrays.asList(empty, playable), 10);

        assertEquals(30, chosen.score);
    }

    @Test
    public void readsNumbersFromFirestoreShape() {
        List<Object> raw = Arrays.asList(0.25d, 1, 2L);
        Matchmaking.Opponent opponent = Matchmaking.fromFields(4, raw, null, null);

        assertEquals(3, opponent.trajectory.size());
        assertEquals(0.25, opponent.trajectory.get(0), 0.0001);
        assertEquals(1.0, opponent.trajectory.get(1), 0.0001);
        assertEquals(2.0, opponent.trajectory.get(2), 0.0001);
        assertEquals("skin1", opponent.skinId);
        assertEquals(100, opponent.sampleIntervalMs);
        assertFalse(opponent.bot);
    }

    private static Matchmaking.Opponent session(int score, double y) {
        return Matchmaking.fromFields(score, Collections.singletonList(y), "skin2", 100);
    }
}
