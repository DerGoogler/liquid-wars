package com.dergoogler.liquidwars.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dergoogler.liquidwars.MyGLSurfaceView
import com.dergoogler.liquidwars.NativeInterface
import com.dergoogler.liquidwars.R
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
    val closeButtonInteractionSource: MutableInteractionSource =
        remember { MutableInteractionSource() }

    val density = LocalDensity.current
    val topInsets = WindowInsets.systemBars.getTop(density)
    val leftInsets = WindowInsets.systemBars.getLeft(density, LayoutDirection.Ltr)

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> vm.destroyGame()
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> vm.resumeGame()
        }
    }

    val close = remember(vm.paused, vm.gameFinished) {
        {
            if (!vm.gameFinished && !vm.lostGame) {
                vm.pauseGame()
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.startGame()
    }

    BackHandler(
        enabled = true,
        onBack = close
    )

    Scaffold { paddingValues ->

        Box(
            modifier = Modifier.padding(paddingValues),
        ) {
            AndroidView(
                modifier = Modifier
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
            Box(
                modifier = Modifier
                    .padding(
                        vertical = 16.plus(leftInsets).dp,
                        horizontal = 16.plus(topInsets).dp
                    )
                    .clickable(
                        enabled = true,
                        interactionSource = closeButtonInteractionSource,
                        indication = ripple(),
                        onClick = close
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.x),
                    contentDescription = "Close"
                )
            }
        }
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