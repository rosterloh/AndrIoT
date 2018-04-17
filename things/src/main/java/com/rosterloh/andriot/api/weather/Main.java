package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class Main {

    @Json(name = "temp") private Double mTemp;
    @Json(name = "temp_min") private Double mTempMin;
    @Json(name = "temp_max") private Double mTempMax;
    @Json(name = "pressure") private Double mPressure;
    @Json(name = "humidity") private Integer mHumidity;

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
