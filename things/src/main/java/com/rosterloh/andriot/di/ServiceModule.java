package com.rosterloh.andriot.di;

import com.rosterloh.andriot.scheduler.IpJobService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract IpJobService contributeIpJobService();
}
