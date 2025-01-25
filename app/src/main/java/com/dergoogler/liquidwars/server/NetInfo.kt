package com.dergoogler.liquidwars.server

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager

object NetInfo {
    fun getIPAddress(context: Context): String {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val i = wm.connectionInfo.ipAddress
        return format(i)
    }

    fun getNetMask(context: Context): String {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val i = wm.dhcpInfo.netmask
        return format(i)
    }

    fun getBroadcastAddress(context: Context): String {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wm.connectionInfo.ipAddress
        val nm = wm.dhcpInfo.netmask
        return format(ip or (nm.inv()))
    }

    fun getSSID(context: Context): String {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wm.connectionInfo.ssid
    }

    @SuppressLint("DefaultLocale")
    private fun format(i: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            (i and 0xFF),
            ((i shr 8) and 0xFF),
            ((i shr 16) and 0xFF),
            ((i shr 24) and 0xFF)
        )
    }
}
