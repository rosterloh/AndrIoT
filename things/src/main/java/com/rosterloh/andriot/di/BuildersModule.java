package com.rosterloh.andriot.di;

import com.rosterloh.andriot.ui.MainActivity;
import com.rosterloh.andriot.ui.dash.DashFragment;
import com.rosterloh.andriot.ui.dash.DashModule;
import com.rosterloh.andriot.ui.dash.GraphFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BuildersModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = DashModule.class)
    abstract DashFragment contributeDashFragment();

    @ContributesAndroidInjector(modules = DashModule.class)
    abstract GraphFragment contributeGraphFragment();
}
