package com.rosterloh.andriot.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rosterloh.andriot.api.weather.Clouds;
import com.rosterloh.andriot.api.weather.Coord;
import com.rosterloh.andriot.api.weather.CurrentWeather;
import com.rosterloh.andriot.api.weather.Main;
import com.rosterloh.andriot.api.weather.Sys;
import com.rosterloh.andriot.api.weather.Wind;

import java.util.List;

public class WeatherResponse {

    @SerializedName("coord")
    @Expose
    private Coord mCoord;
    @SerializedName("weather")
    @Expose
    private List<CurrentWeather> mWeather = null;
    @SerializedName("base")
    @Expose
    private String mBase;
    @SerializedName("main")
    @Expose
    private Main mMain;
    @SerializedName("visibility")
    @Expose
    private Integer mVisibility;
    @SerializedName("wind")
    @Expose
    private Wind mWind;
    @SerializedName("clouds")
    @Expose
    private Clouds mClouds;
    @SerializedName("dt")
    @Expose
    private Integer mDt;
    @SerializedName("sys")
    @Expose
    private Sys mSys;
    @SerializedName("id")
    @Expose
    private Integer mId;
    @SerializedName("name")
    @Expose
    private String mName;
    @SerializedName("cod")
    @Expose
    private Integer mCod;

    public Coord getCoord() {
        return mCoord;
    }

    public void setCoord(Coord coord) {
        mCoord = coord;
    }

    public List<CurrentWeather> getWeather() {
        return mWeather;
    }

    public void setWeather(List<CurrentWeather> weather) {
        mWeather = weather;
    }

    public String getBase() {
        return mBase;
    }

    public void setBase(String base) {
        mBase = base;
    }

    public Main getMain() {
        return mMain;
    }

    public void setMain(Main main) {
        mMain = main;
    }

    public Integer getVisibility() {
        return mVisibility;
    }

    public void setVisibility(Integer visibility) {
        mVisibility = visibility;
    }

    public Wind getWind() {
        return mWind;
    }

    public void setWind(Wind wind) {
        mWind = wind;
    }

    public Clouds getClouds() {
        return mClouds;
    }

    public void setClouds(Clouds clouds) {
        mClouds = clouds;
    }

    public Integer getDt() {
        return mDt;
    }

    public void setDt(Integer dt) {
        mDt = dt;
    }

    public Sys getSys() {
        return mSys;
    }

    public void setSys(Sys sys) {
        mSys = sys;
    }

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

    public Integer getCod() {
        return mCod;
    }

    public void setCod(Integer cod) {
        mCod = cod;
    }

}
