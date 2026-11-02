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

    public void saveSession(Context context, String trajectory, int score, String skinId, Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not signed in"));
            return;
        }

        Map<String, Object> sessionData = new java.util.HashMap<>();
        sessionData.put("trajectory", trajectory);
        sessionData.put("score", score);
        sessionData.put("skinId", skinId);
        sessionData.put("userId", user.getUid());
        sessionData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("sessions").add(sessionData)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void findOpponentSession(int playerScore, Callback<OpponentSession> callback) {
        // Запрос: найти сессию с score наиболее близким к playerScore
        // Ищем в диапазоне ±20% от playerScore
        int minScore = (int) (playerScore * 0.8);
        int maxScore = (int) (playerScore * 1.2);

        db.collection("sessions")
                .whereGreaterThanOrEqualTo("score", minScore)
                .whereLessThanOrEqualTo("score", maxScore)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Если база пустая - вернуть дефолтную траекторию бота
                        callback.onSuccess(createDefaultBotSession());
                        return;
                    }

                    // Найти сессию с score наиболее близким к playerScore
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
                        session.trajectory = bestMatch.getString("trajectory");
                        session.score = bestMatch.getLong("score").intValue();
                        session.skinId = bestMatch.getString("skinId");
                        callback.onSuccess(session);
                    } else {
                        callback.onSuccess(createDefaultBotSession());
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private OpponentSession createDefaultBotSession() {
        OpponentSession session = new OpponentSession();
        session.trajectory = null; // Пустая траектория - тень не будет
        session.score = 0;
        session.skinId = "skin1";
        return session;
    }

    public static class OpponentSession {
        public String trajectory;
        public int score;
        public String skinId;
    }
}
