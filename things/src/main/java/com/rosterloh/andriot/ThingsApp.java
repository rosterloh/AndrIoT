package com.rosterloh.andriot;

import com.google.android.things.AndroidThings;
import com.rosterloh.andriot.di.AppComponent;
import com.rosterloh.andriot.di.DaggerAppComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class ThingsApp extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        AppComponent appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.i("Built with Things Version: " + AndroidThings.getVersionString());
    }
}
