package com.rosterloh.andriot;

import android.app.Application;

/**
 * We use a separate App for tests to prevent initialising dependency injection.
 *
 * See {@link com.rosterloh.andriot.ThingsTestRunner}.
 */
public class TestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
