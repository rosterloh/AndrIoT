package com.rosterloh.andriot.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rosterloh.andriot.api.weather.Clouds;
import com.rosterloh.andriot.api.weather.Coord;
import com.rosterloh.andriot.api.weather.CurrentWeather;
import com.rosterloh.andriot.api.weather.Main;
import com.rosterloh.andriot.api.weather.Sys;
import com.rosterloh.andriot.api.weather.Wind;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity
public class Weather {

    public final @PrimaryKey int id;
    @SerializedName("weather_icon")
    public final String weatherIcon;
    @SerializedName("temperature")
    public final String temperature;
    @SerializedName("description")
    public final String description;
    @SerializedName("last_update")
    public final long lastUpdate;
    @SerializedName("update_string")
    public final String updateString;

    public Weather(int id, String weatherIcon, String temperature, String description, long lastUpdate, String updateString) {
        this.id = id;
        this.weatherIcon = weatherIcon;
        this.temperature = temperature;
        this.description = description;
        this.lastUpdate = lastUpdate;
        this.updateString = updateString;
    }
}
