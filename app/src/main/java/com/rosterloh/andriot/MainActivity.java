package com.rosterloh.andriot;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import com.rosterloh.andriot.databinding.ActivityMainBinding;

import java.net.Inet4Address;
import java.net.InetAddress;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Info info = new Info();
        binding.setInfo(info);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        String eth = "";
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
                                    info.setEthIp(address);
                                } else {
                                    info.setWifiIp(address);
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

        if (info.getWifiConnected()) {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo conInfo = wm.getConnectionInfo();
            //info.setWifiIp(Formatter.formatIpAddress(conInfo.getIpAddress()));
            info.setWifiName(conInfo.getSSID());
        }
    }

}
