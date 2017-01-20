package com.rosterloh.andriot.weather;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather")
    Flowable<WeatherResponse> getCurrentWeatherConditions(@Query("lat") String lat,
                                                          @Query("lon") String lon,
                                                          @Query("APPID") String apiKey,
                                                          @Query("units") String units);
}
