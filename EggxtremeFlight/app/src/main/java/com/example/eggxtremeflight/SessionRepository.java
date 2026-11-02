package com.example.eggxtremeflight;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionRepository {
    private static final String TAG = "Matchmaking";

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

    public void findOpponent(FirebaseUser user, UserRepository.Callback<Matchmaking.Opponent> callback) {
        if (user == null) {
            Log.w(TAG, "no signed-in user, using bot");
            callback.onSuccess(Matchmaking.defaultBot());
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> queryClosest(bestScoreOf(snapshot), callback))
                .addOnFailureListener(error -> {
                    Log.w(TAG, "best score unavailable, searching from zero", error);
                    queryClosest(0, callback);
                });
    }

    private void queryClosest(int targetScore, UserRepository.Callback<Matchmaking.Opponent> callback) {
        Task<QuerySnapshot> higher = db.collection("sessions")
                .whereGreaterThanOrEqualTo("score", targetScore)
                .orderBy("score")
                .limit(8)
                .get();
        Task<QuerySnapshot> lower = db.collection("sessions")
                .whereLessThan("score", targetScore)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(8)
                .get();

        Tasks.whenAllSuccess(higher, lower)
                .addOnSuccessListener(results -> {
                    List<Matchmaking.Opponent> found = new ArrayList<>();
                    collect(found, (QuerySnapshot) results.get(0));
                    collect(found, (QuerySnapshot) results.get(1));
                    Matchmaking.Opponent opponent = Matchmaking.pickClosest(found, targetScore);
                    if (opponent.bot) {
                        Log.d(TAG, "no playable session near " + targetScore + ", using bot");
                    } else {
                        Log.d(TAG, "opponent score=" + opponent.score
                                + " skin=" + opponent.skinId
                                + " samples=" + opponent.trajectory.size());
                    }
                    callback.onSuccess(opponent);
                })
                .addOnFailureListener(error -> {
                    Log.w(TAG, "opponent query failed, using bot", error);
                    callback.onSuccess(Matchmaking.defaultBot());
                });
    }

    private static int bestScoreOf(DocumentSnapshot snapshot) {
        Long best = snapshot.getLong("bestScore");
        if (best == null || best < 0) return 0;
        if (best > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return best.intValue();
    }

    private static void collect(List<Matchmaking.Opponent> out, QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Long score = document.getLong("score");
            if (score == null) continue;
            Object rawTrajectory = document.get("trajectory");
            List<?> trajectory = rawTrajectory instanceof List ? (List<?>) rawTrajectory : null;
            Long interval = document.getLong("sampleIntervalMs");
            Integer intervalMs = interval == null ? null : interval.intValue();
            out.add(Matchmaking.fromFields(
                    score.intValue(),
                    trajectory,
                    document.getString("skinId"),
                    intervalMs));
        }
    }
}
