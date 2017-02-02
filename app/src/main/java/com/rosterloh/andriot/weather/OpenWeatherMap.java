package com.rosterloh.andriot.weather;

import com.rosterloh.andriot.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class OpenWeatherMap {

    private WeatherApi weatherApi;

    public OpenWeatherMap() {

        OkHttpClient client;

        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        } else {
            client = new OkHttpClient.Builder().build();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);
    }

    public Observable<Weather> getCurrentWeather(WeatherResponse response) {

        return Observable.just(new Weather.Builder()
                .temperature(response.getMain().getTemp().intValue() + "º")
                .description(response.getWeather().get(0).getDescription())
                .iconId(response.getWeather().get(0).getIcon())
                .lastUpdated(new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date((long) response.getDt() * 1000)))
                .build());
    }

    public WeatherApi getApi() {
        return weatherApi;
    }

    public interface WeatherApi {

        @GET("weather")
        Observable<WeatherResponse> getCurrentWeatherConditions(@Query("lat") String lat,
                                                                @Query("lon") String lon,
                                                                @Query("APPID") String apiKey,
                                                                @Query("units") String units);
    }
}
