package com.rosterloh.thingsclient.di;

import android.app.Application;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ViewModelModule.class)
class AppModule {
/*
    @Provides
    @Singleton
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ClientDb.class, "client.db").build();
    }

    @Provides
    @Singleton
    DeviceDao provideDeviceDao(ClientDb db) {
        return db.deviceDao();
    }
*/
    @Provides
    @Singleton
    ConnectionsClient provideConnectionsClient(Application app) {
        return new ConnectionsClient(app.getApplicationContext());
    }

    @Provides
    @Singleton
    FusedLocationProviderClient provideFusedLocationProviderClient(Application app) {
        return LocationServices.getFusedLocationProviderClient(app);
    }
}
