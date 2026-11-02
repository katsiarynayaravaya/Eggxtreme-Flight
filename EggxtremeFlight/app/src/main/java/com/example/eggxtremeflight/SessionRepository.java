package com.example.eggxtremeflight;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionRepository {
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveSession(FirebaseUser user, int score, String skinId, List<Double> trajectory, int sampleIntervalMs, Callback<SaveResult> callback) {
        if (user == null) {
            callback.onError(new Exception("User not signed in"));
            return;
        }

        Map<String, Object> sessionData = new java.util.HashMap<>();
        sessionData.put("score", score);
        sessionData.put("skinId", skinId);
        sessionData.put("trajectory", trajectory);
        sessionData.put("sampleIntervalMs", sampleIntervalMs);
        sessionData.put("userId", user.getUid());
        sessionData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("sessions").add(sessionData)
                .addOnSuccessListener(documentReference -> {
                    SaveResult result = new SaveResult();
                    result.sessionId = documentReference.getId();
                    result.newRecord = false; // TODO: check if new record
                    result.bestScore = score; // TODO: fetch actual best score
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }

    public void findOpponentSession(int playerScore, Callback<OpponentSession> callback) {
        int minScore = (int) (playerScore * 0.8);
        int maxScore = (int) (playerScore * 1.2);

        db.collection("sessions")
                .whereGreaterThanOrEqualTo("score", minScore)
                .whereLessThanOrEqualTo("score", maxScore)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(createDefaultBotSession());
                        return;
                    }

                    DocumentSnapshot bestMatch = null;
                    int minDiff = Integer.MAX_VALUE;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        int score = doc.getLong("score").intValue();
                        int diff = Math.abs(score - playerScore);
                        if (diff < minDiff) {
                            minDiff = diff;
                            bestMatch = doc;
                        }
                    }

                    if (bestMatch != null) {
                        OpponentSession session = new OpponentSession();
                        session.trajectory = (List<Double>) bestMatch.get("trajectory");
                        session.score = bestMatch.getLong("score").intValue();
                        session.skinId = bestMatch.getString("skinId");
                        session.sampleIntervalMs = bestMatch.getLong("sampleIntervalMs").intValue();
                        callback.onSuccess(session);
                    } else {
                        callback.onSuccess(createDefaultBotSession());
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private OpponentSession createDefaultBotSession() {
        OpponentSession session = new OpponentSession();
        session.trajectory = new ArrayList<>();
        session.score = 0;
        session.skinId = "skin1";
        session.sampleIntervalMs = 100;
        return session;
    }

    public static class SaveResult {
        public String sessionId;
        public boolean newRecord;
        public int bestScore;
    }

    public static class OpponentSession {
        public List<Double> trajectory;
        public int score;
        public String skinId;
        public int sampleIntervalMs;
    }
}
