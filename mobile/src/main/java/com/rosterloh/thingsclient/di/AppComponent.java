package com.rosterloh.thingsclient.di;

import com.rosterloh.thingsclient.ClientApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        BuildersModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(ClientApplication application);
        AppComponent build();
    }
    void inject(ClientApplication clientApplication);
}