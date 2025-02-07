package com.dergoogler.liquidwars.ui.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
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
}