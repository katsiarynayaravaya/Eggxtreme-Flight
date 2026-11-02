package com.example.eggxtremeflight;

public class LeaderEntry {
    public final String uid;
    public final String nickname;
    public final long bestScore;

    public LeaderEntry(String uid, String nickname, long bestScore) {
        this.uid = uid;
        this.nickname = nickname;
        this.bestScore = bestScore;
    }
}