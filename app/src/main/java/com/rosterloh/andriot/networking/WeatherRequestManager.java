package com.rosterloh.andriot.networking;

import android.content.Context;

import com.rosterloh.andriot.BuildConfig;
import com.rosterloh.andriot.data.PrivateSharedPreferencesManager;
import com.rosterloh.andriot.networking.weather.WeatherAPIService;
import com.rosterloh.andriot.weather.WeatherResponse;

import io.reactivex.MaybeSource;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRequestManager {

    private static WeatherRequestManager instance;

    private PrivateSharedPreferencesManager preferences;

    private WeatherAPIService weatherAPIService;
    //private ForecastAPIService forecastAPIService;

    private WeatherRequestManager(Context context) {

        preferences = PrivateSharedPreferencesManager.getInstance(context);

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

        this.weatherAPIService = new WeatherAPIService(retrofit, preferences);

    }

    public static WeatherRequestManager getInstance(Context context) {

        synchronized (WeatherRequestManager.class) {
            if (instance == null) {
                instance = new WeatherRequestManager(context);
            }

            return instance;
        }
    }

    public boolean isRequestingInformation() {
        return weatherAPIService.isRequestingWeather();
    }

    public MaybeSource<WeatherResponse> getWeather() {
        return weatherAPIService.requestWeather();
    }
}
