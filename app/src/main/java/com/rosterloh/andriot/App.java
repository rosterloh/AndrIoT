package com.rosterloh.andriot;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

public class App extends Application {

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

}
