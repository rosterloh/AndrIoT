package com.rosterloh.thingsclient.di;

import com.rosterloh.thingsclient.ui.MainActivity;
import com.rosterloh.thingsclient.ui.interact.InteractFragment;
import com.rosterloh.thingsclient.ui.interact.InteractModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector(modules = InteractModule.class)
    abstract InteractFragment bindInteractFragment();
}
