package com.dergoogler.liquidwars.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.StaticBits.init
import com.dergoogler.liquidwars.StaticBits.newSeed
import com.dergoogler.liquidwars.Util.clientIdToPlayerNumber
import com.dergoogler.liquidwars.server.ServerFinder
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

class MultiplayerGameSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiplayerGameSetupScreen(
                context = this,
                onGameStart = {
                    val intent = Intent(this, GameServerActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ScreenEventListener(onEvent: (event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(newValue = onEvent)
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            eventHandler.value(event)
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun MultiplayerGameSetupScreen(context: Context, onGameStart: () -> Unit) {
    val resources = LocalContext.current.resources
    var publicName by remember { mutableStateOf(StaticBits.publicName) }
    var selectedMapIndex by remember { mutableStateOf(0) }
    var selectedTimeoutIndex by remember { mutableStateOf(2) }
    var selectedTeamSizeIndex by remember { mutableStateOf(2) }
    var myId by remember { mutableStateOf(0) }
    var numberOfClients by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        init()
//        StaticBits.multiplayerGameSetupActivity = this@LaunchedEffect
        newSeed()
    }

//    ScreenEventListener {
//        when (it) {
//            Lifecycle.Event.ON_RESUME -> {
//                myId = 0
//            }
//            else -> {}
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Public Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Public Name:", fontSize = 20.sp)
            BasicTextField(
                value = publicName,
                onValueChange = { publicName = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    StaticBits.publicName = publicName
                    ServerFinder.share(context, StaticBits.PORT_NUMBER + 1, StaticBits.publicName)
                }),
                modifier = Modifier.width(200.dp)
            )
        }

        // Map Selection
        DropdownMenuField(
            label = "Select Map",
            options = resources.getStringArray(R.array.maps_array),
            selectedIndex = selectedMapIndex,
            onOptionSelected = { selectedMapIndex = it }
        )

        // Timeout Selection
        DropdownMenuField(
            label = "Select Timeout",
            options = resources.getStringArray(R.array.timeout_array),
            selectedIndex = selectedTimeoutIndex,
            onOptionSelected = { selectedTimeoutIndex = it }
        )

        // Team Size Selection
        DropdownMenuField(
            label = "Select Team Size",
            options = resources.getStringArray(R.array.teamsize_array),
            selectedIndex = selectedTeamSizeIndex,
            onOptionSelected = { selectedTeamSizeIndex = it }
        )

        // Map Preview
        MapPreview(selectedMapIndex)

        // Start Game Button
        Button(
            onClick = {
                coroutineScope.launch {
                    StaticBits.server!!.stopAccepting()
                    StaticBits.teams[0] = clientIdToPlayerNumber(myId)
                    onGameStart()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Start Game")
        }

        // Footer Info
        Text(
            text = "Number of players: ${numberOfClients + 1}",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun DropdownMenuField(
    label: String,
    options: Array<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(text = options[selectedIndex])
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            onOptionSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MapPreview(selectedMapIndex: Int) {
    val context = LocalContext.current
    val mapImage = remember(selectedMapIndex) {
        try {
            val assetName =
                if (selectedMapIndex == -1) "maps/random-map.png" else "maps/$selectedMapIndex-image.png"
            val `is`: InputStream = context.assets.open(assetName)
            Drawable.createFromStream(`is`, null)?.toBitmap()
        } catch (e: IOException) {
            null
        }
    }

    if (mapImage != null) {
        Image(
            bitmap = mapImage.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
    } else {
        Text("No map preview available", color = Color.Red, fontSize = 16.sp)
    }
}
