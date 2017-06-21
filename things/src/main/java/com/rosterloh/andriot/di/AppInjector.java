package com.rosterloh.andriot.di;

import com.rosterloh.andriot.ThingsApp;

public class AppInjector {

    public static void init(ThingsApp thingsApp) {
        DaggerAppComponent.builder()
                .application(thingsApp)
                .build()
                .inject(thingsApp);
    }
}
