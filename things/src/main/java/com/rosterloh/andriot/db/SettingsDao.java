package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

/**
 * Interface for database access for Settings related operations.
 */
@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Settings settings);

    @Query("SELECT * FROM " + Settings.TABLE_NAME)
    LiveData<Settings> load();
}
