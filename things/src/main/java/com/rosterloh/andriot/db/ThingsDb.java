package com.rosterloh.andriot.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.rosterloh.andriot.vo.Settings;
import com.rosterloh.andriot.vo.Weather;

/**
 * Main database description.
 */
@Database(entities = {Weather.class, Settings.class}, version = 1)
public abstract class ThingsDb extends RoomDatabase {

    public abstract WeatherDao weatherDao();

    public abstract SettingsDao settingsDao();
}
