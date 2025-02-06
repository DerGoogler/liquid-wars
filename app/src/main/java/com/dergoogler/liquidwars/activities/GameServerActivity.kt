package com.dergoogler.liquidwars.activities

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameServerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameServerScreen()
        }
    }
}

@Composable
fun GameServerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gameRunning by remember { mutableStateOf(true) }
    var gameFinished by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val playerScores = remember { mutableStateListOf(0, 0, 0, 0, 0, 0) }
    val readyPlayers = remember { mutableStateListOf(false, false, false, false, false, false) }

    // Simulate game logic
    LaunchedEffect(gameRunning) {
        while (gameRunning) {
            delay(1000)
            if (!gameFinished) {
                val winner = checkForWinner(playerScores)
                if (winner != null) {
                    message = if (winner == 0) "You Win!" else "Player $winner Wins!"
                    gameFinished = true
                } else {
                    incrementScores(playerScores)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Game Server") })
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Game Server Running", style = MaterialTheme.typography.titleMedium)

                    Button(onClick = {
                        if (!gameFinished) {
                            gameRunning = false
                            Toast.makeText(context, "Game Paused", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Game Already Finished", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Pause Game")
                    }

                    Button(onClick = {
                        gameRunning = true
                        Toast.makeText(context, "Game Resumed", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Resume Game")
                    }

                    Button(onClick = {
                        gameRunning = false
                        gameFinished = true
                        message = "Game Ended"
                    }) {
                        Text("End Game")
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (message.contains("Win")) Color.Green else Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playerScores.size) { index ->
                            Text("Player ${index + 1} Score: ${playerScores[index]}")
                        }
                    }
                }
            }
        }
    )
}

private fun checkForWinner(scores: List<Int>): Int? {
    val maxScore = scores.maxOrNull() ?: return null
    return if (maxScore >= 100) scores.indexOf(maxScore) else null
}

private fun incrementScores(scores: MutableList<Int>) {
    for (i in scores.indices) {
        scores[i] += (1..10).random()
    }
}
