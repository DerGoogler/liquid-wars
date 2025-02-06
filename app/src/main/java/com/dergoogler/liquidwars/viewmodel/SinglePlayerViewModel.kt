package com.dergoogler.liquidwars.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringArrayResource
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.activities.GameActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SinglePlayerViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    val context: Context get() = getApplication<Application>().applicationContext

    val teamSizeList = listOf(100, 200, 400, 800, 1000, 1500, 2000, 3000, 4500)
    val timeoutList = context.resources.getStringArray(R.array.timeout_array).toList()
    val mapsList = context.resources.getStringArray(R.array.maps_array).toList()
    val teamsList = context.resources.getStringArray(R.array.teams_array).toList()

    fun startGame(context: Context) {
        val intent = Intent(context, GameActivity::class.java)
        context.startActivity(intent)
    }

    fun onTeamSizeSelected(sel: Any) {
        StaticBits.dotsPerTeam = sel.toString().toInt()
    }

    fun onTimeoutSelected(sel: Any) {
        val selection = sel.toString()
        val pos = timeoutList.indexOfFirst { ix -> ix == selection }

        StaticBits.timeLimit = when (pos) {
            0 -> 30
            1 -> 60
            2 -> 60 * 2
            3 -> 60 * 3
            4 -> 60 * 5
            5 -> 60 * 10
            6 -> 60 * 60 * 24 * 23
            else -> StaticBits.timeLimit
        }
    }

    fun onTeamSelected(sel: Any) {
        val selection = sel.toString()
        val pos = teamsList.indexOfFirst { ix -> ix == selection }
        StaticBits.team = pos
    }

    fun onMapSelected(sel: Any) {
        val selection = sel.toString()
        val pos = mapsList.indexOfFirst { ix -> ix == selection }
        StaticBits.map = pos - 1
    }
}
