package com.rosterloh.andriot.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.bluetooth.GattServer;
import com.rosterloh.andriot.db.SettingsDao;
import com.rosterloh.andriot.db.SettingsRepository;
import com.rosterloh.andriot.db.ThingsDb;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.nearby.ConnectionsServer;
import com.rosterloh.andriot.sensors.SensorHub;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = ViewModelModule.class)
class AppModule {

    @Singleton
    @Provides
    WeatherService provideWeatherService() {
        return new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService.class);
    }

    @Singleton
    @Provides
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ThingsDb.class, "things.db").build();
    }

    @Singleton
    @Provides
    WeatherDao provideWeatherDao(ThingsDb db) {
        return db.weatherDao();
    }

    @Singleton
    @Provides
    SettingsDao provideSettingsDao(ThingsDb db) {
        return db.settingsDao();
    }

    @Singleton
    @Provides
    SensorHub provideSensorHub() {
        return new SensorHub();
    }

    @Singleton
    @Provides
    ConnectionsServer provideConnectionsServer(Application app, SettingsRepository settingsRepository) {
        return new ConnectionsServer(app.getApplicationContext(), settingsRepository);
    }

    @Singleton
    @Provides
    GattServer provideGattServer(Application app) {
        return new GattServer(app.getApplicationContext());
    }

    /*
    @Singleton
    @Provides
    MQTTPublisher provideMqttPublisher(SettingsDao settingsDao) {
        return new MQTTPublisher(settingsDao);
    }
    */

    /*
    @Singleton
    @Provides
    CameraController provideCameraController(Application app, SensorHub hub) {
        return new CameraController(app, hub);
    }*/
}
