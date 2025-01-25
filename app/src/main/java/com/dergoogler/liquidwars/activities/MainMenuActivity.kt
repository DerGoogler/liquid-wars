package com.dergoogler.liquidwars.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.server.NetInfo

class MainMenuActivity : LiquidCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)
        setAdsBanner(R.id.main_menu_ads_banner)

        val username = findViewById<TextView>(R.id.google_play_username)

        if (getPlayersClient != null) {
            username.text = getPlayersClient!!.currentPlayer.result.playerId
        }
    }

    fun singlePlayerMenu(view: View?) {
        val intent = Intent(this, SinglePlayerGameSetupActivity::class.java)
        startActivity(intent)
    }

    fun multiplayerMenu(view: View?) {
        val ip = NetInfo.getIPAddress(this)
        if (ip.compareTo("0.0.0.0") == 0) {
            Toast.makeText(this, "Need Wi-Fi connection for multiplayer game.", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            } else {
                val intent = Intent(this, MultiplayerMenuActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun instructions(view: View?) {
        val intent = Intent(this, InstructionsActivity::class.java)
        startActivity(intent)
    }
}
