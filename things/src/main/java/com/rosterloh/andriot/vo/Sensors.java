package com.rosterloh.andriot.vo;

import com.google.gson.annotations.SerializedName;

public class Sensors {

    @SerializedName("temperature")
    private final Float mTemperature;
    @SerializedName("humidity")
    private final Float mHumidity;
    @SerializedName("wifi_name")
    private final String mWifiName;
    @SerializedName("wifi_ip")
    private final String mWifiIp;
    @SerializedName("eth_ip")
    private final String mEthIp;

    public Sensors(Float temperature, Float humdity, String wifiName, String wifiIp, String ethIp) {
        mTemperature = temperature;
        mHumidity = humdity;
        mWifiName = wifiName;
        mWifiIp = wifiIp;
        mEthIp = ethIp;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public String getWifiName() {
        return mWifiName;
    }

    public String getWifiIp() {
        return mWifiIp;
    }

    public String getEthIp() {
        return mEthIp;
    }
}
