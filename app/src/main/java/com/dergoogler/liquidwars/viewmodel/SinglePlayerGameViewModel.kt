package com.dergoogler.liquidwars.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.MyRenderer
import com.dergoogler.liquidwars.NativeInterface
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.Util.regulateSpeed
import com.dergoogler.liquidwars.Util.teamToNameString
import com.dergoogler.liquidwars.activities.GameActivity
import com.dergoogler.liquidwars.ui.activity.SinglePlayerGameActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel(assistedFactory = SinglePlayerGameViewModel.Factory::class)
class SinglePlayerGameViewModel @AssistedInject constructor(
    @Assisted val myGLSurfaceView: MyGLSurfaceView,
    application: Application,
) : AndroidViewModel(application) {

    var paused by mutableStateOf(false)
    var running by mutableStateOf(false)
    var gameFinished by mutableStateOf(false)
    var lostGame by mutableStateOf(false)
    var gameStateDialogTitle by mutableStateOf("")
    var gameStateDialogMessage by mutableStateOf("")
    var gameStep by mutableIntStateOf(0)
    var frozen by mutableStateOf(false)
    val xs = Array(6) { ShortArray(5) }
    val ys = Array(6) { ShortArray(5) }
    var winningTeam by mutableIntStateOf(0)

    fun onTouch(event: MotionEvent): Boolean {
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

        return true
    }

    fun onJoystick(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {

            val inputDevice = event.device

            if (inputDevice != null) {
                val count = event.pointerCount
                for (i in 0..4) {
                    if (i < count) {
                        val xAxis = event.getAxisValue(MotionEvent.AXIS_X, count)
                        val yAxis = event.getAxisValue(MotionEvent.AXIS_Y, count)
                        xs[StaticBits.team][i] =
                            ((xAxis / MyRenderer.displayWidth.toFloat()) * MyRenderer.WIDTH.toFloat()).toInt()
                                .toShort()
                        ys[StaticBits.team][i] =
                            ((MyRenderer.HEIGHT - 1) - ((yAxis / MyRenderer.displayHeight.toFloat()) * MyRenderer.HEIGHT.toFloat())).toInt()
                                .toShort()
                    } else {
                        xs[StaticBits.team][i] = -1
                        ys[StaticBits.team][i] = -1
                    }
                }

            }
        }

        return true
    }

    fun pauseGame() {
        paused = true
        myGLSurfaceView.releasePointerCapture()
        myGLSurfaceView.onPause()
    }

    fun resumeGame() {
        paused = false
        myGLSurfaceView.requestPointerCapture()
        myGLSurfaceView.onResume()
    }

    fun destroyGame() {
        myGLSurfaceView.releasePointerCapture()
        running = false
    }

    suspend fun startGame() = withContext(Dispatchers.IO) {
        running = true
        gameFinished = false
        lostGame = false
        gameStep = 0
        frozen = false
        var aiStartDelay = 6
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
                delay(100)
            }
        }
        NativeInterface.destroyGame()
        NativeInterface.uninit()
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

    private fun checkTimeout(timeDiff: Int) {
        if ((timeDiff >= StaticBits.timeLimit) && !gameFinished) {
            viewModelScope.launch {
                frozen = true
                gameFinished = true
                winningTeam = (0..5).maxByOrNull { NativeInterface.teamScore(it) } ?: 0
                if (winningTeam == StaticBits.team) {
                    gameStateDialogTitle = "Out of time!"
                    gameStateDialogMessage = "You've won the game!"
                } else {
                    gameStateDialogTitle = "Out of time!"
                    gameStateDialogMessage = "${teamToNameString(winningTeam)} wins the game!"
                }
            }
        }
    }

    private fun checkIfLost() {
        if (lostGame) return
        if (NativeInterface.teamScore(StaticBits.team) == 0) {
            viewModelScope.launch {
                lostGame = true
                gameStateDialogTitle = "Lose!"
                gameStateDialogMessage = "You've lost the game"
            }
        }
    }

    private fun checkForWinner() {
        if (gameFinished) return
        for (i in 0..5) {
            if (NativeInterface.teamScore(i) == StaticBits.NUMBER_OF_TEAMS * StaticBits.dotsPerTeam) {
                viewModelScope.launch {
                    gameFinished = true
                    if (i == StaticBits.team) {
                        gameStateDialogTitle = "Winner!"
                        gameStateDialogMessage = "You've won the game!"
                    } else {
                        gameStateDialogTitle = "Lost!"
                        gameStateDialogMessage = "${teamToNameString(i)} wins the game!"
                    }
                }
                break
            }
        }
    }

    companion object {
        fun startGame(context: Context) {
            val intent = Intent(context, SinglePlayerGameActivity::class.java)
            context.startActivity(intent)
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(myGLSurfaceView: MyGLSurfaceView): SinglePlayerGameViewModel
    }
}