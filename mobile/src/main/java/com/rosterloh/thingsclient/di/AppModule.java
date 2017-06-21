package com.rosterloh.thingsclient.di;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.thingsclient.ClientApplication;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;
import com.rosterloh.thingsclient.ui.interact.InteractViewModel;
import com.rosterloh.thingsclient.viewmodel.ViewModelFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AppModule {
/*
    @Provides
    @Singleton
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ThingsDb.class,"things.db").build();
    }

    @Provides
    @Singleton
    DeviceDao provideDeviceDao(ThingsDb db) {
        return db.deviceDao();
    }
*/
    @Provides
    @Singleton
    ConnectionsClient provideConnectionsClient(Application app) {
        return new ConnectionsClient(app.getApplicationContext());
    }

    @Provides
    ViewModel provideInteractViewModel(InteractViewModel viewModel) {
        return viewModel;
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(ViewModelFactory factory){
        return factory;
    }
}
