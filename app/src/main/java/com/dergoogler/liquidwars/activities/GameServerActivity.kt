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
import com.dergoogler.liquidwars.PlayerHistory
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.Util.loadPlayerInitialPositions
import com.dergoogler.liquidwars.Util.makeDialogCancelableIn
import com.dergoogler.liquidwars.Util.makeDialogDismissIn
import com.dergoogler.liquidwars.Util.regulateSpeed
import com.dergoogler.liquidwars.Util.teamToColour
import com.dergoogler.liquidwars.Util.teamToNameString
import com.dergoogler.liquidwars.server.Server.ServerCallbacks

class GameServerActivity : LiquidCompatActivity(), ServerCallbacks, Runnable, SurfaceCallbacks {
    private var myGLSurfaceView: MyGLSurfaceView? = null
    private var running = false
    private var gameStep = 0
    private val clientLags = IntArray(6)
    private val playerHistory = PlayerHistory()
    private var playerWithMissedStepsId = -1
    private var playerWithMissedStepsStep = 0
    private val xs = Array(6) { ShortArray(5) }
    private val ys = Array(6) { ShortArray(5) }
    private val ready: BooleanArray? = BooleanArray(6)
    private var context: Context? = null
    private var gameFinished = false
    private var lostGame = false
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

        createReadyList()

        for (i in clientLags.indices) clientLags[i] = 0

        NativeInterface.init(assets)
        NativeInterface.createGame(
            StaticBits.team,
            StaticBits.map,
            StaticBits.seed,
            StaticBits.dotsPerTeam
        )

