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
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.MyGLSurfaceView.SurfaceCallbacks
import com.dergoogler.liquidwars.MyRenderer
import com.dergoogler.liquidwars.NativeInterface
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.Util.loadPlayerInitialPositions
import com.dergoogler.liquidwars.Util.makeDialogCancelableIn
import com.dergoogler.liquidwars.Util.makeDialogDismissIn
import com.dergoogler.liquidwars.Util.regulateSpeed
import com.dergoogler.liquidwars.Util.teamToColour
import com.dergoogler.liquidwars.Util.teamToNameString

class GameActivity : LiquidCompatActivity(), Runnable, SurfaceCallbacks {
    private var myGLSurfaceView: MyGLSurfaceView? = null
    private var running = false
    private var paused = false
    private val xs = Array(6) { ShortArray(5) }
    private val ys = Array(6) { ShortArray(5) }
    private var context: Context? = null
    private var gameFinished = false
    private var lostGame = false
    private var gameStep = 0
    private var frozen = false
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.hideSystemUI()
        this.keepOn()
        context = this

        setContentView(R.layout.game)
        myGLSurfaceView = findViewById(R.id.mySurfaceView)
        myGLSurfaceView?.setSurfaceCallbacks(this)

        loadPlayerInitialPositions(xs, ys)

        NativeInterface.init(assets)
        NativeInterface.createGame(
            StaticBits.team,
            StaticBits.map,
            StaticBits.seed,
            StaticBits.dotsPerTeam
        )

        myGLSurfaceView?.requestPointerCapture()

        Thread(this).start()
    }

    override fun onPause() {
        super.onPause()
        paused = true
        if (myGLSurfaceView != null) {
            myGLSurfaceView!!.releasePointerCapture()
            myGLSurfaceView!!.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        paused = false
        if (myGLSurfaceView != null) {
            myGLSurfaceView!!.requestPointerCapture()
            myGLSurfaceView!!.onResume()
        }
    }

    override fun onDestroy() {
        myGLSurfaceView!!.releasePointerCapture()
        if (!isFinishing) finish()
        super.onDestroy()
        running = false
        if (dialog != null) dialog!!.dismiss()
    }

    override fun run() {
        running = true
        gameFinished = false
        lostGame = false
        gameStep = 0
        frozen = false
        var aiStartDelay = 6
        StaticBits.startTimestamp = System.currentTimeMillis()
        while (running) {
            if (!paused && !frozen) {
                stepGame()
                val timeDiff =
                    (System.currentTimeMillis() - StaticBits.startTimestamp).toInt() / 1000
                if (!gameFinished) NativeInterface.setTimeSidebar(timeDiff.toFloat() / StaticBits.timeLimit.toFloat())
                checkTimeout(timeDiff)
                checkForWinner()
                checkIfLost()
                if (aiStartDelay-- < 0) updateAI()
            } else {
                try {
                    Thread.sleep(100)
                } catch (ie: InterruptedException) {
                }
            }
        }
        NativeInterface.destroyGame()
        NativeInterface.uninit()
    }

    private fun checkTimeout(timeDiff: Int) {
        if ((timeDiff >= StaticBits.timeLimit) && !gameFinished) {
            runOnUiThread {
                frozen = true
                gameFinished = true
                if (dialog != null) dialog!!.dismiss()
                val builder = AlertDialog.Builder(context)
                var winningTeam = 0
                for (i in 0..5) {
                    val score = NativeInterface.teamScore(winningTeam)
                    val temp = NativeInterface.teamScore(i)
                    if (temp > score) winningTeam = i
                }
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

    private fun updateAI() {
        for (p in 0..5) {
            if (p != StaticBits.team) {
                val nearestXY = NativeInterface.getNearestDot(p, xs[p][0], ys[p][0])
                val nearestX = (nearestXY ushr 16).toShort()
                val nearestY = (nearestXY and 0x0000FFFF).toShort()
                xs[p][0] = nearestX
                ys[p][0] = nearestY
            }
        }
    }

    private fun stepGame() {
        setPlayerPositions()
        for (i in 0..9) {
            val previousTime = System.nanoTime()
            NativeInterface.stepDots()
            regulateSpeed(previousTime, StaticBits.GAME_SPEED)
        }
        gameStep++
    }

    private fun setPlayerPositions() {
        for (i in 0..5) NativeInterface.setPlayerPosition(i, xs[i], ys[i])
    }

    override fun onTouch(event: MotionEvent) {
        val count = event.pointerCount
        for (i in 0..4) {
            if (i < count) {
                xs[StaticBits.team][i] =
                    ((event.getX(i) / MyRenderer.displayWidth.toFloat()) * MyRenderer.WIDTH.toFloat()).toInt()
                        .toShort()
                ys[StaticBits.team][i] =
                    ((MyRenderer.HEIGHT - 1) - ((event.getY(i) / MyRenderer.displayHeight.toFloat()) * MyRenderer.HEIGHT.toFloat())).toInt()
                        .toShort()
            } else {
                xs[StaticBits.team][i] = -1
                ys[StaticBits.team][i] = -1
            }
        }

        if (event.action == MotionEvent.ACTION_POINTER_UP) {
            val upIndex = event.actionIndex
            val upId = event.getPointerId(upIndex)
            xs[StaticBits.team][upId] = -1
            ys[StaticBits.team][upId] = -1
        }
    }

    override fun onHover(v: View, event: MotionEvent) {
        onTouch(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished && !lostGame) {
            val clicker = DialogInterface.OnClickListener { dialog, which -> finish() }
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        paused = !hasFocus
    }
}
