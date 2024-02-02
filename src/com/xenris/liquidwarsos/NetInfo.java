//    This file is part of Liquid Wars.
//
//    Liquid Wars is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquid Wars is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Liquid Wars.  If not, see <http://www.gnu.org/licenses/>.

package com.xenris.liquidwarsos;

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

    private static String format(int i) {
        return String.format("%d.%d.%d.%d",
            (i & 0xFF),
            ((i >> 8) & 0xFF),
            ((i >> 16) & 0xFF),
            ((i >> 24) & 0xFF));
    }
}
