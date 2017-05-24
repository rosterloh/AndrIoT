package com.rosterloh.andriot.api;

import android.arch.lifecycle.LiveData;

import com.rosterloh.things.common.api.ApiResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * REST API access points
 */
public interface WeatherService {

    @GET("weather")
    LiveData<ApiResponse<WeatherResponse>> getWeather(@Query("lat") String lat,
                                              @Query("lon") String lon,
                                              @Query("APPID") String apiKey,
                                              @Query("units") String units);
}
