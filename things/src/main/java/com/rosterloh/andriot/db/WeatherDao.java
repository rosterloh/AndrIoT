package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.rosterloh.andriot.vo.Weather;

/**
 * Interface for database access for Weather related operations.
 */
@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Weather weather);

    @Query("SELECT * FROM " + Weather.TABLE_NAME)
    LiveData<Weather> load();
}
