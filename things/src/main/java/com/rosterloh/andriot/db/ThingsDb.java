package com.rosterloh.andriot.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

/**
 * Main database description.
 */
@Database(entities = {Weather.class, CloudSettings.class, LocalSettings.class, SensorData.class},
        version = 4, exportSchema = false)
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

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new table
            database.execSQL("CREATE TABLE cloud_settings_new ("
                    + "project_id TEXT NOT NULL, "
                    + "registry_id TEXT, "
                    + "device_id TEXT, "
                    + "cloud_region TEXT, "
                    + "bridge_hostname TEXT, "
                    + "bridge_port INTEGER NOT NULL, "
                    + " PRIMARY KEY(project_id))");
            // Copy the data
            database.execSQL("INSERT INTO cloud_settings_new (project_id, registry_id, device_id, cloud_region"
                    + ", bridge_hostname, bridge_port) SELECT * FROM cloud_settings");
            // Remove the old table
            database.execSQL("DROP TABLE cloud_settings");
            // Change the table name to the correct one
            database.execSQL("ALTER TABLE cloud_settings_new RENAME TO cloud_settings");
            // Do the same for local_settings
            database.execSQL("CREATE TABLE local_settings_new ("
                    + "device_id TEXT NOT NULL, "
                    + "ip_address TEXT, "
                    + "refresh_rate INTEGER NOT NULL, "
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + " PRIMARY KEY(device_id))");
            database.execSQL("INSERT INTO local_settings_new (device_id, ip_address, refresh_rate, latitude, longitude)"
                    + " SELECT * FROM local_settings");
            database.execSQL("DROP TABLE local_settings");
            database.execSQL("ALTER TABLE local_settings_new RENAME TO local_settings");

            // And again for sensor_data
            database.execSQL("CREATE TABLE sensor_data_new ("
                    + "time TEXT NOT NULL, "
                    + "temperature REAL NOT NULL, "
                    + "humidity REAL NOT NULL, "
                    + "pressure REAL NOT NULL, "
                    + "eco2 INTEGER NOT NULL, "
                    + "tvoc INTEGER NOT NULL, "
                    + " PRIMARY KEY(time))");
            database.execSQL("INSERT INTO sensor_data_new (time, temperature, humidity, pressure, eco2, tvoc)"
                    + " SELECT * FROM sensor_data");
            database.execSQL("DROP TABLE sensor_data");
            database.execSQL("ALTER TABLE sensor_data_new RENAME TO sensor_data");
        }
    };
}
