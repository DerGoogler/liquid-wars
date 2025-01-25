package com.dergoogler.liquidwars.server

import android.content.Context
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object ServerFinder {
    private var datagramSocket: DatagramSocket? = null
    private const val queryMessage = "9287349213846"
    private const val responseMessage = "2138479287348"
    private var isSearching = false

    fun share(context: Context, port: Int, serverName: String) {
        if (datagramSocket != null) return
        object : Thread() {
            override fun run() {
                try {
                    datagramSocket = DatagramSocket(port)
                    val receiveData = ByteArray(32)
                    val sendData = (responseMessage + serverName).toByteArray()
                    val broadcastAddress = NetInfo.getBroadcastAddress(context)
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    while (true) {
                        datagramSocket!!.receive(receivePacket)
                        val s = String(receivePacket.data, 0, receivePacket.length)
                        if (s.compareTo(queryMessage) == 0) {
                            val inIP = receivePacket.address
                            val sendPacket =
                                DatagramPacket(sendData, sendData.size, inIP, receivePacket.port)
                            datagramSocket!!.send(sendPacket)
                        }
                    }
                } catch (ignored: IOException) {
                }
            }
        }.start()
    }

    fun stopSharing() {
        if (datagramSocket != null) datagramSocket!!.close()
        datagramSocket = null
    }

    fun search(sfc: ServerFinderCallbacks, ip: String?, port: Int) {
        if (isSearching) return
        object : Thread() {
            override fun run() {
                isSearching = true
                try {
                    datagramSocket = DatagramSocket(port)
                    datagramSocket!!.soTimeout = 1000
                    val receiveData = ByteArray(128)
                    val sendData = queryMessage.toByteArray()
                    val sendPacket =
                        DatagramPacket(sendData, sendData.size, InetAddress.getByName(ip), port)
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    while (isSearching) {
                        datagramSocket!!.send(sendPacket)
                        while (true) {
                            try {
                                datagramSocket!!.receive(receivePacket)
                            } catch (i: IOException) {
                                break
                            }
                            val data = receivePacket.data
                            val l = responseMessage.toByteArray().size
                            val s = String(data, 0, l)
                            if (s.compareTo(responseMessage) == 0) {
                                val name = String(data, l, receivePacket.length - l)
                                val address = receivePacket.address.hostAddress
                                sfc.onServerFound(ServerInfo(name, address))
                            }
                        }
                    }
                } catch (ignored: IOException) {
                }
                stopSearching()
            }
        }.start()
    }

    fun stopSearching() {
        if (datagramSocket != null) datagramSocket!!.close()
        datagramSocket = null
        isSearching = false
    }

    class ServerInfo(var name: String, var ip: String)

    interface ServerFinderCallbacks {
        fun onServerFound(serverInfo: ServerInfo?)
    }
}
