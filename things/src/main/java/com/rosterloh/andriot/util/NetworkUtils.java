package com.rosterloh.andriot.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public final class NetworkUtils {

    public enum NetworkType {
        NETWORK_WIFI,
        NETWORK_ETHERNET,
        NETWORK_UNKNOWN,
        NETWORK_NO
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    public static boolean getWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    @RequiresPermission("android.permission.CHANGE_WIFI_STATE")
    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        }
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    public String getWifiSSid(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo conInfo = wifiManager.getConnectionInfo();
        return conInfo.getSSID();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static NetworkType getNetworkType(Context context) {
        NetworkType netType = NetworkType.NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                netType = NetworkType.NETWORK_ETHERNET;
            } else {
                netType = NetworkType.NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();) {
                NetworkInterface ni = nis.nextElement();
                if (!ni.isUp()) continue;
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements();) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase()
                                        : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, InetAddress> getIpAddresses(Context context) {

        Map<String, InetAddress> ips = new HashMap<>();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                        break;
                }
            }
        }

        return ips;
    }

}
