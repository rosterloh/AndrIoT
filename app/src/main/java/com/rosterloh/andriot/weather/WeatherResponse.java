package com.rosterloh.andriot.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("weather")
    @Expose
    private List<CurrentWeather> weather = null;
    @SerializedName("main")
    @Expose
    private Main main;
    @SerializedName("dt")
    @Expose
    private Integer dt;

    public List<CurrentWeather> getWeather() {
        return weather;
    }

    public void setWeather(List<CurrentWeather> weather) {
        this.weather = weather;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

}
