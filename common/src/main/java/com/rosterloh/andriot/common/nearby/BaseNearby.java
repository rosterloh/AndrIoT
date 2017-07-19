package com.rosterloh.andriot.common.nearby;

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
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import static com.rosterloh.andriot.common.BuildConfig.NEARBY_SERVICE_ID;

public abstract class BaseNearby implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public enum NearbyStatus {
        CONNECTING,
        DISCOVERING,
        ADVERTISING,
        REQUESTING,
        CONNECTED,
        REJECTED,
        DISCONNECTED,
        SUSPENDED,
        TRANSFERRING,
        ERROR
    }

    private final GoogleApiClient mGoogleApiClient;
    private final String mDeviceName;
    private NearbyStatus mStatus;

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
            onNearbyConnectionEndpointFound(endpointId, discoveredEndpointInfo.getEndpointName());
            sendConnectionRequest(endpointId);
        }

        @Override
        public void onEndpointLost(String endpointId) {
            onNearbyConnectionEndpointLost(endpointId);
            startDiscovering();
        }
    };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            mStatus = NearbyStatus.REQUESTING;
            Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution connectionResolution) {
            if (connectionResolution.getStatus().isSuccess()) {
                mStatus = NearbyStatus.CONNECTED;
                onNearbyConnectionEndpointConnected(endpointId);
            } else {
                mStatus = NearbyStatus.REJECTED;
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            mStatus = NearbyStatus.DISCONNECTED;
            onNearbyConnectionEndpointDisconnected(endpointId);
        }
    };

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            onNearbyConnectionMessageReceived(payload.asBytes());
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
            switch (payloadTransferUpdate.getStatus()) {
                case PayloadTransferUpdate.Status.IN_PROGRESS:
                    mStatus = NearbyStatus.TRANSFERRING;
                    break;
                case PayloadTransferUpdate.Status.SUCCESS:
                    mStatus = NearbyStatus.CONNECTED;
                    break;
                case PayloadTransferUpdate.Status.FAILURE:
                    onNearbyConnectionTransferError(endpointId);
                    break;
                default:
                    break;
            }
        }
    };

    public BaseNearby(Context context, String deviceName) {

        mDeviceName = deviceName;
        mStatus = NearbyStatus.CONNECTING;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        mGoogleApiClient.connect();
    }

    public NearbyStatus getStatus() {
        return mStatus;
    }

    protected void disconnectNearbyConnection() {
        Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startDiscovering() {
        Nearby.Connections
                .startDiscovery(mGoogleApiClient, NEARBY_SERVICE_ID, mEndpointDiscoveryCallback,
                        new DiscoveryOptions(Strategy.P2P_CLUSTER))
                .setResultCallback((status) -> {
                    if (status.isSuccess()) {
                        mStatus = NearbyStatus.DISCOVERING;
                        onNearbyConnectionDiscoveringSuccess();
                    } else {
                        mStatus = NearbyStatus.ERROR;
                        onNearbyConnectionError(status.getStatusCode());
                    }
                });
    }

    protected void stopDiscovery() {
        Nearby.Connections.stopDiscovery(mGoogleApiClient);
    }

    protected void startAdvertising() {
        Nearby.Connections
                .startAdvertising(mGoogleApiClient, mDeviceName, NEARBY_SERVICE_ID, mConnectionLifecycleCallback,
                        new AdvertisingOptions(Strategy.P2P_CLUSTER))
                .setResultCallback((result) -> {
                    if (result.getStatus().isSuccess()) {
                        mStatus = NearbyStatus.ADVERTISING;
                        onNearbyConnectionAdvertisingSuccess();
                    } else {
                        mStatus = NearbyStatus.ERROR;
                        onNearbyConnectionError(result.getStatus().getStatusCode());
                    }
                });
    }

    protected void sendConnectionRequest(String remoteEndpointId) {
        mStatus = NearbyStatus.REQUESTING;
        Nearby.Connections
                .requestConnection(mGoogleApiClient, mDeviceName, remoteEndpointId, mConnectionLifecycleCallback)
                .setResultCallback((status) -> {
                    if (status.isSuccess()) {
                        onNearbyConnectionEndpointConnectionRequest(remoteEndpointId);
                    } else {
                        mStatus = NearbyStatus.ERROR;
                        onNearbyConnectionError(status.getStatusCode());
                    }
                });
    }

    protected void sendPayload(String endpointId, byte[] data) {
        Nearby.Connections
                .sendPayload(mGoogleApiClient, endpointId, Payload.fromBytes(data))
                .setResultCallback(status -> {
                    if (!status.isSuccess())
                        onNearbyConnectionTransferError(endpointId);
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onNearbyConnectionConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mStatus = NearbyStatus.SUSPENDED;
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mStatus = NearbyStatus.ERROR;
        onNearbyConnectionFailed(connectionResult.getErrorMessage());
    }

    public abstract void onNearbyConnectionConnected();
    public abstract void onNearbyConnectionFailed(String errorMessage);
    public abstract void onNearbyConnectionError(int status);
    public abstract void onNearbyConnectionDiscoveringSuccess();
    public abstract void onNearbyConnectionAdvertisingSuccess();
    public abstract void onNearbyConnectionEndpointFound(String endpointId, String name);
    public abstract void onNearbyConnectionEndpointLost(String endpointId);
    public abstract void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId);
    public abstract void onNearbyConnectionMessageReceived(byte[] bytes);
    public abstract void onNearbyConnectionTransferError(String endpointId);
    public abstract void onNearbyConnectionEndpointConnected(String remoteEndpointId);
    public abstract void onNearbyConnectionEndpointDisconnected(String remoteEndpointId);
}
