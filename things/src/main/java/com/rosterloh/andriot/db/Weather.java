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

    static final String TABLE_NAME = "weather";

    private final @PrimaryKey int mId;
    @SerializedName("weather_icon")
    private final String mWeatherIcon;
    @SerializedName("temperature")
    private final Double mTemperature;
    @SerializedName("description")
    private final String mDescription;
    @SerializedName("last_update")
    private final LocalDateTime mLastUpdate;

    public Weather(int id, String weatherIcon, Double temperature, String description, LocalDateTime lastUpdate) {
        mId = id;
        mWeatherIcon = weatherIcon;
        mTemperature = temperature;
        mDescription = description;
        mLastUpdate = lastUpdate;
    }

    public Weather(WeatherResponse response) {
        mId = response.getId();
        mWeatherIcon = response.getWeather().get(0).getIcon();
        mTemperature = response.getMain().getTemp();
        mDescription = response.getWeather().get(0).getDescription();
        mLastUpdate = LocalDateTime.now();
        //mLastUpdate = LocalDateTime.ofInstant(Instant.ofEpochMilli(response.getDt()), ZoneOffset.UTC);
    }

    public int getId() {
        return mId;
    }

    public String getWeatherIcon() {
        return mWeatherIcon;
    }

    public Double getTemperature() {
        return mTemperature;
    }

    public String getDescription() {
        return mDescription;
    }

    public LocalDateTime getLastUpdate() {
        return mLastUpdate;
    }

    public long getMinutesSinceUpdate() {
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), mLastUpdate);
    }

    public String getLastUpdateTime() {
        return mLastUpdate.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
