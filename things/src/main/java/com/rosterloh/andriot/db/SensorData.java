package com.rosterloh.andriot.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.time.LocalDateTime;

@Entity(tableName = "sensor_data")
@TypeConverters(DateTypeConverter.class)
public class SensorData {

    @PrimaryKey
    @ColumnInfo(name = "time")
    private final LocalDateTime mTime;

    @ColumnInfo(name = "temperature")
    private float mTemperature;

    @ColumnInfo(name = "humidity")
    private float mHumidity;

    @ColumnInfo(name = "pressure")
    private float mPressure;

    @ColumnInfo(name = "eco2")
    private int mECO2;

    @ColumnInfo(name = "tvoc")
    private int mTVOC;

    public SensorData(LocalDateTime time, float temperature, float humidity, float pressure,
                      int eCO2, int tVOC) {
        mTime = time;
        mTemperature = temperature;
        mHumidity = humidity;
        mPressure = pressure;
        mECO2 = eCO2;
        mTVOC = tVOC;
    }

    @Ignore
    public SensorData(float[] th, int[] aq) {
        mTime = LocalDateTime.now();
        if (th.length > 2) {
            mTemperature = th[0];
            mHumidity = th[1];
            mPressure = th[2];
        } else {
            mTemperature = th[0];
            mHumidity = th[1];
            mPressure = 0;
        }
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

    public void setTemperature(float mTemperature) {
        this.mTemperature = mTemperature;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public void setHumidity(float mHumidity) {
        this.mHumidity = mHumidity;
    }

    public float getPressure() {
        return mPressure;
    }

    public void setPressure(float mPressure) {
        this.mPressure = mPressure;
    }

    public int getECO2() {
        return mECO2;
    }

    public void setECO2(int eco2) {
        mECO2 = eco2;
    }

    public int getTVOC() {
        return mTVOC;
    }

    public void setTVOC(int tvoc) {
        mTVOC = tvoc;
    }

    @Override
    public String toString() {
        return "SensorData{"
                + "time=" + mTime
                + ", temperature=" + mTemperature
                + ", humidity=" + mHumidity
                + ", pressure=" + mPressure
                + ", eco2=" + mECO2
                + ", tvoc=" + mTVOC
                + '}';
    }
}