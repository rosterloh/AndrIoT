package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sys {

    @SerializedName("type")
    @Expose
    private Integer mType;
    @SerializedName("id")
    @Expose
    private Integer mId;
    @SerializedName("message")
    @Expose
    private Double mMessage;
    @SerializedName("country")
    @Expose
    private String mCountry;
    @SerializedName("sunrise")
    @Expose
    private Integer mSunrise;
    @SerializedName("sunset")
    @Expose
    private Integer mSunset;

    public Integer getType() {
        return mType;
    }

    public void setType(Integer type) {
        mType = type;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public Double getMessage() {
        return mMessage;
    }

    public void setMessage(Double message) {
        mMessage = message;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public Integer getSunrise() {
        return mSunrise;
    }

    public void setSunrise(Integer sunrise) {
        mSunrise = sunrise;
    }

    public Integer getSunset() {
        return mSunset;
    }

    public void setSunset(Integer sunset) {
        mSunset = sunset;
    }
}
