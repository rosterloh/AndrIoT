package com.rosterloh.andriot.api.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class City {

    @SerializedName("id")
    @Expose
    private Integer mId;
    @SerializedName("name")
    @Expose
    private String mName;
    @SerializedName("coord")
    @Expose
    private Coord mCoord;
    @SerializedName("country")
    @Expose
    private String mCountry;
    @SerializedName("population")
    @Expose
    private Integer mPopulation;
    @SerializedName("sys")
    @Expose
    private Sys mSys;

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
