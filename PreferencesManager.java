package com.boug.runner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "BougRunnerPrefs";
    private static final String KEY_COINS = "coins";
    private static final String KEY_DIAMONDS = "diamonds";
    private static final String KEY_BEST_SCORE = "bestScore";
    private static final String KEY_TOTAL_GAMES = "totalGames";
    private static final String KEY_ADS_REMOVED = "adsRemoved";
    private static final String KEY_SOUND_ENABLED = "soundEnabled";
    private static final String KEY_VIBRATION_ENABLED = "vibrationEnabled";
    
    private static PreferencesManager instance;
    private SharedPreferences preferences;

    private PreferencesManager() {}

    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }

    public void init(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addCoins(int amount) {
        int current = getCoins();
        setCoins(current + amount);
    }

    public void addDiamonds(int amount) {
        int current = getDiamonds();
        setDiamonds(current + amount);
    }

    public void setCoins(int coins) {
        preferences.edit().putInt(KEY_COINS, coins).apply();
    }

    public int getCoins() {
        return preferences.getInt(KEY_COINS, 0);
    }

    public void setDiamonds(int diamonds) {
        preferences.edit().putInt(KEY_DIAMONDS, diamonds).apply();
    }

    public int getDiamonds() {
        return preferences.getInt(KEY_DIAMONDS, 0);
    }

    public void setBestScore(int score) {
        preferences.edit().putInt(KEY_BEST_SCORE, score).apply();
    }

    public int getBestScore() {
        return preferences.getInt(KEY_BEST_SCORE, 0);
    }

    public void setTotalGames(int games) {
        preferences.edit().putInt(KEY_TOTAL_GAMES, games).apply();
    }

    public int getTotalGames() {
        return preferences.getInt(KEY_TOTAL_GAMES, 0);
    }

    public void setAdsRemoved(boolean removed) {
        preferences.edit().putBoolean(KEY_ADS_REMOVED, removed).apply();
    }

    public boolean areAdsRemoved() {
        return preferences.getBoolean(KEY_ADS_REMOVED, false);
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(KEY_VIBRATION_ENABLED, true);
    }
}
