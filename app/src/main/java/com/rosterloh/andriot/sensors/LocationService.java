package com.rosterloh.andriot.sensors;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;

public class LocationService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final Context context;
    private final GoogleApiClient apiClient;
    protected MaybeEmitter<Location> emitter;

    public LocationService(Context context) {
        this.context = context.getApplicationContext();
        apiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public Maybe<Location> getLastLocation() {

        return Maybe.create(locationEmitter -> {
            try {
                apiClient.connect();
            } catch (Throwable ex) {
                locationEmitter.onError(ex);
            }

            locationEmitter.setCancellable(() -> apiClient.disconnect() );
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            //noinspection MissingPermission
            Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);

            if (location != null) {
                emitter.onSuccess(location);
            } else {
                emitter.onComplete();
            }
        } catch (Throwable ex) {
            emitter.onError(ex);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        emitter.onError(new RuntimeException("Cause: " + i));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        emitter.onError(new RuntimeException("Error connecting to GoogleApiClient."));
    }
}
