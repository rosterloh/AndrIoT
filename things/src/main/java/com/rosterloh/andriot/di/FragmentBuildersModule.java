package com.rosterloh.andriot.di;

import com.rosterloh.andriot.ui.dash.DashFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract DashFragment contributeDashFragment();
}
