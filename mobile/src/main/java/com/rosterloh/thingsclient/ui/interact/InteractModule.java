package com.rosterloh.thingsclient.ui.interact;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;

import dagger.Module;
import dagger.Provides;

@Module
public class InteractModule {

    @Provides
    InteractViewModelFactory provideInteractViewModelFactory(ConnectionsClient connectionsClient,
                                                             FusedLocationProviderClient fusedLocationProviderClient) {
        return new InteractViewModelFactory(connectionsClient, fusedLocationProviderClient);
    }
}
