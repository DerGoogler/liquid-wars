package com.dergoogler.liquidwars.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.StaticBits.init
import com.dergoogler.liquidwars.StaticBits.newSeed
import com.dergoogler.liquidwars.Util.clientIdToPlayerNumber
import com.dergoogler.liquidwars.Util.intToTime
import com.dergoogler.liquidwars.Util.teamToNameString
import com.dergoogler.liquidwars.server.NetInfo
import com.dergoogler.liquidwars.server.Server
import com.dergoogler.liquidwars.server.Server.ServerCallbacks
import com.dergoogler.liquidwars.server.ServerFinder
import com.google.android.material.button.MaterialButton
import java.io.IOException
import java.io.InputStream

class MultiplayerGameSetupActivity : LiquidCompatActivity(), AdapterView.OnItemSelectedListener,
    OnLongClickListener, ServerCallbacks {
    private val teamSpinner: Spinner? = null
    private var mapSpinner: Spinner? = null
    private var timeoutSpinner: Spinner? = null
    private var teamSizeSpinner: Spinner? = null
    private var nameEditText: EditText? = null
    private var context: Context? = null
    private var nametv: TextView? = null
    private var myID = 0
    private var numberOfClients = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        StaticBits.multiplayerGameSetupActivity = this
        StaticBits.clientGameSetupActivity = null
        setContentView(R.layout.multi_game_setup)
        setAdsBanner(R.id.multi_game_ads_banner)
        init()
        refreshMapImage()
        initSpinners()
        initButtons()
        ServerFinder.share(context, StaticBits.PORT_NUMBER + 1, StaticBits.publicName)
        StaticBits.server = Server(this, StaticBits.PORT_NUMBER)
        updateMessageTextView()
        nametv = findViewById(R.id.public_name_textview)
        nametv?.text = StaticBits.publicName
        val tv = findViewById<TextView>(R.id.team_textview)
        tv.text = teamToNameString(0)
    }

    override fun onResume() {
        super.onResume()
        newSeed()
        if (StaticBits.gameWasDisconnected) {
            if (StaticBits.client != null) StaticBits.client!!.destroy()
            StaticBits.client = null
            myID = 0
            for (i in 1..5) StaticBits.teams[i] = StaticBits.AI_PLAYER
            StaticBits.teams[0] = myID
            StaticBits.gameWasDisconnected = false
        }
        if (StaticBits.server != null) {
            ServerFinder.share(context, StaticBits.PORT_NUMBER + 1, StaticBits.publicName)
            StaticBits.server!!.startAccepting()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StaticBits.server!!.destroy()
        ServerFinder.stopSharing()
    }

    fun changePublicName(view: View?) {
        val clicker = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
            val name = nameEditText!!.text.toString()
            if ((name.length > 0) && (StaticBits.publicName.compareTo(name) != 0)) {
                StaticBits.publicName = name
                ServerFinder.stopSharing()
                ServerFinder.share(context, StaticBits.PORT_NUMBER + 1, StaticBits.publicName)
                val nameBytes = name.toByteArray()
                val data = IntArray(1 + nameBytes.size)
                data[0] = StaticBits.UPDATE_SERVER_NAME
                for (i in 1..<data.size) data[i] = nameBytes[i - 1].toInt()
                StaticBits.server!!.sendToAll(data.size, data)
                nametv!!.text = StaticBits.publicName
            }
        }
        nameEditText = EditText(this)
        nameEditText!!.setText(nametv!!.text)
        AlertDialog.Builder(this)
            .setTitle("Enter a name to identify your game:")
            .setPositiveButton("Done", clicker)
            .setNegativeButton("Cancel", null)
            .setView(nameEditText)
            .show()
    }

    private fun initSpinners() {
        var adapter: ArrayAdapter<CharSequence?>
        val simpleSpinnerItem = android.R.layout.simple_spinner_item

        mapSpinner = findViewById(R.id.map_spinner)
        adapter = ArrayAdapter.createFromResource(this, R.array.maps_array, simpleSpinnerItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapSpinner?.setAdapter(adapter)
        mapSpinner?.setOnItemSelectedListener(this)

        timeoutSpinner = findViewById(R.id.timeout_spinner)
        adapter = ArrayAdapter.createFromResource(this, R.array.timeout_array, simpleSpinnerItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeoutSpinner?.setAdapter(adapter)
        timeoutSpinner?.setOnItemSelectedListener(this)
        timeoutSpinner?.setSelection(2)

        teamSizeSpinner = findViewById(R.id.teamsize_spinner)
        adapter = ArrayAdapter.createFromResource(this, R.array.teamsize_array, simpleSpinnerItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teamSizeSpinner?.setAdapter(adapter)
        teamSizeSpinner?.setOnItemSelectedListener(this)
        teamSizeSpinner?.setSelection(2)
    }

    private fun initButtons() {
        val previousButton = findViewById<MaterialButton>(R.id.previous_button)
        previousButton.setOnLongClickListener(this)
        val nextButton = findViewById<MaterialButton>(R.id.next_button)
        nextButton.setOnLongClickListener(this)
    }

    fun start(view: View?) {
        ServerFinder.stopSharing()
        StaticBits.server!!.stopAccepting()
        val args = intArrayOf(
            StaticBits.START_GAME,
            StaticBits.seed,
            StaticBits.map,
            StaticBits.dotsPerTeam
        )
        StaticBits.server!!.sendToAll(4, args)
        StaticBits.server!!.setCallbacks(null)
        StaticBits.team = clientIdToPlayerNumber(myID)

        val intent = Intent(this, GameServerActivity::class.java)
        startActivity(intent)
    }

    override fun onLongClick(view: View): Boolean {
        val id = view.id
        if (id == R.id.next_button) {
            var pos = mapSpinner!!.selectedItemPosition
            pos += 20
            if (pos > StaticBits.NUMBER_OF_MAPS) pos = StaticBits.NUMBER_OF_MAPS
            mapSpinner!!.setSelection(pos)
        } else if (id == R.id.previous_button) {
            var pos = mapSpinner!!.selectedItemPosition
            pos -= 20
            if (pos < 0) pos = 0
            mapSpinner!!.setSelection(pos)
        }
        return true
    }

    fun nextMap(view: View?) {
        var pos = mapSpinner!!.selectedItemPosition
        pos++
        if (pos > StaticBits.NUMBER_OF_MAPS) pos = StaticBits.NUMBER_OF_MAPS
        mapSpinner!!.setSelection(pos)
    }

    fun previousMap(view: View?) {
        var pos = mapSpinner!!.selectedItemPosition
        pos--
        if (pos < 0) pos = 0
        mapSpinner!!.setSelection(pos)
    }

    fun changeTeam(view: View?) {
        //TODO cycle through available teams.
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val spinnerId = parent.id
        if (spinnerId == R.id.map_spinner) {
            StaticBits.map = pos - 1
            val m = mapSpinner!!.selectedItemPosition
            StaticBits.server!!.sendToAll(StaticBits.SET_MAP, pos)
            refreshMapImage()
        } else if (spinnerId == R.id.timeout_spinner) {
            StaticBits.timeLimit = intToTime(pos)
            val t = timeoutSpinner!!.selectedItemPosition
            StaticBits.server!!.sendToAll(StaticBits.SET_TIME_LIMIT, pos)
        } else if (spinnerId == R.id.teamsize_spinner) {
            if (view != null) {
                StaticBits.dotsPerTeam = ((view as TextView).text.toString() + "").toInt()
            }
            StaticBits.server!!.sendToAll(StaticBits.SET_TEAM_SIZE, StaticBits.dotsPerTeam)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun refreshMapImage() {
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
        val iv = findViewById<ImageView>(R.id.map_imageview)
        iv.setImageDrawable(d)
    }

    override fun onClientMessageReceived(id: Int, argc: Int, args: IntArray) {
        if (args[0] == StaticBits.SEND_VERSION_CODE) {
            if (args[1] != StaticBits.VERSION_CODE) checkVersionCompatibility(args[1])
        }
    }

    private fun checkVersionCompatibility(v: Int) {
        if (StaticBits.VERSION_CODE < v) toast("Liquid Wars needs updating.", Toast.LENGTH_LONG)
    }

    override fun onClientConnected(id: Int) {
        numberOfClients++
        updateMessageTextView()
        for (i in 0..5) {
            if (StaticBits.teams[i] == StaticBits.AI_PLAYER) {
                StaticBits.teams[i] = id
                StaticBits.server!!.sendToOne(id, StaticBits.SET_TEAM, i)
                StaticBits.server!!.sendToOne(
                    id,
                    StaticBits.SEND_VERSION_CODE,
                    StaticBits.VERSION_CODE
                )
                break
            }
        }
        val t = timeoutSpinner!!.selectedItemPosition
        StaticBits.server!!.sendToOne(id, StaticBits.SET_TIME_LIMIT, t)
        val m = mapSpinner!!.selectedItemPosition
        StaticBits.server!!.sendToOne(id, StaticBits.SET_MAP, m)
        StaticBits.server!!.sendToAll(StaticBits.SET_TEAM_SIZE, StaticBits.dotsPerTeam)
    }

    override fun onClientDisconnected(id: Int) {
        numberOfClients--
        updateMessageTextView()
        for (i in 0..5) {
            if (StaticBits.teams[i] == id) {
                StaticBits.teams[i] = StaticBits.AI_PLAYER
                break
            }
        }
    }

    private fun updateMessageTextView() {
        runOnUiThread {
            val tv = findViewById<TextView>(R.id.multi_game_textview)
            val ip = NetInfo.getIPAddress(context)
            val ssid = NetInfo.getSSID(context)
            tv.text =
                "Sharing game on " + ssid + ". IP Address: " + ip + ". Number of players: " + (numberOfClients + 1)
        }
    }

    private fun toast(message: String, length: Int) {
        runOnUiThread { Toast.makeText(context, message, length).show() }
    }
}
