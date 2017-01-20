package com.rosterloh.andriot.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    private static final String APP_SETTINGS_NAME = "config";
    private static final String LAST_UPDATE_TIME_IN_MS = "last_update";

    public static long saveLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_SETTINGS_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        sp.edit().putLong(LAST_UPDATE_TIME_IN_MS, now).apply();
        return now;
    }

    public static long getLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_SETTINGS_NAME, Context.MODE_PRIVATE);
        return sp.getLong(LAST_UPDATE_TIME_IN_MS, 0);
    }

}
