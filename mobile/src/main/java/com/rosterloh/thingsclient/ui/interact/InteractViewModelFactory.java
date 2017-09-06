package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;

public class InteractViewModelFactory implements ViewModelProvider.Factory {

    private final ConnectionsClient connectionsClient;

    private final FusedLocationProviderClient fusedLocationProviderClient;

    InteractViewModelFactory(ConnectionsClient connectionsClient, FusedLocationProviderClient fusedLocationProviderClient) {
        this.connectionsClient = connectionsClient;
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InteractViewModel.class)) {
            return (T) new InteractViewModel(connectionsClient, fusedLocationProviderClient);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}