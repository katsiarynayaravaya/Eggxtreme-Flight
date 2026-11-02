package com.example.eggxtremeflight;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {
    public static final int TOP_LIMIT = 10;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadTopPlayers(UserRepository.Callback<List<LeaderEntry>> callback) {
        db.collection("users")
                .whereGreaterThan("bestScore", 0)
                .orderBy("bestScore", Query.Direction.DESCENDING)
                .limit(TOP_LIMIT)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<LeaderEntry> result = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nickname = doc.getString("nickname");
                        Long bestScore = doc.getLong("bestScore");
                        if (nickname == null || bestScore == null) continue;
                        result.add(new LeaderEntry(doc.getId(), nickname, bestScore));
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }
}