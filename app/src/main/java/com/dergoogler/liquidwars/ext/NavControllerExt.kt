package com.dergoogler.liquidwars.ui.utils

import android.os.BaseBundle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import com.dergoogler.liquidwars.ext.toDecodedUrl
import com.dergoogler.liquidwars.ext.toEncodedUrl

val NavBackStackEntry.panicArguments get() = arguments ?: throw NullPointerException("Arguments are null")

fun BaseBundle.loadString(key: String, force: Boolean = false): String? {
    return this.getString(key)?.toDecodedUrl(force)
}

fun BaseBundle.panicString(key: String, force: Boolean = false): String {
    return this.loadString(key, force) ?: throw NullPointerException("Key '$key' is null")
}

fun NavController.navigateSingleTopTo(
    route: String,
    launchSingleTop: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) = navigate(
    route = route
) {
    this.launchSingleTop = launchSingleTop
    restoreState = true
    builder()
}

fun NavController.navigateSingleTopTo(
    route: String,
    args: Map<String, String> = emptyMap(),
    launchSingleTop: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    var modifiedRoute = route
    args.forEach { (key, value) ->
        modifiedRoute = modifiedRoute.replace("{$key}", value.toEncodedUrl())
    }

    navigate(modifiedRoute) {
        this.launchSingleTop = launchSingleTop
        restoreState = true
        builder()
    }
}

fun NavController.navigatePopUpTo(
    route: String,
    launchSingleTop: Boolean = true,
    restoreState: Boolean = true,
    inclusive: Boolean = true,
) = navigateSingleTopTo(
    route = route
) {
    popUpTo(
        id = currentDestination?.parent?.id ?: graph.findStartDestination().id
    ) {
        this.saveState = restoreState
        this.inclusive = inclusive
    }
    this.launchSingleTop = launchSingleTop
    this.restoreState = restoreState
}