package com.rosterloh.andriot.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.rosterloh.andriot.api.WeatherResponse;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import static com.rosterloh.andriot.db.Weather.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
@TypeConverters(DateTypeConverter.class)
public class Weather {

    public static final String TABLE_NAME = "weather";

    public final @PrimaryKey int id;
    @SerializedName("weather_icon")
    public final String weatherIcon;
    @SerializedName("temperature")
    public final Double temperature;
    @SerializedName("description")
    public final String description;
    @SerializedName("last_update")
    public final LocalDateTime lastUpdate;

    public Weather(int id, String weatherIcon, Double temperature, String description, LocalDateTime lastUpdate) {
        this.id = id;
        this.weatherIcon = weatherIcon;
        this.temperature = temperature;
        this.description = description;
        this.lastUpdate = lastUpdate;
    }

    public Weather(WeatherResponse response) {
        this.id = response.getId();
        this.weatherIcon = response.getWeather().get(0).getIcon();
        this.temperature = response.getMain().getTemp();
        this.description = response.getWeather().get(0).getDescription();
        this.lastUpdate = LocalDateTime.now();
        //this.lastUpdate = LocalDateTime.ofInstant(Instant.ofEpochMilli(response.getDt()), ZoneOffset.UTC);
    }

    public long getMinutesSinceUpdate() {
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), lastUpdate);
    }

    public String getLastUpdateTime() {
        return lastUpdate.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
