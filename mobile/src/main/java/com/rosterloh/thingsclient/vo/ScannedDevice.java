package com.rosterloh.thingsclient.vo;

import android.bluetooth.BluetoothDevice;

import com.google.gson.annotations.SerializedName;

public class ScannedDevice {
    @SerializedName("device")
    public final BluetoothDevice btDevice;
    @SerializedName("rssi")
    public int rssi;

    public ScannedDevice(BluetoothDevice btDevice, int rssi) {
        this.btDevice= btDevice;
        this.rssi= rssi;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) ||
                ((obj instanceof ScannedDevice) && btDevice.equals(((ScannedDevice) obj).btDevice));
    }
}
