package com.dergoogler.liquidwars.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dergoogler.liquidwars.R

enum class MainScreen(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val iconFilled: Int,
) {
    Home(
        route = "HomeScreen",
        label = "Home",
        icon = R.drawable.home_outlined,
        iconFilled = R.drawable.home
    ),

    Settings(
        route = "Settings",
        label = "Settings",
        icon = R.drawable.settings_outlined,
        iconFilled = R.drawable.settings
    ),
}