package com.rosterloh.andriot.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Main database description.
 */
@Database(entities = {Weather.class, Settings.class}, version = 1, exportSchema = false)
public abstract class ThingsDb extends RoomDatabase {

    public abstract WeatherDao weatherDao();

    public abstract SettingsDao settingsDao();
}
