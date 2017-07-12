package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wind {

    @SerializedName("speed")
    @Expose
    private Double mSpeed;
    @SerializedName("deg")
    @Expose
    private Double mDeg;

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
