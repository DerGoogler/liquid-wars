package com.dergoogler.liquidwars

import android.app.Dialog
import android.content.Context
import android.os.Handler

object Util {
    fun loadPlayerInitialPositions(xs: Array<ShortArray>, ys: Array<ShortArray>) {
        for (p in 0..5) for (i in 0..4) {
            ys[p][i] = -1
            xs[p][i] = ys[p][i]
        }
        val ydis = 50
        val xdis = 20
        xs[0][0] = xdis.toShort()
        ys[0][0] = ydis.toShort()
        xs[1][0] = xdis.toShort()
        ys[1][0] = (MyRenderer.HEIGHT - ydis).toShort()
        xs[2][0] = (MyRenderer.WIDTH / 2).toShort()
        ys[2][0] = ydis.toShort()
        xs[3][0] = (MyRenderer.WIDTH / 2).toShort()
        ys[3][0] = (MyRenderer.HEIGHT - ydis).toShort()
        xs[4][0] = (MyRenderer.WIDTH - xdis).toShort()
        ys[4][0] = ydis.toShort()
        xs[5][0] = (MyRenderer.WIDTH - xdis).toShort()
        ys[5][0] = (MyRenderer.HEIGHT - ydis).toShort()
    }

    fun teamToColour(p: Int): Int {
        when (p) {
            0 -> return -0xff0100
            1 -> return -0xdfdf01
            2 -> return -0x10000
            3 -> return -0xff0001
            4 -> return -0x100
            5 -> return -0xff01
        }
        return -0x1
    }

    fun teamToNameString(p: Int): String {
        when (p) {
            0 -> return "Green"
            1 -> return "Blue"
            2 -> return "Red"
            3 -> return "Cyan"
            4 -> return "Yellow"
            5 -> return "Magenta"
        }
        return "Unknown"
    }

    fun getMapName(c: Context, m: Int): String {
        val maps = c.resources.getStringArray(R.array.maps_array)
        return maps[m]
    }

    fun getTimeoutString(c: Context, t: Int): String {
        val timeout = c.resources.getStringArray(R.array.timeout_array)
        return timeout[t]
    }

    fun clientIdToPlayerNumber(id: Int): Int {
        for (i in 0..5) if (StaticBits.teams[i] == id) return i
        return -1
    }

    fun intToTime(i: Int): Int {
        when (i) {
            0 -> return 30
            1 -> return 60
            2 -> return 60 * 2
            3 -> return 60 * 3
            4 -> return 60 * 5
            5 -> return 60 * 10
            6 -> return 60 * 60 * 24 * 23
        }
        return 60 * 3
    }

    fun regulateSpeed(previousTime: Long, totalDelay: Int) {
        val currentTime = System.nanoTime()
        val timeDiff = currentTime - previousTime

        var nanoDelay = (totalDelay * 1000) - timeDiff
        var milliDelay = nanoDelay / 1000000

        nanoDelay -= (milliDelay * 1000000)
        if (nanoDelay < 0) nanoDelay = 0
        if (milliDelay < 0) milliDelay = 0

        try {
            Thread.sleep(milliDelay, nanoDelay.toInt())
        } catch (ie: InterruptedException) {
        }
    }

    fun makeDialogCancelableIn(dialog: Dialog, millis: Int) {
        val handler = Handler()
        handler.postDelayed({ dialog.setCanceledOnTouchOutside(true) }, millis.toLong())
    }

    fun makeDialogDismissIn(dialog: Dialog, millis: Int) {
        val handler = Handler()
        handler.postDelayed({ dialog.dismiss() }, millis.toLong())
    }
}
