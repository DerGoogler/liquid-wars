package com.dergoogler.liquidwars.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("CompositionLocal NavController not present")
}