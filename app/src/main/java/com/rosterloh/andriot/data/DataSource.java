package com.rosterloh.andriot.data;

import com.rosterloh.andriot.weather.Weather;

import io.reactivex.Observable;

/**
 * Main entry point for accessing data.
 */
public interface DataSource {

    Observable<Weather> getWeather();
}
