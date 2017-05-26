package com.rosterloh.andriot.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.rosterloh.andriot.db.DateTypeConverter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

@Entity(tableName = "weather")
@TypeConverters(DateTypeConverter.class)
public class Weather {

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

    public long getMinutesSinceUpdate() {
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), lastUpdate);
    }

    public String getLastUpdateTime() {
        return lastUpdate.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
