package com.rosterloh.andriot.di;

import android.app.Application;

import com.rosterloh.andriot.ThingsApp;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import dagger.android.support.DaggerApplication;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        BuildersModule.class,
        ServiceModule.class
})
public interface AppComponent extends AndroidInjector<DaggerApplication> {

    void inject(ThingsApp thingsApp);

    @Override
    void inject(DaggerApplication instance);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }
}
