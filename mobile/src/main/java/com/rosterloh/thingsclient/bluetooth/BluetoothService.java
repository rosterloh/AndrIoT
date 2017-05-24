package com.rosterloh.thingsclient.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BluetoothService extends Service {

    private static final String TAG = BluetoothService.class.getSimpleName();

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    Handler handler;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCallback extraCallback;

    private final Runnable scanTimeout = () -> stopScanning();

    private final static long RSSI_UPDATE_FREQ = 2000;
    private final Runnable rssiUpdate = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null) {
                bluetoothGatt.readRemoteRssi();
                handler.postDelayed(this, RSSI_UPDATE_FREQ);
            }
        }
    };

    private final Runnable connectionTimeout = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null) {
                Log.d(TAG, "timeout called");
                bluetoothGatt.disconnect();
                bluetoothGatt.close();/*
                if (extraCallback != null) {
                    extraCallback.onTimeout();
                }*/
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            stopSelf();
            return;
        }

        handler = new Handler();
    }

    private void stopScanning() {/*
        Collection<BluetoothLEGatt> leGattsToClose;
        synchronized (discoveredDevices) {
            leGattsToClose = new ArrayList<>(discoveredDevices.size());
            for (BluetoothDeviceInfo devInfo : discoveredDevices.values()) {
                leGattsToClose.add((BluetoothLEGatt) devInfo.gattHandle);
            }
        }

        BluetoothLEGatt.cancelAll(leGattsToClose);*/
    }

    public void registerGattCallback(boolean requestRssiUpdates, BluetoothGattCallback callback) {
        if (requestRssiUpdates) {
            handler.post(rssiUpdate);
        } else {
            handler.removeCallbacks(rssiUpdate);
        }
        extraCallback = callback;
    }

    public void discoverGattServices() {
        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
        }
    }

    public boolean isGattConnected() {
        return bluetoothGatt != null &&
                bluetoothManager.getConnectionState(bluetoothGatt.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED;
    }

    public BluetoothGatt getConnectedGatt() {
        return bluetoothGatt;
    }
}
