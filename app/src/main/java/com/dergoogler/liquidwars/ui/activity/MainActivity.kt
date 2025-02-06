package com.dergoogler.liquidwars.ui.activity

import androidx.activity.ComponentActivity
import com.dergoogler.liquidwars.ext.setBaseContent

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
//        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

//        splashScreen.setKeepOnScreenCondition { isLoading }

        setBaseContent {
            MainScreen()
        }
    }
}