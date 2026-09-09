package com.boug.runner.utils;

import android.content.Context;
import android.util.Log;

import com.boug.runner.BougApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;
    private FirebaseUser currentUser;

    private FirebaseManager() {}

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();
    }

    public void signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser = auth.getCurrentUser();
                    createUserDocument();
                    Log.d("FirebaseManager", "Signed in anonymously");
                } else {
                    Log.e("FirebaseManager", "Sign in failed", task.getException());
                }
            });
    }

    private void createUserDocument() {
        if (currentUser == null) return;
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLogin", System.currentTimeMillis());
        userData.put("coins", 0);
        userData.put("diamonds", 0);
        userData.put("bestScore", 0);
        userData.put("totalGames", 0);
        
        db.collection("users")
            .document(currentUser.getUid())
            .set(userData)
            .addOnSuccessListener(aVoid -> Log.d("FirebaseManager", "User document created"))
            .addOnFailureListener(e -> Log.e("FirebaseManager", "Failed to create user document", e));
    }

    public void savePlayerProgress() {
        if (currentUser == null) return;
        
        PreferencesManager prefs = PreferencesManager.getInstance();
        Map<String, Object> progress = new HashMap<>();
        progress.put("coins", prefs.getCoins());
        progress.put("diamonds", prefs.getDiamonds());
        progress.put("bestScore", prefs.getBestScore());
        progress.put("lastUpdated", System.currentTimeMillis());
        
        db.collection("users")
            .document(currentUser.getUid())
            .set(progress, SetOptions.merge())
            .addOnSuccessListener(aVoid -> Log.d("FirebaseManager", "Progress saved"))
            .addOnFailureListener(e -> Log.e("FirebaseManager", "Failed to save progress", e));
    }

    public void loadPlayerProgress(OnCompleteListener<DocumentSnapshot> listener) {
        if (currentUser == null) {
            signInAnonymously();
            return;
        }
        
        db.collection("users")
            .document(currentUser.getUid())
            .get()
            .addOnCompleteListener(listener);
    }

    public void updateLeaderboard(int score) {
        if (currentUser == null) return;
        
        db.collection("leaderboard")
            .document(currentUser.getUid())
            .set(new HashMap<String, Object>() {{
                put("score", score);
                put("updatedAt", System.currentTimeMillis());
                put("userId", currentUser.getUid());
            }}, SetOptions.merge())
            .addOnSuccessListener(aVoid -> Log.d("FirebaseManager", "Leaderboard updated"))
            .addOnFailureListener(e -> Log.e("FirebaseManager", "Failed to update leaderboard", e));
    }

    public void logEvent(String eventName, Map<String, Object> params) {
        // Analytics event logging
        // Firebase Analytics is handled automatically with bundle
        Log.d("FirebaseManager", "Event: " + eventName + " Params: " + params);
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
