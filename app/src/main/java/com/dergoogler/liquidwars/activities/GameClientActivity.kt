package com.dergoogler.liquidwars.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.dergoogler.liquidwars.Client.ClientCallbacks
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.MyGLSurfaceView.SurfaceCallbacks
import com.dergoogler.liquidwars.MyRenderer
import com.dergoogler.liquidwars.NativeInterface
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.Util.makeDialogCancelableIn
import com.dergoogler.liquidwars.Util.makeDialogDismissIn
import com.dergoogler.liquidwars.Util.regulateSpeed
import com.dergoogler.liquidwars.Util.teamToColour
import com.dergoogler.liquidwars.Util.teamToNameString

class GameClientActivity : LiquidCompatActivity(), ClientCallbacks, Runnable, SurfaceCallbacks {
    private var myGLSurfaceView: MyGLSurfaceView? = null
    private var running = false
    private var gameStep = -1
    private val xs = ShortArray(5)
    private val ys = ShortArray(5)
    private var touchReduction = 5
    private var context: Context? = null
    private var usingNativeStateLock = false
    private var gameFinished = false
    private var lostGame = false
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.hideSystemUI()
        this.keepOn()
        context = this


        setContentView(R.layout.game)
        myGLSurfaceView = findViewById(R.id.mySurfaceView)
        myGLSurfaceView?.setSurfaceCallbacks(this)

        NativeInterface.init(assets)
        NativeInterface.createGame(
            StaticBits.team,
            StaticBits.map,
            StaticBits.seed,
            StaticBits.dotsPerTeam
        )

        StaticBits.client!!.setCallbacks(this)

