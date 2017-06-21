package com.rosterloh.thingsclient.bluetooth;

import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import javax.inject.Inject;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class BleController {

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    LocationManager locationManager;
    boolean locationPermission = false;

    private final Runnable stopScanRunnable = this::stopLeScan;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Inject
    public BleController(Context context) {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationPermission = isLocationPermissionGranted(context);
    }

    public void scanBleDevices() {
        if (bluetoothAdapter == null) {
            throw new RuntimeException("Bluetooth not available");
        } else if (!bluetoothAdapter.isEnabled()) {
            throw new RuntimeException("Bluetooth is not enabled");
        } else if (!locationPermission) {
            throw new RuntimeException("Location permission required");
        } else if (!isLocationProviderEnabled()) {
            throw new RuntimeException("Location provider is not enabled");
        } else {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(1000)
                    .build();
            List<ScanFilter> filters = new ArrayList<>();
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        }
    }

    private void stopLeScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    private boolean isLocationPermissionGranted(Context context) {
        return (ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isLocationProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
