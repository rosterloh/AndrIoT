package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Main {

    @SerializedName("temp")
    @Expose
    private Double mTemp;
    @SerializedName("temp_min")
    @Expose
    private Double mTempMin;
    @SerializedName("temp_max")
    @Expose
    private Double mTempMax;
    @SerializedName("pressure")
    @Expose
    private Double mPressure;
    @SerializedName("humidity")
    @Expose
    private Integer mHumidity;

    public Double getTemp() {
        return mTemp;
    }

    public void setTemp(Double temp) {
        mTemp = temp;
    }

    public Double getTempMin() {
        return mTempMin;
    }

    public void setTempMin(Double tempMin) {
        mTempMin = tempMin;
    }

    public Double getTempMax() {
        return mTempMax;
    }

    public void setTempMax(Double tempMax) {
        mTempMax = tempMax;
    }

    public Double getPressure() {
        return mPressure;
    }

    public void setPressure(Double pressure) {
        mPressure = pressure;
    }

    public Integer getHumidity() {
        return mHumidity;
    }

    public void setHumidity(Integer humidity) {
        mHumidity = humidity;
    }
}
