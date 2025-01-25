package com.dergoogler.liquidwars.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.StaticBits.init
import com.dergoogler.liquidwars.StaticBits.newSeed
import com.google.android.material.button.MaterialButton
import java.io.IOException
import java.io.InputStream

class SinglePlayerGameSetupActivity : LiquidCompatActivity(), AdapterView.OnItemSelectedListener,
    OnLongClickListener {
    private var teamSpinner: Spinner? = null
    private var mapSpinner: Spinner? = null
    private var timeoutSpinner: Spinner? = null
    private var teamSizeSpinner: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_game_setup)
        setAdsBanner(R.id.single_game_ads_banner)
        init()
        refreshMapImage()
        initSpinners()
        initButtons()
    }

    override fun onResume() {
        super.onResume()
        newSeed()
    }

    private fun initSpinners() {
        var adapter: ArrayAdapter<CharSequence?>
        val simpleSpinnerItem = android.R.layout.simple_spinner_item

        teamSpinner = findViewById(R.id.team_spinner)
        adapter = ArrayAdapter.createFromResource(this, R.array.teams_array, simpleSpinnerItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teamSpinner?.setAdapter(adapter)
        teamSpinner?.setOnItemSelectedListener(this)

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
        val intent = Intent(this, GameActivity::class.java)
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

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val spinnerId = parent.id
        if (spinnerId == R.id.map_spinner) {
            StaticBits.map = pos - 1
            refreshMapImage()
        } else if (spinnerId == R.id.team_spinner) {
            StaticBits.team = pos
        } else if (spinnerId == R.id.timeout_spinner) {
            if (pos == 0) StaticBits.timeLimit = 30
            else if (pos == 1) StaticBits.timeLimit = 60
            else if (pos == 2) StaticBits.timeLimit = 60 * 2
            else if (pos == 3) StaticBits.timeLimit = 60 * 3
            else if (pos == 4) StaticBits.timeLimit = 60 * 5
            else if (pos == 5) StaticBits.timeLimit = 60 * 10
            else if (pos == 6) StaticBits.timeLimit = 60 * 60 * 24 * 23
        } else if (spinnerId == R.id.teamsize_spinner) {
            if (view != null) {
                StaticBits.dotsPerTeam = ((view as TextView).text.toString() + "").toInt()
            }
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
}
