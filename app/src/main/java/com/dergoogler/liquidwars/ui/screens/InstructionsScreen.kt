package com.dergoogler.liquidwars.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.ui.component.TopAppBar

@Composable
fun InstructionsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Instructions") })
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text(text = stringResource(id = R.string.instructions_one))
            Text(text = stringResource(id = R.string.instructions_two))
        }
    }
}