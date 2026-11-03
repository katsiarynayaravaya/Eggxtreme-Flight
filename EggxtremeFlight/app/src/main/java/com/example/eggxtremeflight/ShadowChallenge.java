package com.example.eggxtremeflight;

public final class ShadowChallenge {
    private static Matchmaking.Opponent current;

    private ShadowChallenge() {
    }

    public static void remember(Matchmaking.Opponent opponent) {
        current = opponent;
    }

    public static Matchmaking.Opponent current() {
        return current;
    }
}
