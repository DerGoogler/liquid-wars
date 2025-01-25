package com.dergoogler.liquidwars.server

import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer

class SubServer(
    private val subServerCallbacks: SubServerCallbacks,
    private var socket: Socket?,
    var id: Int
) : Thread() {
    private var sendLocked = true

    override fun run() {
        try {
            socket!!.getOutputStream().write(id)
            sendLocked = false
            val buffer = ByteArray(512)
            val intBuffer = IntArray(128)
            val inIB = ByteBuffer.wrap(buffer).asIntBuffer()
            val `is` = socket!!.getInputStream()
            while (true) {
                val count = `is`.read()
                if (count == -1) break
                `is`.read(buffer, 0, count)
                inIB[intBuffer]
                inIB.rewind()
                subServerCallbacks.onSubServerMessageReceived(id, count, intBuffer)
            }
        } catch (e: IOException) {
        }
        subServerCallbacks.onSubServerDisconnect(id)
    }

    override fun destroy() {
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
            }
            socket = null
        }
    }

    fun send(argc: Int, args: IntArray?) {
        while (sendLocked) {
            try {
                sleep(10)
            } catch (ie: InterruptedException) {
            }
        }
        sendLocked = true
        try {
            val bb = ByteBuffer.allocate(argc * 4)
            val ib = bb.asIntBuffer()
            ib.put(args, 0, argc)
            val os = socket!!.getOutputStream()
            os.write(argc * 4)
            os.write(bb.array())
            os.flush()
        } catch (e: IOException) {
        }
        sendLocked = false
    }

    interface SubServerCallbacks {
        fun onSubServerMessageReceived(id: Int, argc: Int, args: IntArray?)
        fun onSubServerDisconnect(id: Int)
    }
}
