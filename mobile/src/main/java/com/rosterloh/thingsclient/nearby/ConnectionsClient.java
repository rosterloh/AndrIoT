package com.rosterloh.thingsclient.nearby;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.Arrays;

import javax.inject.Inject;

import timber.log.Timber;

public class ConnectionsClient implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final GoogleApiClient googleApiClient;
    private static final String SERVICE_ID = "com.rosterloh.andriot.service";
    private static final String DEVICE_NAME = "ThingsClient";
    private String serverEndpointId;

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
            Timber.d("onEndpointFound:" + endpointId);
            Nearby.Connections.stopDiscovery(googleApiClient);
            sendConnectionRequest(endpointId);
        }

        @Override
        public void onEndpointLost(String endpointId) {
            Timber.d("onEndpointLost:" + endpointId);
            startDiscovering();
        }
    };

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
                serverEndpointId = endpointId;
            } else {
                Timber.w("Connection to " + endpointId + " rejected");
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
            Timber.d("onPayloadReceived");
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
    public ConnectionsClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        googleApiClient.connect();
    }

    protected void startDiscovering() {

        DiscoveryOptions discoveryOptions = new DiscoveryOptions(Strategy.P2P_CLUSTER);

        Nearby.Connections
                .startDiscovery(googleApiClient, SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .setResultCallback((status) -> {
                    if (status.isSuccess()) {
                        Timber.d("Discovery result: STATUS_OK");
                    } else {
                        Timber.w("Discovery failed: " + status.getStatusMessage());
                    }
                });
    }

    protected void sendConnectionRequest(String endpointId) {
        Nearby.Connections
                .requestConnection(googleApiClient, DEVICE_NAME, endpointId, connectionLifecycleCallback)
                .setResultCallback((status) -> {
                    if (status.isSuccess()) {
                        Timber.d("Connection Request: STATUS_OK");
                    } else {
                        Timber.w("Connection Request: STATUS_ALREADY_CONNECTED_TO_ENDPOINT");
                    }
                });
    }

    public void sendMessage(String message) {

        if (serverEndpointId != null) {
            Nearby.Connections
                    .sendPayload(googleApiClient, serverEndpointId, Payload.fromBytes(message.getBytes()))
                    .setResultCallback(status -> {
                        if (!status.isSuccess())
                            Timber.w("Message not sent");
                    });
        } else {
            Timber.w("Server not connected");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startDiscovering();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("onConnectionSuspended");
        googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed (" + connectionResult + ")");
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
