package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class Wind {

    @Json(name = "speed") private Double mSpeed;
    @Json(name = "deg") private Double mDeg;

    public Double getSpeed() {
        return mSpeed;
    }

    public void setSpeed(Double speed) {
        mSpeed = speed;
    }

    public Double getDeg() {
        return mDeg;
    }

    public void setDeg(Double deg) {
        mDeg = deg;
    }
}