        StaticBits.server!!.setCallbacks(this)

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
        StaticBits.server!!.setCallbacks(StaticBits.multiplayerGameSetupActivity)
        running = false
        if (dialog != null) dialog!!.dismiss()
    }

    override fun run() {
        running = true
        gameFinished = false
        lostGame = false
        frozen = false
        waitForEveryoneToBeReady()
        StaticBits.startTimestamp = System.currentTimeMillis()
        var aiStartDelay = 6
        while (running) {
            playerHistory.savePlayerPositions(xs, ys)
            if (!frozen) {
                sendStepData()
                stepGame()
                val timeDiff =
                    (System.currentTimeMillis() - StaticBits.startTimestamp).toInt() / 1000
                if (!gameFinished) {
                    StaticBits.server!!.sendToAll(StaticBits.TIME_DIFF, timeDiff)
                    NativeInterface.setTimeSidebar(timeDiff.toFloat() / StaticBits.timeLimit.toFloat())
                }
                checkTimeout(timeDiff)
                checkForWinner()
                checkIfLost()
                if (aiStartDelay-- < 0) updateAI()
                resendAnyLostSteps()
            }
            waitForSlowClients()
        }
        NativeInterface.destroyGame()
        NativeInterface.uninit()
        finish()
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
                StaticBits.server!!.sendToAll(StaticBits.OUT_OF_TIME, winningTeam)
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
                    val messageText = dialog?.findViewById<TextView>(android.R.id.message)
                    messageText?.gravity = Gravity.CENTER
                    messageText?.setTextColor(teamToColour(p))
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
            if (StaticBits.teams[p] == StaticBits.AI_PLAYER) {
                val nearestXY = NativeInterface.getNearestDot(p, xs[p][0], ys[p][0])
                val nearestX = (nearestXY ushr 16).toShort()
                val nearestY = (nearestXY and 0x0000FFFF).toShort()
                xs[p][0] = nearestX
                ys[p][0] = nearestY
            }
        }
    }

    private fun waitForEveryoneToBeReady() {
        if ((StaticBits.server == null) || (ready == null)) return

        var countdown = 10
        while (true) {
            if (ready[0] && ready[1] && ready[2] && ready[3] && ready[4] && ready[5]) break
            if (countdown-- < 0) {
                StaticBits.server!!.sendToAll(StaticBits.CLIENT_READY_QUERY, 0)
                countdown = 10
            }
            try {
                Thread.sleep(10)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun createReadyList() {
        for (i in 0..5) {
            if ((StaticBits.teams[i] == StaticBits.AI_PLAYER) || (StaticBits.teams[i] == 0)) ready!![i] =
                true
            else ready!![i] = false
        }
    }

    private fun sendStepData() {
        val data = IntArray(3 + 6 * 5 * 2)
        data[0] = StaticBits.STEP_GAME
        data[1] = StaticBits.REGULATED_STEP
        data[2] = gameStep
        playerHistory.serialiseCurrentPlayerState(data, 3)
        StaticBits.server!!.sendToAll(data.size, data)
    }

    private fun resendAnyLostSteps() {
        val data = IntArray(3 + 6 * 5 * 2)
        if (playerWithMissedStepsId == -1) return
        data[0] = StaticBits.STEP_GAME
        data[1] = StaticBits.FAST_STEP
        for (i in playerWithMissedStepsStep..gameStep) {
            data[2] = i
            playerHistory.serialiseHistoricalPlayerState(gameStep - i, data, 3)
            StaticBits.server!!.sendToOne(playerWithMissedStepsId, data.size, data)
        }
        playerWithMissedStepsId = -1
    }

    private fun waitForSlowClients() {
        if (playerWithMissedStepsId != -1) return
        var biggestIndex = 0
        var biggestValue = 0
        for (i in 0..5) {
            if (clientLags[i] > biggestValue) {
                biggestIndex = i
                biggestValue = clientLags[i]
            }
        }
        biggestValue = biggestValue * biggestValue * biggestValue
        for (i in 0..<biggestValue) {
            try {
                Thread.sleep(1)
            } catch (ie: InterruptedException) {
            }
            if (clientLags[biggestIndex] < 4) break
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
        for (i in 0..5) {
            val tempxs = playerHistory.playerX[playerHistory.historyIndex][i]
            val tempys = playerHistory.playerY[playerHistory.historyIndex][i]
            NativeInterface.setPlayerPosition(i, tempxs, tempys)
        }
    }

    override fun onTouch(event: MotionEvent) {
        val count = event.pointerCount
        val p = clientIdToPlayerNumber(0)
        for (i in 0..4) {
            if (i < count) {
                xs[p][i] =
                    ((event.getX(i) / MyRenderer.displayWidth.toFloat()) * MyRenderer.WIDTH.toFloat()).toInt()
                        .toShort()
                ys[p][i] =
                    ((MyRenderer.HEIGHT - 1) - ((event.getY(i) / MyRenderer.displayHeight.toFloat()) * MyRenderer.HEIGHT.toFloat())).toInt()
                        .toShort()
            } else {
                xs[p][i] = -1
                ys[p][i] = -1
            }
        }

        if (event.action == MotionEvent.ACTION_POINTER_UP) {
            val upIndex = event.actionIndex
            xs[p][upIndex] = -1
            ys[p][upIndex] = -1
        }
    }

    override fun onHover(v: View, event: MotionEvent) {
        val count = event.pointerCount
        val p = clientIdToPlayerNumber(0)
        for (i in 0..4) {
            if (i < count) {
                xs[p][i] =
                    ((event.getX(i) / MyRenderer.displayWidth.toFloat()) * MyRenderer.WIDTH.toFloat()).toInt()
                        .toShort()
                ys[p][i] =
                    ((MyRenderer.HEIGHT - 1) - ((event.getY(i) / MyRenderer.displayHeight.toFloat()) * MyRenderer.HEIGHT.toFloat())).toInt()
                        .toShort()
            } else {
                xs[p][i] = -1
                ys[p][i] = -1
            }
        }

        if (event.action == MotionEvent.ACTION_POINTER_UP) {
            val upIndex = event.actionIndex
            xs[p][upIndex] = -1
            ys[p][upIndex] = -1
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !gameFinished) {
            val clicker = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                StaticBits.server!!.sendToAll(StaticBits.KILL_GAME, 0)
                finish()
                running = false
            }
            if (dialog != null) dialog!!.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Game is still in play! Back to menu?")
            builder.setPositiveButton("Yes", clicker)
            builder.setNegativeButton("No", null)
            dialog = builder.show()
            val messageText = dialog?.findViewById<TextView>(android.R.id.message)
            messageText?.gravity = Gravity.CENTER
            return true
        } else {
            StaticBits.server!!.sendToAll(StaticBits.BACK_TO_MENU, 0)
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onClientMessageReceived(id: Int, argc: Int, args: IntArray) {
        if (args[0] == StaticBits.RESEND_STEPS) {
            if (playerWithMissedStepsId == -1) noteMissedSteps(id, args[1])
        } else if (args[0] == StaticBits.PLAYER_POSITION_DATA) {
            val p = clientIdToPlayerNumber(id)
            for (i in 0..4) {
                xs[p][i] = args[i + 1].toShort()
                ys[p][i] = args[i + 5 + 1].toShort()
            }
        } else if (args[0] == StaticBits.CLIENT_CURRENT_GAMESTEP) {
            val p = clientIdToPlayerNumber(id)
            val clientGameStep = args[1]
            clientLags[p] = gameStep - clientGameStep
        } else if (args[0] == StaticBits.CLIENT_READY) {
            val p = clientIdToPlayerNumber(id)
            ready!![p] = true
        } else if (args[0] == StaticBits.CLIENT_EXIT) {
            val p = clientIdToPlayerNumber(id)
            if ((p >= 0) && (p < 6)) clientLags[p] = 0
        }
    }

    override fun onClientConnected(id: Int) {
    }

    override fun onClientDisconnected(id: Int) {
        val p = clientIdToPlayerNumber(id)
        if ((p >= 0) && (p < 6)) clientLags[p] = 0
        StaticBits.multiplayerGameSetupActivity!!.onClientDisconnected(id)
        ready!![p] = true
    }

    private fun noteMissedSteps(id: Int, step: Int) {
        playerWithMissedStepsStep = step
        playerWithMissedStepsId = id
    }

    private fun clientIdToPlayerNumber(id: Int): Int {
        for (i in 0..5) if (StaticBits.teams[i] == id) return i
        return -1
    }
}
