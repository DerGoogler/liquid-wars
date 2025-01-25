package com.dergoogler.liquidwars.activities

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dergoogler.liquidwars.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.PlayersClient
import com.google.android.gms.tasks.Task

open class LiquidCompatActivity : AppCompatActivity() {
    private var adView: AdView? = null

    protected var getPlayersClient: PlayersClient? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        PlayGamesSdk.initialize(this)


        val gamesSignInClient = PlayGames.getGamesSignInClient(this)

        gamesSignInClient.isAuthenticated()
            .addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
                val isAuthenticated =
                    (isAuthenticatedTask.isSuccessful &&
                            isAuthenticatedTask.result.isAuthenticated)
                if (isAuthenticated) {
                    getPlayersClient = PlayGames.getPlayersClient(this)
                } else {
                    Log.i("LiquidCompat", "Not logged in Google Play Services")
                }
            }



        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        this.adView = AdView(this)
    }

    protected fun hideSystemUI() {
        val window = window
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false)
    }

    protected fun keepOn() {
        val fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN
        val keepOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        window.addFlags(fullscreen or keepOn)
    }

    protected fun setAdsBanner(id: Int) {
        adView!!.setAdSize(AdSize(AdSize.FULL_WIDTH, 50))
        adView!!.adUnitId = BuildConfig.BANNER_UNIT
        val layout = findViewById<LinearLayout>(id)
        layout.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
    }

    public override fun onResume() {
        super.onResume()
        adView!!.resume()
    }

    public override fun onPause() {
        adView!!.pause()
        super.onPause()
    }

    public override fun onDestroy() {
        adView!!.destroy()
        super.onDestroy()
    }
}
