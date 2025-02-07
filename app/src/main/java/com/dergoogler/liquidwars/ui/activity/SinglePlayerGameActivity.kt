package com.dergoogler.liquidwars.ui.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.ext.setBaseContent
import com.dergoogler.liquidwars.ui.screens.SinglePlayerGameScreen
import com.dergoogler.liquidwars.viewmodel.SinglePlayerGameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SinglePlayerGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        keepOn()
        hideSystemUI()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        val myGLSurfaceView = MyGLSurfaceView(this)


        setBaseContent {
            val viewModel =
                hiltViewModel<SinglePlayerGameViewModel, SinglePlayerGameViewModel.Factory> { factory ->
                    factory.create(myGLSurfaceView)
                }

            SinglePlayerGameScreen(this, viewModel)
        }
    }

    private fun keepOn() {
        val fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val keepOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        window.addFlags(fullscreen or keepOn)
    }

    private fun hideSystemUI() {
        val window = window
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false)
    }
}