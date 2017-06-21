package com.rosterloh.andriot.nearby;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;


import javax.inject.Inject;

import timber.log.Timber;

public class ConnectionsServer implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final GoogleApiClient googleApiClient;
    private static final String DEVICE_NAME = "AndrIoT";
    private static final String SERVICE_ID = "com.rosterloh.andriot.service";

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            Timber.d("Connection initiated from " + endpointId + " " + connectionInfo.getEndpointName());
            Nearby.Connections.acceptConnection(googleApiClient, endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution connectionResolution) {
            Timber.d("Connection from " + endpointId);
            if (connectionResolution.getStatus().isSuccess()) {
                Timber.d("Endpoint " + endpointId + " connected");
            } else {
                Timber.w("Endpoint " + endpointId + " already connected");
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Timber.d(endpointId + " disconnected");
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            Timber.d("Message from " + endpointId);
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
            switch (payloadTransferUpdate.getStatus()) {
                case PayloadTransferUpdate.Status.IN_PROGRESS:
                    Timber.d("onPayloadTransferUpdate " + payloadTransferUpdate.getBytesTransferred() + " bytes transferred");
                    break;
                case PayloadTransferUpdate.Status.SUCCESS:
                    Timber.d("onPayloadTransferUpdate completed");
                    break;
                case PayloadTransferUpdate.Status.FAILURE:
                    Timber.d("onPayloadTransferUpdate failed");
                    break;
            }
        }
    };

    @Inject
    public ConnectionsServer(Context context) {

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        googleApiClient.connect();
    }

    private void startAdvertising() {

        AdvertisingOptions advertisingOptions = new AdvertisingOptions(Strategy.P2P_CLUSTER);

        Nearby.Connections
                .startAdvertising(googleApiClient, DEVICE_NAME, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .setResultCallback((result) -> {
                    if (result.getStatus().isSuccess()) {
                        Timber.d("startAdvertising:onResult: SUCCESS");
                    } else {
                        int statusCode = result.getStatus().getStatusCode();
                        if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                            Timber.d("startAdvertising:onResult: FAILURE STATUS_ALREADY_ADVERTISING");
                        } else {
                            Timber.d("startAdvertising:onResult: FAILURE STATE_READY");
                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("GoogleApiClient Connected");
        startAdvertising();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("GoogleApiClient Connection Suspended");
        googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("GoogleApiClient Connection Failed (" + connectionResult + ")");
    }

    @Override
    protected void finalize() throws Throwable {
        Nearby.Connections.stopAllEndpoints(googleApiClient);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.finalize();
    }
}
