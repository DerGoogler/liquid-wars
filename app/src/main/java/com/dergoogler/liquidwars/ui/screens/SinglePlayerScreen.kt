package com.dergoogler.liquidwars.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.ui.component.MapImage
import com.dergoogler.liquidwars.ui.component.MapSelectBottomSheet
import com.dergoogler.liquidwars.ui.component.Spinner
import com.dergoogler.liquidwars.ui.providable.LocalNavController
import com.dergoogler.liquidwars.ui.utils.navigateSingleTopTo
import com.dergoogler.liquidwars.viewmodel.SinglePlayerViewModel
import com.dergoogler.liquidwars.ui.navigation.graphs.HomeScreen
import com.dergoogler.liquidwars.viewmodel.SinglePlayerGameViewModel

@Composable
fun SinglePlayerScreen(
    vm: SinglePlayerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var openMapBottomSheet by remember { mutableStateOf(false) }
    if (openMapBottomSheet) {
        MapSelectBottomSheet(
            onClose = { openMapBottomSheet = false },
            maps = vm.mapsList,
            onMapSelect = {
                vm.onMapSelected(it)
                openMapBottomSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Single Player Game") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Team",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                    Spinner(
                        modifier = Modifier.weight(1f),
                        items = vm.teamsList,
                        onItemSelected = vm::onTeamSelected
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time Limit",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                    Spinner(
                        modifier = Modifier.weight(1f),
                        items = vm.timeoutList,
                        onItemSelected = vm::onTimeoutSelected
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Team Size",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                    Spinner(
                        modifier = Modifier.weight(1f),
                        items = vm.teamSizeList,
                        onItemSelected = vm::onTeamSizeSelected
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Map",
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                    OutlinedButton(
                        onClick = { openMapBottomSheet = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = vm.currentMapName)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                MapImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    selection = StaticBits.map
                )
            }

            Button(
                onClick = {
                    SinglePlayerGameViewModel.startGame(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(56.dp)
            ) {
                Text(text = "Start", fontSize = 20.sp)
            }
        }
    }
}

