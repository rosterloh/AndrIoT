package com.rosterloh.thingsclient.di;

import com.rosterloh.thingsclient.ClientApplication;

public class AppInjector {

    private AppInjector() {}

    public static void init(ClientApplication clientApplication) {
        DaggerAppComponent.builder()
                .application(clientApplication)
                .build()
                .inject(clientApplication);
    }
}
