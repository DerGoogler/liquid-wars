package com.dergoogler.liquidwars.activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.dergoogler.liquidwars.Client
import com.dergoogler.liquidwars.Client.ClientCallbacks
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.StaticBits.init
import com.dergoogler.liquidwars.Util.getMapName
import com.dergoogler.liquidwars.Util.getTimeoutString
import com.dergoogler.liquidwars.Util.intToTime
import com.dergoogler.liquidwars.Util.teamToNameString
import java.io.IOException
import java.io.InputStream

class ClientGameSetupActivity : LiquidCompatActivity(), ClientCallbacks {
    private val nameEditText: EditText? = null
    private var context: Context? = null
    private var serverIP: String? = null
    private var serverName: String? = null
    private var retries = 0
    private var myID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        StaticBits.multiplayerGameSetupActivity = null
        StaticBits.clientGameSetupActivity = this
        retries = 4
        setContentView(R.layout.client_game_setup)
        setAdsBanner(R.id.client_ads_banner)
        init()
        refreshMapImage()

        val extras = intent.extras
        serverIP = extras!!.getString("ip")
        serverName = extras.getString("name")
        setTextView(R.id.client_game_textview, "Connecting to $serverName...")
        StaticBits.client = Client(this, serverIP, StaticBits.PORT_NUMBER)
    }

    override fun onDestroy() {
        super.onDestroy()
        StaticBits.client!!.destroy()
    }

    private fun refreshMapImage() {
        runOnUiThread {
            var `is`: InputStream? = null
            try {
                `is` = if (StaticBits.map == -1) assets.open("maps/random-map.png")
                else assets.open("maps/" + StaticBits.map + "-image.png")
            } catch (e: IOException) {
                try {
                    `is` = assets.open("maps/" + StaticBits.map + "-map.png")
                } catch (ex: IOException) {
                }
            }
            val d = Drawable.createFromStream(`is`, null)
            val iv = findViewById<View>(R.id.map_imageview) as ImageView
            iv.setImageDrawable(d)
        }
    }

    override fun onServerMessageReceived(argc: Int, args: IntArray) {
        if (args[0] == StaticBits.UPDATE_SERVER_NAME) {
            serverName = String(args, 1, argc - 1)
            setTextView(R.id.client_game_textview, "Connected to $serverName")
        } else if (args[0] == StaticBits.SET_TEAM) {
            setTextView(R.id.team_textview, teamToNameString(args[1]))
            StaticBits.team = args[1]
        } else if (args[0] == StaticBits.SET_TIME_LIMIT) {
            setTextView(R.id.timeout_textview, getTimeoutString(this, args[1]))
            StaticBits.timeLimit = intToTime(args[1])
        } else if (args[0] == StaticBits.SET_MAP) {
            setTextView(R.id.map_textview, getMapName(this, args[1]))
            StaticBits.map = args[1] - 1
            refreshMapImage()
        } else if (args[0] == StaticBits.START_GAME) {
            StaticBits.seed = args[1]
            StaticBits.map = args[2]
            StaticBits.dotsPerTeam = args[3]
            StaticBits.client!!.setCallbacks(null)
            startClientGame()
        } else if (args[0] == StaticBits.SEND_VERSION_CODE) {
            if (args[1] != StaticBits.VERSION_CODE) checkVersionCompatibility(args[1])
        } else if (args[0] == StaticBits.SET_TEAM_SIZE) {
            StaticBits.dotsPerTeam = args[1]
            setTextView(R.id.teamsize_textview, args[1].toString() + "")
        }
    }

    override fun onServerConnectionMade(id: Int, ip: String) {
        myID = id
        setTextView(R.id.client_game_textview, "Connected to $serverName")
        StaticBits.client!!.send(StaticBits.SEND_VERSION_CODE, StaticBits.VERSION_CODE)
    }

    override fun onServerConnectionFailed(ip: String) {
        myID = 0
        toast("Failed to connect to $serverName", Toast.LENGTH_SHORT)
        finish()
    }

    override fun onServerConnectionClosed(ip: String) {
        myID = 0
        toast("Lost connection with $serverName", Toast.LENGTH_SHORT)
        finish()
    }

    private fun checkVersionCompatibility(v: Int) {
        if (StaticBits.VERSION_CODE < v) {
            toast("Liquid Wars needs updating.", Toast.LENGTH_LONG)
            finish()
        } else if (StaticBits.VERSION_CODE > v) {
            toast("Server needs updating.", Toast.LENGTH_LONG)
            finish()
        }
    }

    private fun startClientGame() {
        runOnUiThread {
            val intent = Intent(context, GameClientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setTextView(id: Int, message: String) {
        runOnUiThread {
            val tv = findViewById<View>(id) as TextView
            tv.text = message
        }
    }

    private fun toast(message: String, length: Int) {
        runOnUiThread { Toast.makeText(context, message, length).show() }
    }
}
