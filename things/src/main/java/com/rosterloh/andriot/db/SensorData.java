package com.rosterloh.andriot.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.time.LocalDateTime;

@Entity(tableName = "sensor_data")
@TypeConverters(DateTypeConverter.class)
public class SensorData {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "time")
    private final LocalDateTime mTime;

    @ColumnInfo(name = "temperature")
    private float mTemperature;

    @ColumnInfo(name = "humidity")
    private float mHumidity;

    @ColumnInfo(name = "pressure")
    private float mPressure;

    @ColumnInfo(name = "air_quality")
    private float mAirQuality;

    public SensorData(@NonNull LocalDateTime time, float temperature, float humidity, float pressure,
                      float airQuality) {
        mTime = time;
        mTemperature = temperature;
        mHumidity = humidity;
        mPressure = pressure;
        mAirQuality = airQuality;
    }

    @Ignore
    public SensorData(float[] values) {
        mTime = LocalDateTime.now();
        if (values.length > 3) {
            mTemperature = values[0];
            mHumidity = values[1];
            mPressure = values[2];
            mAirQuality = values[3];
        } else {
            mTemperature = 0;
            mHumidity = 0;
            mPressure = 0;
            mAirQuality = 0;
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

    public float getAirQuality() {
        return mAirQuality;
    }

    public void setAirQuality(float aq) {
        mAirQuality = aq;
    }

    @Override
    public String toString() {
        return "SensorData{"
                + "time=" + mTime
                + ", temperature=" + mTemperature
                + ", humidity=" + mHumidity
                + ", pressure=" + mPressure
                + ", air_quality=" + mAirQuality
                + '}';
    }
}