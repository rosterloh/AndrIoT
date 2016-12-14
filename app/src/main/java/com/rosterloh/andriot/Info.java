package com.rosterloh.andriot;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.net.InetAddress;

public class Info extends BaseObservable {

    private InetAddress ethIp;
    private boolean ethernetConnected = false;
    private InetAddress wifiIp;
    private String wifiName;
    private boolean wifiConnected = false;

    @Bindable
    public boolean getEthernetConnected() {
        return ethernetConnected;
    }

    @Bindable
    public String getEthernet() {
        return "Ethernet IP: " + ethIp;
    }

    public void setEthIp(InetAddress ip) {
        ethIp = ip;
        ethernetConnected = true;
        notifyPropertyChanged(BR.ethernet);
    }

    public void setWifiIp(InetAddress ip) {
        wifiIp = ip;
        wifiConnected = true;
        notifyPropertyChanged(BR.wifi);
    }

    public void setWifiName(String ssid) {
        wifiName = ssid;
        wifiConnected = true;
        notifyPropertyChanged(BR.wifi);
    }

    @Bindable
    public boolean getWifiConnected() {
        return wifiConnected;
    }

    @Bindable
    public String getWifi() {
        return "WiFi SSID: " + wifiName + " IP: " + wifiIp;
    }
}
