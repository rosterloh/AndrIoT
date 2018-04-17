package com.rosterloh.andriot.api.weather;

import com.squareup.moshi.Json;

public class City {

    @Json(name = "id") private Integer mId;
    @Json(name = "name") private String mName;
    @Json(name = "coord") private Coord mCoord;
    @Json(name = "country") private String mCountry;
    @Json(name = "population") private Integer mPopulation;
    @Json(name = "sys") private Sys mSys;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Coord getCoord() {
        return mCoord;
    }

    public void setCoord(Coord coord) {
        mCoord = coord;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public Integer getPopulation() {
        return mPopulation;
    }

    public void setPopulation(Integer population) {
        mPopulation = population;
    }

    public Sys getSys() {
        return mSys;
    }

    public void setSys(Sys sys) {
        mSys = sys;
    }
}
