package com.example.eggxtremeflight;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String PREFS = "GameSharedPreferences";
    private static final String KEY_NICKNAME = "nickname";

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void ensureSignedIn(Callback<FirebaseUser> callback) {
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            callback.onSuccess(current);
            return;
        }
        auth.signInAnonymously()
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(callback::onError);
    }

    public static String getCachedNickname(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_NICKNAME, null);
    }

    private static void cacheNickname(Context context, String nickname) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_NICKNAME, nickname).apply();
    }

    public void loadNickname(Context context, FirebaseUser user, Callback<String> callback) {
        Context appContext = context.getApplicationContext();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String nickname = doc.getString("nickname");
                    if (nickname != null) {
                        cacheNickname(appContext, nickname);
                    }
                    callback.onSuccess(nickname);
                })
                .addOnFailureListener(callback::onError);
    }

    public void saveNickname(Context context, FirebaseUser user, String nickname,
                             Callback<Void> callback) {
        Context appContext = context.getApplicationContext();
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);
        data.put("bestScore", 0);
        data.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    cacheNickname(appContext, nickname);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }
}