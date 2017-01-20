package com.rosterloh.andriot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class ConnectionDetector {

    private static final String TAG = ConnectionDetector.class.getSimpleName();
    private Context context;

    public ConnectionDetector(Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailableAndConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public Map<String, InetAddress> getIpAddresses() {

        Map<String, InetAddress> ips = new HashMap<>();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();

        for (Network network : networks) {
            NetworkInfo netInfo = cm.getNetworkInfo(network);
            //Log.i(TAG, netInfo.getTypeName() + " is " + netInfo.getState() + ":" + netInfo.getDetailedState());
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {

                switch (netInfo.getType()) {
                    case ConnectivityManager.TYPE_ETHERNET:
                    case ConnectivityManager.TYPE_WIFI:
                        LinkProperties lp = cm.getLinkProperties(network);
                        for (LinkAddress link : lp.getLinkAddresses()) {
                            InetAddress address = link.getAddress();
                            if (address instanceof Inet4Address) {
                                if (lp.getInterfaceName().equalsIgnoreCase("eth0")) {
                                    ips.put("eth0", address);
                                } else {
                                    ips.put("wlan0", address);
                                }
                            }
                        }
                        break;
                    default:
                        Log.w(TAG, "Unhandled interface type " + netInfo.getType() + " " + cm.getLinkProperties(network).getInterfaceName());
                        break;
                }
            }
        }

        return ips;
    }

    public String getWifiSSid() {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo conInfo = wm.getConnectionInfo();
        return conInfo.getSSID();
    }

}
