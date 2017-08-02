package com.rosterloh.andriot.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

/**
 * Main database description.
 */
@Database(entities = {Weather.class, CloudSettings.class, LocalSettings.class, SensorData.class},
        version = 3, exportSchema = false)
public abstract class ThingsDb extends RoomDatabase {

    public abstract WeatherDao weatherDao();

    public abstract SettingsDao settingsDao();

    public abstract SensorDao sensorDao();

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE sensor_data ("
                    + "time TEXT, "
                    + "temperature REAL NOT NULL, "
                    + "humidity REAL NOT NULL, "
                    + "pressure REAL NOT NULL, "
                    + "eco2 INTEGER NOT NULL, "
                    + "tvoc INTEGER NOT NULL, "
                    + "PRIMARY KEY(time))");
        }
    };
}
