package com.rosterloh.andriot.di;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.db.SettingsDao;
import com.rosterloh.andriot.db.ThingsDb;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.images.CameraController;
import com.rosterloh.andriot.sensors.SensorHub;
import com.rosterloh.andriot.viewmodel.ThingsViewModelFactory;
import com.rosterloh.things.common.util.LiveDataCallAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(subcomponents = ViewModelSubComponent.class)
public class AppModule {
    /*
    OkHttpClient client;

    if(BuildConfig.DEBUG) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
    } else {
        client = new OkHttpClient.Builder().build();
    }
    Retrofit.Builder().client(clent)...
    */

    @Singleton @Provides
    WeatherService provideWeatherService() {
        return new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(WeatherService.class);
    }

    @Singleton @Provides
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ThingsDb.class,"things.db").build();
    }

    @Singleton @Provides
    WeatherDao provideWeatherDao(ThingsDb db) {
        return db.weatherDao();
    }

    @Singleton @Provides
    SettingsDao provideSettingsDao(ThingsDb db) {
        return db.settingsDao();
    }

    @Singleton @Provides
    ViewModelProvider.Factory provideViewModelFactory(
            ViewModelSubComponent.Builder viewModelSubComponent) {
        return new ThingsViewModelFactory(viewModelSubComponent.build());
    }

    @Singleton @Provides
    SensorHub provideSensorHub() {
        return new SensorHub();
    }
    /*
    @Singleton @Provides
    CameraController provideCameraController(Application app, SensorHub hub) {
        return new CameraController(app, hub);
    }*/
}
