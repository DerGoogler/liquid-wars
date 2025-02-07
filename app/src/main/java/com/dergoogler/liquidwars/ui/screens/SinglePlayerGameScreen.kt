package com.dergoogler.liquidwars.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.NativeInterface
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.Util.loadPlayerInitialPositions
import com.dergoogler.liquidwars.Util.teamToNameString
import com.dergoogler.liquidwars.ui.activity.SinglePlayerGameActivity
import com.dergoogler.liquidwars.ui.component.ConfirmDialog
import com.dergoogler.liquidwars.viewmodel.SinglePlayerGameViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SinglePlayerGameScreen(
    activity: SinglePlayerGameActivity,
    vm: SinglePlayerGameViewModel,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> vm.destroyGame()
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> vm.resumeGame()
        }
    }

    LaunchedEffect(Unit) {
        vm.startGame()
    }

    BackHandler(
        enabled = true,
        onBack = {
            if (!vm.gameFinished && !vm.lostGame) {
                vm.pauseGame()
            }
        }
    )

    Scaffold { paddingValues ->
        AndroidView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    vm.onTouch(event)
                    true
                },
            factory = {
                loadPlayerInitialPositions(vm.xs, vm.ys)
                NativeInterface.init(context.assets)
                NativeInterface.createGame(
                    StaticBits.team,
                    StaticBits.map,
                    StaticBits.seed,
                    StaticBits.dotsPerTeam
                )
                vm.myGLSurfaceView.requestPointerCapture()
                vm.myGLSurfaceView
            }
        )
    }

    if (vm.paused) ConfirmDialog(
        title = "Quit Game?",
        description = "Are you sure that you wanna go back to the main menu?",
        onClose = { vm.resumeGame() },
        onConfirm = {
            vm.destroyGame()
            activity.finish()
        }
    )

    if (vm.gameFinished) {
        ConfirmDialog(
            title = vm.gameStateDialogTitle,
            description = vm.gameStateDialogMessage,
            confirmText = "Back to menu",
            cancelText = "Close",
            onClose = {
                vm.resumeGame()
            },
            onConfirm = {
                vm.destroyGame()
                activity.finish()
            }
        )
    }
}