package com.rosterloh.thingsclient.nearby;

import android.arch.lifecycle.LiveData;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import javax.inject.Inject;

import timber.log.Timber;

public class LocationLiveData extends LiveData<Location> {

    private FusedLocationProviderClient locationProviderClient;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                setValue(location);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            Timber.d(locationAvailability.toString());
        }
    };

    @Inject
    public LocationLiveData(FusedLocationProviderClient fusedLocationProviderClient) {
        locationProviderClient = fusedLocationProviderClient;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onActive() {
        // Try to immediately find a location
        locationProviderClient.getLastLocation()
                .addOnCompleteListener((task) -> {
                    if (task.isSuccessful()) {
                        Location lastLocation = task.getResult();
                        if (lastLocation != null) {
                            setValue(lastLocation);
                        }
                    }
                });

        locationProviderClient.requestLocationUpdates(new LocationRequest(), locationCallback, null);
    }

    @Override
    protected void onInactive() {
        locationProviderClient.removeLocationUpdates(locationCallback);
    }
}
