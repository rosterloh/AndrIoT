package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

/**
 * Interface for database access for Settings related operations.
 */
@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CloudSettings settings);

    @Query("SELECT * FROM cloud_settings")
    LiveData<CloudSettings> loadCloudSettings();

    //@Query("SELECT * FROM " + CloudSettings.TABLE_NAME + " WHERE name LIKE :name LIMIT 1")
    //CloudSettings findByName(String name);

    @Update
    void update(CloudSettings settings);

    @Delete
    void delete(CloudSettings settings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalSettings settings);

    @Query("SELECT * FROM local_settings")
    LiveData<LocalSettings> loadLocalSettings();

    @Update
    void update(LocalSettings settings);
}
