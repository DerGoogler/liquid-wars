

package com.dergoogler.liquidwars.server;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.content.Context;

public class NetInfo {
    public static String getIPAddress(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int i = wm.getConnectionInfo().getIpAddress();
        return format(i);
    }

    public static String getNetMask(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int i = wm.getDhcpInfo().netmask;
        return format(i);
    }

    public static String getBroadcastAddress(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int ip = wm.getConnectionInfo().getIpAddress();
        int nm = wm.getDhcpInfo().netmask;
        return format(ip | (~nm));
    }

    public static String getSSID(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.getConnectionInfo().getSSID();
    }

    @SuppressLint("DefaultLocale")
    private static String format(int i) {
        return String.format("%d.%d.%d.%d",
            (i & 0xFF),
            ((i >> 8) & 0xFF),
            ((i >> 16) & 0xFF),
            ((i >> 24) & 0xFF));
    }
}
