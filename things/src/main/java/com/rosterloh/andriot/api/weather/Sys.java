package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class Sys {

    @Json(name = "type") private Integer mType;
    @Json(name = "id") private Integer mId;
    @Json(name = "message") private Double mMessage;
    @Json(name = "country") private String mCountry;
    @Json(name = "sunrise") private Integer mSunrise;
    @Json(name = "sunset") private Integer mSunset;

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
