package com.rosterloh.andriot.di;

import com.rosterloh.andriot.ui.dash.DashFragment;
import com.rosterloh.andriot.ui.dash.GraphFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract DashFragment contributeDashFragment();

    @ContributesAndroidInjector
    abstract GraphFragment contributeGraphFragment();
}
