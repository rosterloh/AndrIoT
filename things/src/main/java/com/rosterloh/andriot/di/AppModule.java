package com.rosterloh.andriot.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.SensorManager;

import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.bluetooth.GattServer;
import com.rosterloh.andriot.db.SensorDao;
import com.rosterloh.andriot.db.SettingsDao;
import com.rosterloh.andriot.db.SettingsRepository;
import com.rosterloh.andriot.db.ThingsDb;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.nearby.ConnectionsServer;
import com.rosterloh.andriot.scheduler.ThingsScheduler;

import java.io.InputStream;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class AppModule {

    @Provides
    Context provideContext(Application application) {
        return application.getApplicationContext();
    }

    @Provides
    InputStream provideInputStream(Context context) {
        return context.getResources().openRawResource(context.
                getResources().getIdentifier("rsa_private_pkcs8", "raw", context.getPackageName()));
    }

    @Provides @Singleton
    WeatherService provideWeatherService() {
        return new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService.class);
    }

    @Provides @Singleton
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ThingsDb.class, "things.db")
                .addMigrations(ThingsDb.MIGRATION_2_3, ThingsDb.MIGRATION_3_4, ThingsDb.MIGRATION_4_5)
                .build();
    }

    @Provides @Singleton
    WeatherDao provideWeatherDao(ThingsDb db) {
        return db.weatherDao();
    }

    @Provides @Singleton
    SettingsDao provideSettingsDao(ThingsDb db) {
        return db.settingsDao();
    }

    @Provides @Singleton
    SensorDao provideSensorDao(ThingsDb db) {
        return db.sensorDao();
    }

    @Provides @Singleton
    SensorManager provideSensorManager(Application app) {
        return (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
    }

    @Provides @Singleton
    ConnectionsServer provideConnectionsServer(Context context, SettingsRepository settingsRepository) {
        return new ConnectionsServer(context, settingsRepository);
    }

    @Provides @Singleton
    GattServer provideGattServer(Context context) {
        return new GattServer(context);
    }

    @Provides @Singleton
    ThingsScheduler provideScheduler(Context context) {
        return new ThingsScheduler(context);
    }
    /*
    @Provides @Singleton
    CameraController provideCameraController(Application app, SensorHub hub) {
        return new CameraController(app, hub);
    }*/
}
