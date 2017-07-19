package com.rosterloh.thingsclient.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.thingsclient.ui.interact.InteractViewModel;
import com.rosterloh.thingsclient.viewmodel.ClientViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(InteractViewModel.class)
    abstract ViewModel bindInteractViewModel(InteractViewModel interactViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ClientViewModelFactory factory);
}
