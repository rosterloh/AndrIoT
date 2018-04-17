package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class Coord {

    @Json(name = "lon") private Double mLon;
    @Json(name = "lat") private Double mLat;

    public Double getLon() {
        return mLon;
    }

    public void setLon(Double lon) {
        mLon = lon;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }
}
