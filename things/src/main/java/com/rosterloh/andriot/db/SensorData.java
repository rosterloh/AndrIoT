package com.rosterloh.andriot.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import org.threeten.bp.LocalDateTime;

@Entity(tableName = "sensor_data")
@TypeConverters(DateTypeConverter.class)
public class SensorData {

    @PrimaryKey
    @ColumnInfo(name = "time")
    private final LocalDateTime mTime;

    @ColumnInfo(name = "temperature")
    private final float mTemperature;

    @ColumnInfo(name = "humidity")
    private final float mHumidity;

    @ColumnInfo(name = "eco2")
    private final int mECO2;

    @ColumnInfo(name = "tvoc")
    private final int mTVOC;

    public SensorData(LocalDateTime time, float temperature, float humdity, int eco2, int tvoc) {
        mTime = time;
        mTemperature = temperature;
        mHumidity = humdity;
        mECO2 = eco2;
        mTVOC = tvoc;
    }

    @Ignore
    public SensorData(float[] th, int[] aq) {
        mTime = LocalDateTime.now();
        mTemperature = th[0];
        mHumidity = th[1];
        // Check if data is valid
        if ((aq[2] & (1 << 3)) != 0) {
            mECO2 = aq[0];
            mTVOC = aq[1];
        } else {
            mECO2 = 0;
            mTVOC = 0;
        }
    }

    public LocalDateTime getTime() {
        return mTime;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public int getEco2() {
        return mECO2;
    }

    public int getTvoc() {
        return mTVOC;
    }
}