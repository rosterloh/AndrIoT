package com.rosterloh.thingsclient.nearby;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Parcelable;

import com.rosterloh.andriot.common.nearby.BaseNearby;
import com.rosterloh.andriot.common.nearby.DeviceInfoMessage;
import com.rosterloh.andriot.common.nearby.LocationMessage;
import com.rosterloh.andriot.common.nearby.MessagePayload;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class ConnectionsClient extends BaseNearby {

    private static final String DEVICE_NAME = "ThingsClient";
    private String mEndpointId;
    private MutableLiveData<NearbyStatus> mStatus;

    @Inject
    public ConnectionsClient(Context context) {
        super(context, DEVICE_NAME);
        mStatus = new MutableLiveData<>();
    }

    public LiveData<NearbyStatus> observeStatus() {
        return mStatus;
    }

    @Override
    public void onNearbyConnectionConnected() {
        startDiscovering();
    }

    @Override
    public void onNearbyConnectionFailed(String message) {
        Timber.e("Nearby Connection failed: " + message);
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionError(int status) {
        Timber.e("Error code: " + status);
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionDiscoveringSuccess() {
        Timber.d("Nearby connections discovering");
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionAdvertisingSuccess() {

    }

    @Override
    public void onNearbyConnectionEndpointFound(String endpointId, String name) {
        Timber.d("Endpoint found " + name + ":" + endpointId);
    }

    @Override
    public void onNearbyConnectionEndpointLost(String endpointId) {
        Timber.d("Endpoint " + endpointId + " lost");
    }

    @Override
    public void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId) {
        Timber.d("Connection requested from " + remoteEndpointId);
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionMessageReceived(byte[] bytes) {
        Timber.d("Message received");
        parsePayload(bytes);
    }

    @Override
    public void onNearbyConnectionTransferError(String endpointId) {
        Timber.e("Message transfer error with " + endpointId);
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionEndpointConnected(String remoteEndpointId) {
        stopDiscovery();
        mEndpointId = remoteEndpointId;
        Timber.d("Connected to " + remoteEndpointId);
        mStatus.setValue(getStatus());
    }

    @Override
    public void onNearbyConnectionEndpointDisconnected(String remoteEndpointId) {
        mEndpointId = null;
        Timber.d("Disconnected from " + remoteEndpointId);
        mStatus.setValue(getStatus());
    }

    private void parsePayload(byte[] bytes) {
        MessagePayload msg = MessagePayload.unmarshall(bytes, MessagePayload.CREATOR);

        switch (msg.getType()) {
            case MessagePayload.PAYLOAD_TYPE_DEVICE_INFO:
                DeviceInfoMessage info = (DeviceInfoMessage) msg.getData();
                Timber.d(info.toString());
                break;
            case MessagePayload.PAYLOAD_TYPE_LOCATION:
                LocationMessage data = (LocationMessage) msg.getData();
                Timber.d(data.toString());
                break;
        }
    }

    public void sendMessage(String message) {
        sendMessage(message.getBytes());
    }

    public void sendMessage(byte[] message) {
        if (mEndpointId != null) {
            sendPayload(mEndpointId, message);
        } else {
            Timber.w("Server not connected");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        disconnectNearbyConnection();
        super.finalize();
    }
}
