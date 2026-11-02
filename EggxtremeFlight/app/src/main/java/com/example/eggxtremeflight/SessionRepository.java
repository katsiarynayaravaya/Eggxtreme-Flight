package com.example.eggxtremeflight;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionRepository {

    public static class SaveResult {
        public final String sessionId;
        public final boolean newRecord;
        public final long bestScore;

        SaveResult(String sessionId, boolean newRecord, long bestScore) {
            this.sessionId = sessionId;
            this.newRecord = newRecord;
            this.bestScore = bestScore;
        }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveSession(FirebaseUser user, int score, String skinId,
                            List<Double> trajectory, int sampleIntervalMs,
                            UserRepository.Callback<SaveResult> callback) {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        DocumentReference sessionRef = db.collection("sessions").document(); // id создаётся автоматически

        db.runTransaction(transaction -> {
                    DocumentSnapshot userSnapshot = transaction.get(userRef);
                    Long storedBest = userSnapshot.getLong("bestScore");
                    long oldBest = storedBest == null ? 0 : storedBest;

                    Map<String, Object> session = new HashMap<>();
                    session.put("uid", user.getUid());
                    session.put("score", score);
                    session.put("skinId", skinId);
                    session.put("trajectory", trajectory);
                    session.put("sampleIntervalMs", sampleIntervalMs);
                    session.put("createdAt", FieldValue.serverTimestamp());
                    transaction.set(sessionRef, session);

                    boolean newRecord = score > oldBest;
                    if (newRecord) {
                        transaction.update(userRef, "bestScore", score);
                    }
                    return new SaveResult(sessionRef.getId(), newRecord, Math.max(oldBest, score));
                })
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }
}