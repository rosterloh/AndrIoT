package com.rosterloh.thingsclient.di;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.rosterloh.thingsclient.ClientApplication;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AppModule {

    @Provides
    Context provideContext(ClientApplication application) {
        return application.getApplicationContext();
    }

    @Provides
    Application provideApplication(ClientApplication application) {
        return application;
    }

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
