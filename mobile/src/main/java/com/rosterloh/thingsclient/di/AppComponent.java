package com.rosterloh.thingsclient.di;

import android.app.Application;

import com.rosterloh.thingsclient.ClientApplication;
import com.rosterloh.thingsclient.ui.MainActivity;
import com.rosterloh.thingsclient.ui.interact.InteractFragment;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        MainActivityModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }
    void inject(ClientApplication clientApplication);
}