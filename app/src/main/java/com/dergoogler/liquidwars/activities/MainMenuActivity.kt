package com.dergoogler.liquidwars.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.server.NetInfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dergoogler.liquidwars.ext.setBaseContent

//class MainMenuActivity : LiquidCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main_menu)
//        setAdsBanner(R.id.main_menu_ads_banner)
//
//        val username = findViewById<TextView>(R.id.google_play_username)
//
//        if (getPlayersClient != null) {
//            username.text = getPlayersClient!!.currentPlayer.result.playerId
//        }
//    }
//
//    fun singlePlayerMenu(view: View?) {
//        val intent = Intent(this, SinglePlayerGameSetupActivity::class.java)
//        startActivity(intent)
//    }
//
//    fun multiplayerMenu(view: View?) {
//        val ip = NetInfo.getIPAddress(this)
//        if (ip.compareTo("0.0.0.0") == 0) {
//            Toast.makeText(this, "Need Wi-Fi connection for multiplayer game.", Toast.LENGTH_SHORT)
//                .show()
//        } else {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    0
//                )
//            } else {
//                val intent = Intent(this, MultiplayerMenuActivity::class.java)
//                startActivity(intent)
//            }
//        }
//    }
//
//    fun instructions(view: View?) {
//        val intent = Intent(this, InstructionsActivity::class.java)
//        startActivity(intent)
//    }
//}


class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent {
//            MainScreen(
//                onSinglePlayerMenuClicked = { /* Handle single player menu click */ },
//                onMultiplayerMenuClicked = { /* Handle multiplayer menu click */ },
//                onInstructionsClicked = {
//                    val intent = Intent(this, InstructionsActivity::class.java)
//                    startActivity(intent)
//                }
//            )
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
}

//
//@Composable
//fun MainScreen(
//    onSinglePlayerMenuClicked: () -> Unit,
//    onMultiplayerMenuClicked: () -> Unit,
//    onInstructionsClicked: () -> Unit,
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .systemBarsPadding(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        // Single Player Button
//        Button(
//            onClick = onSinglePlayerMenuClicked,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp)
//        ) {
//            Text(
//                text = "Single Player Game",
//                fontSize = 25.sp
//            )
//        }
//
//        // Multiplayer Button
//        Button(
//            onClick = onMultiplayerMenuClicked,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp)
//        ) {
//            Text(
//                text = "Multiplayer Game",
//                fontSize = 25.sp
//            )
//        }
//
//        // Instructions Button
//        Button(
//            onClick = onInstructionsClicked,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
//        ) {
//            Text(
//                text = "How to Play",
//                fontSize = 25.sp
//            )
//        }
//    }
//}