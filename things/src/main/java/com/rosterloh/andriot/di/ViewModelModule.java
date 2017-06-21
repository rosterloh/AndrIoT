package com.rosterloh.andriot.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.andriot.ui.dash.DashViewModel;
import com.rosterloh.andriot.viewmodel.ThingsViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(DashViewModel.class)
    abstract ViewModel bindDashViewModel(DashViewModel dashViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ThingsViewModelFactory factory);
}
