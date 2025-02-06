package com.dergoogler.liquidwars.ui.screens

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
import com.dergoogler.liquidwars.ui.providable.LocalNavController
import com.dergoogler.liquidwars.ui.utils.navigateSingleTopTo
import com.dergoogler.liquidwars.ui.navigation.graphs.HomeScreen


@Composable
fun HomeScreen() {
    val navController = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Single Player Button
        Button(
            onClick = {
                navController.navigateSingleTopTo(HomeScreen.SinglePlayer.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "Single Player Game",
                fontSize = 25.sp
            )
        }

        // Multiplayer Button
        Button(
            enabled = false,
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "Multiplayer Game",
                fontSize = 25.sp
            )
        }

        // Instructions Button
        Button(
            onClick = {
                navController.navigateSingleTopTo(HomeScreen.Instructions.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(
                text = "How to Play",
                fontSize = 25.sp
            )
        }
    }
}