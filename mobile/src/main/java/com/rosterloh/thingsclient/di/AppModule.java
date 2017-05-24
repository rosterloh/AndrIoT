package com.rosterloh.thingsclient.di;

import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.thingsclient.viewmodel.ThingsViewModelFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(subcomponents = ViewModelSubComponent.class)
class AppModule {
/*
    @Singleton @Provides
    GithubService provideGithubService() {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(GithubService.class);
    }

    @Singleton @Provides
    ThingsDb provideDb(Application app) {
        return Room.databaseBuilder(app, ThingsDb.class,"things.db").build();
    }

    @Singleton @Provides
    DeviceDao provideDeviceDao(ThingsDb db) {
        return db.deviceDao();
    }

    @Singleton @Provides
    RepoDao provideRepoDao(ThingsDb db) {
        return db.repoDao();
    }
*/
    @Singleton @Provides
    ViewModelProvider.Factory provideViewModelFactory(
            ViewModelSubComponent.Builder viewModelSubComponent) {
        return new ThingsViewModelFactory(viewModelSubComponent.build());
    }
}
