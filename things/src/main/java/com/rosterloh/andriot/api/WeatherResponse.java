package com.rosterloh.andriot.api;

import com.rosterloh.andriot.api.weather.Clouds;
import com.rosterloh.andriot.api.weather.Coord;
import com.rosterloh.andriot.api.weather.CurrentWeather;
import com.rosterloh.andriot.api.weather.Main;
import com.rosterloh.andriot.api.weather.Sys;
import com.rosterloh.andriot.api.weather.Wind;
import com.squareup.moshi.Json;

import java.util.List;

public class WeatherResponse {

    @Json(name = "coord") private Coord mCoord;
    @Json(name = "weather") private List<CurrentWeather> mWeather = null;
    @Json(name = "base") private String mBase;
    @Json(name = "main") private Main mMain;
    @Json(name = "visibility") private Integer mVisibility;
    @Json(name = "wind") private Wind mWind;
    @Json(name = "clouds") private Clouds mClouds;
    @Json(name = "dt") private Integer mDt;
    @Json(name = "sys") private Sys mSys;
    @Json(name = "id") private Integer mId;
    @Json(name = "name") private String mName;
    @Json(name = "cod") private Integer mCod;

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
