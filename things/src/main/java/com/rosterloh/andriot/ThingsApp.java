package com.rosterloh.andriot;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rosterloh.andriot.di.AppInjector;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

public class ThingsApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> mDispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        AppInjector.init(this);

        AndroidThreeTen.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return mDispatchingAndroidInjector;
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            FirebaseCrash.logcat(priority, tag, message);

            if (t != null) {
                FirebaseCrash.report(t);
            }
        }
    }
}
