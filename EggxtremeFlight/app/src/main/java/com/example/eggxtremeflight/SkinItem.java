package com.example.eggxtremeflight;

public class SkinItem {
    public final String id;
    public final int nameRes;
    public final int upRes;
    public final int midRes;
    public final int downRes;

    public SkinItem(String id, int nameRes, int upRes, int midRes, int downRes) {
        this.id = id;
        this.nameRes = nameRes;
        this.upRes = upRes;
        this.midRes = midRes;
        this.downRes = downRes;
    }
}