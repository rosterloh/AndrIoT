package com.rosterloh.andriot.networking.weather;

import com.rosterloh.andriot.weather.WeatherResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IWeatherAPI {

    @GET("weather")
    Observable<WeatherResponse> getCurrentWeatherConditions(@Query("lat") String lat,
                                                            @Query("lon") String lon,
                                                            @Query("APPID") String apiKey,
                                                            @Query("units") String units);

}
