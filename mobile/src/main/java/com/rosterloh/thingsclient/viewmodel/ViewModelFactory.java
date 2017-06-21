package com.rosterloh.thingsclient.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.thingsclient.ui.interact.InteractViewModel;

import javax.inject.Inject;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private InteractViewModel interactViewModel;

    @Inject
    public ViewModelFactory(InteractViewModel viewModel) {
        this.interactViewModel = viewModel;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InteractViewModel.class)) {
            return (T) interactViewModel;
        }
        throw new IllegalArgumentException("Unknown class name");
    }
}
