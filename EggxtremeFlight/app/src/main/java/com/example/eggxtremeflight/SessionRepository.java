package com.example.eggxtremeflight;

import android.util.Log;

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
import java.util.Random;

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
                            List<Double> pipeTops, int worldWidth, String nickname,
                            float scrollSpeed, float spawnInterval,
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
                    session.put("pipeTops", pipeTops == null ? new ArrayList<>() : pipeTops);
                    session.put("worldWidth", worldWidth);
                    session.put("scrollSpeed", scrollSpeed);
                    session.put("spawnInterval", spawnInterval);
                    session.put("sampleIntervalMs", sampleIntervalMs);
                    if (nickname != null && !nickname.isEmpty()) {
                        session.put("nickname", nickname);
                    }
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

    public void findOpponent(FirebaseUser user, Matchmaking.Opponent avoid,
                             UserRepository.Callback<Matchmaking.Opponent> callback) {
        if (user == null) {
            Log.w(TAG, "no signed-in user, using bot");
            callback.onSuccess(Matchmaking.defaultBot());
            return;
        }

        String uid = user.getUid();
        com.google.android.gms.tasks.Task<DocumentSnapshot> profile =
                db.collection("users").document(uid).get();
        com.google.android.gms.tasks.Task<QuerySnapshot> ownRuns =
                db.collection("sessions").whereEqualTo("uid", uid).limit(40).get();
        Tasks.whenAllSuccess(profile, ownRuns)
                .addOnSuccessListener(results -> {
                    DocumentSnapshot profileDoc = (DocumentSnapshot) results.get(0);
                    QuerySnapshot runs = (QuerySnapshot) results.get(1);
                    Long storedBest = profileDoc.getLong("bestScore");
                    int best = storedBest == null ? 0 : storedBest.intValue();
                    List<Integer> scores = new ArrayList<>();
                    for (DocumentSnapshot document : runs.getDocuments()) {
                        Long score = document.getLong("score");
                        if (score != null) scores.add(score.intValue());
                    }
                    queryBand(uid, Matchmaking.skillBand(scores, best), avoid, callback);
                })
                .addOnFailureListener(error -> {
                    Log.w(TAG, "could not read player level, using a beginner band", error);
                    queryBand(uid, Matchmaking.skillBand(null, 0), avoid, callback);
                });
    }

    private void queryBand(String uid, Matchmaking.SkillBand band, Matchmaking.Opponent avoid,
                           UserRepository.Callback<Matchmaking.Opponent> callback) {
        Log.d(TAG, "band average=" + band.average + " floor=" + band.floor + " ceiling=" + band.ceiling);
        com.google.android.gms.tasks.Task<QuerySnapshot> stronger = db.collection("sessions")
                .whereGreaterThanOrEqualTo("score", band.floor)
                .whereLessThanOrEqualTo("score", band.ceiling)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20)
                .get();
        com.google.android.gms.tasks.Task<QuerySnapshot> closer = db.collection("sessions")
                .whereGreaterThanOrEqualTo("score", band.floor)
                .whereLessThanOrEqualTo("score", band.ceiling)
                .orderBy("score")
                .limit(20)
                .get();
        Tasks.whenAllSuccess(stronger, closer)
                .addOnSuccessListener(results -> {
                    List<Matchmaking.Opponent> found = new ArrayList<>();
                    collect(found, (QuerySnapshot) results.get(0));
                    collect(found, (QuerySnapshot) results.get(1));
                    Matchmaking.Opponent opponent = Matchmaking.pickRandom(
                            Matchmaking.inRange(found, band.floor, band.ceiling),
                            uid,
                            new Random(),
                            avoid);
                    resolveNickname(opponent, callback);
                })
                .addOnFailureListener(error -> {
                    Log.w(TAG, "opponent query failed, using bot", error);
                    callback.onSuccess(Matchmaking.defaultBot());
                });
    }

    private void resolveNickname(Matchmaking.Opponent opponent,
                                 UserRepository.Callback<Matchmaking.Opponent> callback) {
        if (opponent.bot) {
            Log.d(TAG, "no other session, using bot");
            callback.onSuccess(opponent);
            return;
        }
        Log.d(TAG, "opponent uid=" + opponent.uid
                + " name=" + opponent.nickname
                + " score=" + opponent.score
                + " pipes=" + opponent.pipeTops.size());
        if (opponent.uid == null || (opponent.nickname != null && !opponent.nickname.isEmpty())) {
            callback.onSuccess(opponent);
            return;
        }
        db.collection("users").document(opponent.uid).get()
                .addOnSuccessListener(document -> {
                    String nickname = document.getString("nickname");
                    callback.onSuccess(nickname == null ? opponent : opponent.withNickname(nickname));
                })
                .addOnFailureListener(error -> {
                    Log.w(TAG, "nickname lookup failed", error);
                    callback.onSuccess(opponent);
                });
    }

    private static float readFloat(DocumentSnapshot document, String key) {
        Object value = document.get(key);
        if (value instanceof Number) return ((Number) value).floatValue();
        return 0f;
    }

    private static void collect(List<Matchmaking.Opponent> out, QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Long score = document.getLong("score");
            if (score == null) continue;
            Object rawTrajectory = document.get("trajectory");
            List<?> trajectory = rawTrajectory instanceof List ? (List<?>) rawTrajectory : null;
            Long interval = document.getLong("sampleIntervalMs");
            Integer intervalMs = interval == null ? null : interval.intValue();
            Object rawPipes = document.get("pipeTops");
            List<?> pipeTops = rawPipes instanceof List ? (List<?>) rawPipes : null;
            Long width = document.getLong("worldWidth");
            Integer worldWidth = width == null ? null : width.intValue();
            out.add(Matchmaking.fromFields(
                    score.intValue(),
                    trajectory,
                    document.getString("skinId"),
                    intervalMs,
                    document.getString("uid"),
                    document.getString("nickname"),
                    pipeTops,
                    worldWidth,
                    readFloat(document, "scrollSpeed"),
                    readFloat(document, "spawnInterval")));
        }
    }
}
