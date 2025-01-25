package com.dergoogler.liquidwars

import com.dergoogler.liquidwars.activities.ClientGameSetupActivity
import com.dergoogler.liquidwars.activities.MultiplayerGameSetupActivity
import com.dergoogler.liquidwars.server.Server
import kotlin.math.abs

object StaticBits {
    var clientGameSetupActivity: ClientGameSetupActivity? = null
    var multiplayerGameSetupActivity: MultiplayerGameSetupActivity? = null
    var team: Int = 0
    var map: Int = 0
    var seed: Int = 0
    var dotsPerTeam: Int = 400
    var startTimestamp: Long = 0
    var timeLimit: Int = 4 * 60
    var client: Client? = null
    var server: Server? = null
    var publicName: String = "Liquid Wars Game"
    var teams: IntArray = IntArray(6)
    var gameWasDisconnected: Boolean = false

    const val VERSION_CODE: Int = 11
    const val NUMBER_OF_TEAMS: Int = 6
    const val AI_PLAYER: Int = -1
    const val NUMBER_OF_MAPS: Int = 46
    const val PORT_NUMBER: Int = 51055
    const val GAME_SPEED: Int = 7000
    const val RESEND_STEPS: Int = 0x70
    const val PLAYER_POSITION_DATA: Int = 0x71
    const val STEP_GAME: Int = 0x72
    const val REGULATED_STEP: Int = 0x73
    const val FAST_STEP: Int = 0x74
    const val CLIENT_CURRENT_GAMESTEP: Int = 0x75
    const val CLIENT_READY: Int = 0x76
    const val CLIENT_READY_QUERY: Int = 0x77
    const val KILL_GAME: Int = 0x78
    const val CLIENT_EXIT: Int = 0x79
    const val BACK_TO_MENU: Int = 0x7A
    const val OUT_OF_TIME: Int = 0x7B
    const val TIME_DIFF: Int = 0x7C
    const val UPDATE_SERVER_NAME: Int = 0x7D
    const val SET_TEAM: Int = 0x7E
    const val SET_TIME_LIMIT: Int = 0x7F
    const val SET_MAP: Int = 0x80
    const val START_GAME: Int = 0x81
    const val SEND_VERSION_CODE: Int = 0x82
    const val SET_TEAM_SIZE: Int = 0x83

    fun init() {
        team = 0
        map = -1
        newSeed()
        client = null
        server = null
        for (i in teams.indices) teams[i] = AI_PLAYER
        teams[team] = 0
    }

    fun newSeed() {
        seed = abs(System.currentTimeMillis().toInt().toDouble())
            .toInt()
    }
}
