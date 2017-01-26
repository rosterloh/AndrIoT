package com.rosterloh.andriot.weather;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather")
    Observable<WeatherResponse> getCurrentWeatherConditions(@Query("lat") String lat,
                                                            @Query("lon") String lon,
                                                            @Query("APPID") String apiKey,
                                                            @Query("units") String units);
}
