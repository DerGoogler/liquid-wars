package com.dergoogler.liquidwars.server

import android.content.Context
import com.dergoogler.liquidwars.server.SubServer.SubServerCallbacks
import java.io.IOException
import java.net.ServerSocket

class Server(private val context: Context, private val port: Int) : SubServerCallbacks, Runnable {
    var serverCallbacks: ServerCallbacks?
    private var serverSocket: ServerSocket? = null
    private val subServers: ArrayList<SubServer>
    private var accepting = false

    init {
        this.serverCallbacks = context as ServerCallbacks
        subServers = ArrayList()
    }

    fun startAccepting() {
        if (!accepting) Thread(this).start()
    }

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            var id = 1
            accepting = true
            while (accepting) {
                val socket = serverSocket!!.accept()
                if (subServers.size < MAX_CLIENTS) {
                    val subServer = SubServer(this, socket, id)
                    subServer.start()
                    subServers.add(subServer)
                    if (serverCallbacks != null) serverCallbacks!!.onClientConnected(id)
                    id++
                } else {
                    socket.close()
                }
            }
        } catch (ignored: IOException) {
        }
        serverSocket = null
        accepting = false
    }

    override fun onSubServerMessageReceived(id: Int, argc: Int, args: IntArray?) {
        if (serverCallbacks != null) serverCallbacks!!.onClientMessageReceived(id, argc, args)
    }

    override fun onSubServerDisconnect(id: Int) {
        synchronized(subServers) {
            for (subServer in subServers) {
                if (subServer.id == id) {
                    subServers.remove(subServer)
                    break
                }
            }
        }
        if (serverCallbacks != null) serverCallbacks!!.onClientDisconnected(id)
    }

    fun stopAccepting() {
        accepting = false
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
            }
        }
    }

    fun sendToOne(id: Int, argc: Int, args: IntArray?) {
        for (subServer in subServers) {
            if (subServer.id == id) {
                subServer.send(argc, args)
                break
            }
        }
    }

    fun sendToOne(id: Int, arg1: Int, arg2: Int) {
        val args = intArrayOf(arg1, arg2)
        sendToOne(id, 2, args)
    }

    fun sendToAll(argc: Int, args: IntArray?) {
        for (subServer in subServers) subServer.send(argc, args)
    }

    fun sendToAll(arg1: Int, arg2: Int) {
        val args = intArrayOf(arg1, arg2)
        sendToAll(2, args)
    }

    fun sendToAll(arg1: Int, arg2: Int, arg3: Int) {
        val args = intArrayOf(arg1, arg2, arg3)
        sendToAll(3, args)
    }

    fun destroy() {
        stopAccepting()
        synchronized(subServers) {
            for (s in subServers) s.destroy()
        }
    }

    fun setCallbacks(sc: ServerCallbacks?) {
        serverCallbacks = sc
    }

    interface ServerCallbacks {
        fun onClientMessageReceived(id: Int, argc: Int, args: IntArray?)
        fun onClientConnected(id: Int)
        fun onClientDisconnected(id: Int)
    }

    companion object {
        private const val MAX_CLIENTS = 5
        var SET_MAP_IMAGE_COMMAND: Int = 0x56
        var TEAM_LIST_COMMAND: Int = 0x57
        var REQUEST_CHANGE_TEAM_COMMAND: Int = 0x58
        var START_GAME_COMMAND: Int = 0x59
    }
}
