package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.rosterloh.andriot.vo.Settings;

/**
 * Interface for database access for Weather related operations.
 */
@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Settings settings);

    @Query("SELECT * FROM settings")
    LiveData<Settings> load();
}
