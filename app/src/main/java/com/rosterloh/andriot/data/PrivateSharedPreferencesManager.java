package com.rosterloh.andriot.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PrivateSharedPreferencesManager {

    public static final String SHARED_PREFERENCES_KEY = "com.rosterloh.andriot";
    public static final String REFRESH_FREQUENCY_KEY = "refresh";

    private static PrivateSharedPreferencesManager instance;

    private SharedPreferences privateSharedPreferences;

    private PrivateSharedPreferencesManager(Context context) {

        this.privateSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static PrivateSharedPreferencesManager getInstance(Context context) {

        synchronized (PrivateSharedPreferencesManager.class) {
            if (instance == null) {
                instance = new PrivateSharedPreferencesManager(context);
            }
            return instance;
        }
    }

    private void storeStringInSharedPreferences(String key, String content) {

        SharedPreferences.Editor editor = privateSharedPreferences.edit();
        editor.putString(key, content);
        editor.apply();
    }

    private String getStringFromSharedPreferences(String key) {

        return privateSharedPreferences.getString(key, "");
    }

    public void storeRefreshRate(String interval) {

        storeStringInSharedPreferences(REFRESH_FREQUENCY_KEY, interval);
    }

    public int getRefreshRate() {

        return privateSharedPreferences.getInt(REFRESH_FREQUENCY_KEY, 30);
    }
}
