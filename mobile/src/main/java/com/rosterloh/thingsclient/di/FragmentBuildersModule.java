package com.rosterloh.thingsclient.di;

import com.rosterloh.thingsclient.ui.interact.InteractFragment;
import com.rosterloh.thingsclient.ui.scan.ScanFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract InteractFragment contributeInteractFragment();

    @ContributesAndroidInjector
    abstract ScanFragment contributeScanFragment();
}
