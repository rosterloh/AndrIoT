package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;

public class LocationService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationService.class.getSimpleName();
    private final GoogleApiClient apiClient;
    protected MutableLiveData<Location> emitter = new MutableLiveData<>();

    public LocationService(Context context) {
        apiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        apiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);

            if (location != null) {
                FirebaseCrash.logcat(Log.INFO, TAG, "Location:" + location.getLatitude() + ","
                                + location.getLongitude() + " (" + location.getAccuracy() + "m)");
                emitter.setValue(location);
            }
        } catch (Throwable ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "onConnected Cause: " + ex.getMessage());
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onConnectionSuspended Cause: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onConnectionFailed Result : " + connectionResult.getErrorMessage());
    }
}
