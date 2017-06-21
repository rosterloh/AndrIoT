package com.rosterloh.andriot.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * REST API access points
 */
public interface WeatherService {

    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("lat") String lat,
                                     @Query("lon") String lon,
                                     @Query("APPID") String apiKey,
                                     @Query("units") String units);
}
