package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rosterloh.andriot.common.nearby.BaseNearby.NearbyStatus;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;
import com.rosterloh.andriot.common.nearby.MessagePayload;

import javax.inject.Inject;

import timber.log.Timber;

public class InteractViewModel extends ViewModel {

    private ConnectionsClient mConnectionsClient;
    private FusedLocationProviderClient mFusedLocationClient;

    final MutableLiveData<Location> location;

    @Inject
    InteractViewModel(ConnectionsClient connectionsClient,
                      FusedLocationProviderClient fusedLocationProviderClient) {
        mConnectionsClient = connectionsClient;
        mFusedLocationClient = fusedLocationProviderClient;

        location = new MutableLiveData<>();
    }

    LiveData<NearbyStatus> getStatus() {
        return mConnectionsClient.observeStatus();
    }

    LiveData<Location> getLocation() {
        return location;
    }

    @SuppressWarnings("MissingPermission")
    void setLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener((task) -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        location.setValue(task.getResult());
                        MessagePayload msg = new MessagePayload(location.getValue());
                        mConnectionsClient.sendMessage(MessagePayload.marshall(msg));
                    } else {
                        Timber.e("getLastLocation:exception", task.getException());
                    }
                });
    }
}
