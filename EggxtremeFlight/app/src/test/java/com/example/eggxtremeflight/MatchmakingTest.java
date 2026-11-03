package com.example.eggxtremeflight;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchmakingTest {

    @Test
    public void emptyDatabaseUsesBotTrajectory() {
        Matchmaking.Opponent opponent = Matchmaking.pickRandom(Collections.emptyList(), "me", new Random(1));

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
    public void prefersSomeoneElseOverOwnRecord() {
        Matchmaking.Opponent ownRecord = player("me", 50, 0.4);
        Matchmaking.Opponent friend = player("friend", 3, 0.5);

        for (int seed = 0; seed < 20; seed++) {
            Matchmaking.Opponent chosen = Matchmaking.pickRandom(
                    Arrays.asList(ownRecord, friend), "me", new Random(seed));
            assertEquals("friend", chosen.uid);
        }
    }

    @Test
    public void usesOwnRunsOnlyWhenNobodyElsePlayed() {
        Matchmaking.Opponent low = player("me", 2, 0.4);
        Matchmaking.Opponent high = player("me", 9, 0.5);
        Matchmaking.Opponent chosen = Matchmaking.pickRandom(
                Arrays.asList(low, high), "me", new Random(1));

        assertFalse(chosen.bot);
        assertEquals("me", chosen.uid);
    }

    @Test
    public void skipsSessionWithoutTrajectory() {
        Matchmaking.Opponent empty = Matchmaking.fromFields(10, Collections.emptyList(), "skin2", 100);
        Matchmaking.Opponent playable = player("friend", 30, 0.2);

        Matchmaking.Opponent chosen = Matchmaking.pickRandom(
                Arrays.asList(empty, playable), "me", new Random(1));

        assertEquals(30, chosen.score);
        assertEquals("friend", chosen.uid);
    }

    @Test
    public void dropsZeroAndWeakSessionsAroundThePlayerAverage() {
        Matchmaking.SkillBand band = Matchmaking.skillBand(Arrays.asList(8, 10, 12), 12);
        Matchmaking.Opponent zero = player("stranger", 0, 0.4);
        Matchmaking.Opponent weak = player("stranger", 2, 0.4);
        Matchmaking.Opponent fair = player("leader", 15, 0.4);
        Matchmaking.Opponent tooStrong = player("champion", 40, 0.4);

        assertEquals(7, band.floor);
        assertEquals(18, band.ceiling);
        List<Matchmaking.Opponent> kept = Matchmaking.inRange(Arrays.asList(zero, weak, fair, tooStrong), band.floor, band.ceiling);

        assertEquals(1, kept.size());
        assertEquals(15, kept.get(0).score);
    }

    @Test
    public void beginnerDoesNotFaceAChampion() {
        Matchmaking.SkillBand band = Matchmaking.skillBand(Collections.emptyList(), 0);

        assertEquals(1, band.floor);
        assertTrue(band.ceiling < 10);
        assertTrue(Matchmaking.inRange(Collections.singletonList(player("champion", 30, 0.4)), band.floor, band.ceiling).isEmpty());
    }

    @Test
    public void buildsGapsAroundTheRecordedFlight() {
        List<Double> flat = Collections.nCopies(80, 0.5);
        List<Double> gaps = Matchmaking.gapsForTrajectory(flat, 100, 400, 200f, 2.5f);

        assertFalse(gaps.isEmpty());
        for (double top : gaps) {
            assertTrue(top >= 1.0 / 6.0 && top <= 2.0 / 3.0);
        }
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

    private static Matchmaking.Opponent player(String uid, int score, double y) {
        return Matchmaking.fromFields(score, Collections.singletonList(y), "skin2", 100,
                uid, uid, null, 400);
    }
}