        Thread(this).start()
    }

    override fun onPause() {
        super.onPause()
        if (myGLSurfaceView != null) myGLSurfaceView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (myGLSurfaceView != null) myGLSurfaceView!!.onResume()
    }

    override fun onDestroy() {
        if (!isFinishing) finish()
        super.onDestroy()
        if (StaticBits.client != null) StaticBits.client!!.setCallbacks(StaticBits.clientGameSetupActivity)
        if (myGLSurfaceView != null) myGLSurfaceView!!.onPause()
        running = false
        if (dialog != null) dialog!!.dismiss()
    }

    override fun run() {
        running = true
        gameFinished = false
        lostGame = false
        StaticBits.client!!.send(StaticBits.CLIENT_READY, 0)
        while (running) {
            StaticBits.client!!.send(StaticBits.CLIENT_CURRENT_GAMESTEP, gameStep)
            checkForWinner()
            checkIfLost()
            try {
                Thread.sleep(50)
            } catch (ie: InterruptedException) {
            }
        }
        while (usingNativeStateLock) try {
            Thread.sleep(3)
        } catch (ie: InterruptedException) {
        }
        NativeInterface.destroyGame()
        NativeInterface.uninit()
        StaticBits.client!!.send(StaticBits.CLIENT_EXIT, 0)
        finish()
    }

    private fun checkIfLost() {
        if (lostGame) return
        if (NativeInterface.teamScore(StaticBits.team) == 0) {
            runOnUiThread {
                lostGame = true
                if (dialog != null) dialog!!.dismiss()
                val builder = AlertDialog.Builder(context)
                builder.setMessage("You Lose")
                dialog = builder.show()
                val messageText = dialog?.findViewById<View>(android.R.id.message) as TextView
                messageText.gravity = Gravity.CENTER
                messageText.setTextColor(teamToColour(StaticBits.team))
                dialog?.setCanceledOnTouchOutside(false)
                makeDialogCancelableIn(dialog!!, 1500)
                makeDialogDismissIn(dialog!!, 5000)
            }
        }
    }

    private fun checkForWinner() {
        if (gameFinished) return
        for (i in 0..5) {
            if (NativeInterface.teamScore(i) == StaticBits.NUMBER_OF_TEAMS * StaticBits.dotsPerTeam) {
                val p = i
                runOnUiThread {
                    gameFinished = true
                    if (dialog != null) dialog!!.dismiss()
                    val builder = AlertDialog.Builder(context)
                    if (p == StaticBits.team) builder.setMessage("You Win!")
                    else builder.setMessage(teamToNameString(p) + " Wins")
                    dialog = builder.show()
                    val messageText = dialog?.findViewById<View>(android.R.id.message) as TextView
                    messageText.gravity = Gravity.CENTER
                    messageText.setTextColor(teamToColour(p))
                    dialog?.setCanceledOnTouchOutside(false)
                    makeDialogCancelableIn(dialog!!, 1500)
                    makeDialogDismissIn(dialog!!, 5000)
                }
                break
            }
        }
    }

    private fun sendPlayerData() {
        val data = IntArray(5 + 5 + 1)
        for (i in 0..4) {
            data[i + 1] = xs[i].toInt()
            data[i + 5 + 1] = ys[i].toInt()
        }
        data[0] = StaticBits.PLAYER_POSITION_DATA
        StaticBits.client!!.send(data.size, data)
    }

    override fun onTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_MOVE) if (touchReduction-- != 0) return
        else touchReduction = 5

        val count = event.pointerCount
        for (i in 0..4) {
            if (i < count) {
                xs[i] =
                    ((event.getX(i) / MyRenderer.displayWidth.toFloat()) * MyRenderer.WIDTH.toFloat()).toInt()
                        .toShort()
                ys[i] =
                    ((MyRenderer.HEIGHT - 1) - ((event.getY(i) / MyRenderer.displayHeight.toFloat()) * MyRenderer.HEIGHT.toFloat())).toInt()
                        .toShort()
            } else {
                xs[i] = -1
                ys[i] = -1
            }
        }

        if (event.action == MotionEvent.ACTION_POINTER_UP) { //XXX this appears to be not working.
            val upIndex = event.actionIndex
            val upId = event.getPointerId(upIndex)
            xs[upId] = -1
            ys[upId] = -1
        }
        sendPlayerData()
        //        android.util.Log.i("mylog", xs[1] + " " + ys[1]);
    }

    override fun onHover(v: View, event: MotionEvent) {
        onTouch(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished) {
            val clicker = DialogInterface.OnClickListener { dialog, which -> running = false }
            if (dialog != null) dialog!!.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Back to menu?")
            builder.setPositiveButton("Yes", clicker)
            builder.setNegativeButton("No", null)
            dialog = builder.show()
            val messageText = dialog?.findViewById<View>(android.R.id.message) as TextView
            messageText.gravity = Gravity.CENTER
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onServerMessageReceived(argc: Int, args: IntArray) {
        if (args[0] == StaticBits.STEP_GAME) {
            if (running) {
                if (args[2] == (gameStep + 1)) {
                    loadPlayerPositions(args, 3)
                    stepGame(args[1])
                } else if (args[2] > (gameStep + 1)) {
                    StaticBits.client!!.send(StaticBits.RESEND_STEPS, gameStep + 1)
                }
            }
        } else if (args[0] == StaticBits.CLIENT_READY_QUERY) {
            if (running) StaticBits.client!!.send(StaticBits.CLIENT_READY, 0)
        } else if (args[0] == StaticBits.KILL_GAME) {
            ToastOnUiThread("Game canceled")
            running = false
        } else if (args[0] == StaticBits.BACK_TO_MENU) {
            running = false
        } else if (args[0] == StaticBits.OUT_OF_TIME) {
            outOfTimeMessage(args[1])
        } else if (args[0] == StaticBits.TIME_DIFF) {
            NativeInterface.setTimeSidebar(args[1].toFloat() / StaticBits.timeLimit.toFloat())
        }
    }

    override fun onServerConnectionMade(id: Int, ip: String) {
    }

    override fun onServerConnectionFailed(ip: String) {
        StaticBits.gameWasDisconnected = true
        ToastOnUiThread("Connection lost")
        running = false
    }

    override fun onServerConnectionClosed(ip: String) {
        StaticBits.gameWasDisconnected = true
        ToastOnUiThread("Connection lost")
        running = false
    }

    private fun ToastOnUiThread(s: String) {
        runOnUiThread { Toast.makeText(context, s, Toast.LENGTH_SHORT).show() }
    }

    private fun outOfTimeMessage(winningTeam: Int) {
        runOnUiThread {
            gameFinished = true
            if (dialog != null) dialog!!.dismiss()
            val builder = AlertDialog.Builder(context)
            if (winningTeam == StaticBits.team) builder.setMessage("Out of time! You win!")
            else builder.setMessage("Out of time! " + teamToNameString(winningTeam) + " wins!")
            dialog = builder.show()
            val messageText = dialog?.findViewById<View>(android.R.id.message) as TextView
            messageText.setTextColor(teamToColour(winningTeam))
            messageText.gravity = Gravity.CENTER
            dialog?.setCanceledOnTouchOutside(false)
            makeDialogCancelableIn(dialog!!, 1500)
            makeDialogDismissIn(dialog!!, 5000)
        }
    }

    private fun loadPlayerPositions(data: IntArray, offset: Int) {
        var offset = offset
        val tempxs = ShortArray(5)
        val tempys = ShortArray(5)
        for (p in 0..5) {
            for (xy in 0..4) {
                tempxs[xy] = data[offset++].toShort()
                tempys[xy] = data[offset++].toShort()
            }
            NativeInterface.setPlayerPosition(p, tempxs, tempys)
        }
    }

    private fun stepGame(speed: Int) {
        if (usingNativeStateLock) return
        usingNativeStateLock = true
        for (i in 0..9) {
            val previousTime = System.nanoTime()
            NativeInterface.stepDots()
            if (speed == StaticBits.REGULATED_STEP) regulateSpeed(
                previousTime,
                StaticBits.GAME_SPEED
            )
        }
        usingNativeStateLock = false
        gameStep++
    }
}
