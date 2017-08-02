package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Interface for database access for SensorData related operations.
 */
@Dao
public interface SensorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SensorData data);

    @Query("SELECT * FROM sensor_data")
    LiveData<List<SensorData>> load();

    @Query("SELECT COUNT(*) FROM sensor_data")
    int getCount();

    @Delete
    void delete(SensorData data);
}
