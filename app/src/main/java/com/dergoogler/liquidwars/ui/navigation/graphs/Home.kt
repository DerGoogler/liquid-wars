package com.dergoogler.liquidwars.ui.navigation.graphs

import com.dergoogler.liquidwars.ui.navigation.MainScreen


import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dergoogler.liquidwars.ui.screens.SinglePlayerGameScreen
import com.dergoogler.liquidwars.ui.screens.HomeScreen
import com.dergoogler.liquidwars.ui.screens.InstructionsScreen
import com.dergoogler.liquidwars.ui.screens.SinglePlayerScreen

enum class HomeScreen(val route: String) {
    Home("Home"),
    SinglePlayer("SinglePlayer"),
    MultiPlayer("MultiPlayer"),
    Instructions("Instructions")
}

fun NavGraphBuilder.homeScreen() = navigation(
    startDestination = HomeScreen.Home.route,
    route = MainScreen.Home.route
) {
    composable(
        route = HomeScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        HomeScreen()
    }
    composable(
        route = HomeScreen.SinglePlayer.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        SinglePlayerScreen()
    }
//    composable(
//        route = HomeScreen.MultiPlayer.route,
//        enterTransition = { fadeIn() },
//        exitTransition = { fadeOut() }
//    ) {
//
//    }
    composable(
        route = HomeScreen.Instructions.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        InstructionsScreen()
    }
}