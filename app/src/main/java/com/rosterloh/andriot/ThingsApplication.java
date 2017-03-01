package com.rosterloh.andriot;

import android.app.Application;
import android.content.res.Resources;

import io.realm.Realm;

public class ThingsApplication extends Application {

    private static ThingsApplication instance = null;
    //private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        //firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Realm.init(this);
        instance = this;
    }

    public static ThingsApplication getInstance() { return instance; }

    public static Resources getRes() { return instance.getResources(); }

}
