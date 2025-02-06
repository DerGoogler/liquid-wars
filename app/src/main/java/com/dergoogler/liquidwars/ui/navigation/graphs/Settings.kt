package com.dergoogler.liquidwars.ui.navigation.graphs

import com.dergoogler.liquidwars.ui.screens.SettingsScreen
import com.dergoogler.liquidwars.ui.navigation.MainScreen


import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

enum class SettingsScreen(val route: String) {
    Home("Home"),
}

fun NavGraphBuilder.settingsScreen() = navigation(
    startDestination = SettingsScreen.Home.route,
    route = MainScreen.Settings.route
) {
    composable(
        route = SettingsScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        SettingsScreen()
    }
}