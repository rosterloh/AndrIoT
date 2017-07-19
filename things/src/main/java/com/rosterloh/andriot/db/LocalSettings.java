package com.rosterloh.andriot.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Build;

import java.util.concurrent.TimeUnit;

@Entity(tableName = "local_settings")
public class LocalSettings {

    private static final long DEFAULT_REFRESH_MS = TimeUnit.MINUTES.toMillis(30);
    private static final double DEFAULT_LATITUDE = 51.5069949;
    private static final double DEFAULT_LONGITUDE = -0.1317992;

    @PrimaryKey
    @ColumnInfo(name = "device_id")
    private String mDeviceId;

    @ColumnInfo(name = "ip_address")
    private String mIpAddress;

    @ColumnInfo(name = "refresh_rate")
    private long mRefreshRate;

    @ColumnInfo(name = "latitude")
    private double mLatitude;

    @ColumnInfo(name = "longitude")
    private double mLongitude;

    public LocalSettings(String deviceId, String ipAddress, long refreshRate, double latitude, double longitude) {
        mDeviceId = deviceId;
        mIpAddress = ipAddress;
        mRefreshRate = refreshRate;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Ignore
    public LocalSettings() {
        mDeviceId = Build.SERIAL;
        mIpAddress = "";
        mRefreshRate = DEFAULT_REFRESH_MS;
        mLatitude = DEFAULT_LATITUDE;
        mLongitude = DEFAULT_LONGITUDE;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String id) {
        mDeviceId = id;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public void setIpAddress(String ip) {
        mIpAddress = ip;
    }

    public long getRefreshRate() {
        return mRefreshRate;
    }

    public void setRefreshRate(long rate) {
        mRefreshRate = rate;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Override
    public String toString() {
        return "LocalSettings{"
                + "device_id= " + mDeviceId
                + "ip_address= " + mIpAddress
                + "refresh_rate= " + mRefreshRate
                + "latitude= " + mLatitude
                + "longitude= " + mLongitude
                + '}';
    }
}
