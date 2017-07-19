package com.rosterloh.andriot.nearby;

import android.content.Context;

import com.rosterloh.andriot.common.nearby.BaseNearby;
import com.rosterloh.andriot.common.nearby.LocationMessage;
import com.rosterloh.andriot.common.nearby.MessagePayload;
import com.rosterloh.andriot.db.LocalSettings;
import com.rosterloh.andriot.db.SettingsRepository;

import javax.inject.Inject;

import timber.log.Timber;

public class ConnectionsServer extends BaseNearby {

    private static final String DEVICE_NAME = "AndrIoT";
    private final SettingsRepository mSettingsRepo;
    private LocalSettings mSettings;
    private String mEndpointId;

    @Inject
    public ConnectionsServer(Context context, SettingsRepository settings) {
        super(context, DEVICE_NAME);
        mSettingsRepo = settings;
        mSettings = mSettingsRepo.getLocalSettings().getValue();
    }

    @Override
    public void onNearbyConnectionConnected() {
        startAdvertising();
    }

    @Override
    public void onNearbyConnectionFailed(String message) {
        Timber.e("Nearby Connection failed: " + message);
    }

    @Override
    public void onNearbyConnectionError(int status) {
        Timber.e("Error code: " + status);
    }

    @Override
    public void onNearbyConnectionDiscoveringSuccess() {

    }

    @Override
    public void onNearbyConnectionAdvertisingSuccess() {
        Timber.d("Nearby connections advertising");
    }

    @Override
    public void onNearbyConnectionEndpointFound(String endpointId, String name) {

    }

    @Override
    public void onNearbyConnectionEndpointLost(String endpointId) {

    }

    @Override
    public void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId) {
        Timber.d("Connection requested from " + remoteEndpointId);
    }

    @Override
    public void onNearbyConnectionMessageReceived(byte[] bytes) {
        parsePayload(bytes);
    }

    @Override
    public void onNearbyConnectionTransferError(String endpointId) {
        Timber.e("Message transfer error with " + endpointId);
    }

    @Override
    public void onNearbyConnectionEndpointConnected(String remoteEndpointId) {
        mEndpointId = remoteEndpointId;
        Timber.d("Connected to " + remoteEndpointId);
        if (mSettings != null) {
            MessagePayload info = new MessagePayload(mSettings.getDeviceId(), mSettings.getIpAddress());
            sendPayload(mEndpointId, MessagePayload.marshall(info));
        }
    }

    @Override
    public void onNearbyConnectionEndpointDisconnected(String remoteEndpointId) {
        mEndpointId = null;
        Timber.d("Disconnected from " + remoteEndpointId);
    }

    private void parsePayload(byte[] bytes) {
        MessagePayload msg = MessagePayload.unmarshall(bytes, MessagePayload.CREATOR);

        switch (msg.getType()) {
            case MessagePayload.PAYLOAD_TYPE_DEVICE_INFO:
                break;
            case MessagePayload.PAYLOAD_TYPE_LOCATION:
                LocationMessage data = (LocationMessage) msg.getData();
                Timber.d("New location received: " + data.getLatitude() + ',' + data.getLongitude());
                mSettingsRepo.updateLocationSettings(data.getLatitude(), data.getLongitude());
                break;
            default:
                Timber.w("Unknown message type " + msg.getType() + " received");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        disconnectNearbyConnection();
        super.finalize();
    }
}
