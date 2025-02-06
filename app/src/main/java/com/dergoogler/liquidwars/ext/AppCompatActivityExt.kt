package com.dergoogler.liquidwars.ext

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.dergoogler.liquidwars.ui.providable.LocalNavController
import com.dergoogler.liquidwars.ui.theme.AppTheme

fun ComponentActivity.setBaseContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
) = this.setContent(
    parent = parent,
) {
    val navController = rememberNavController()

    AppTheme(
        darkMode = isSystemInDarkTheme(), themeColor = 0
    ) {
        CompositionLocalProvider(
            LocalNavController provides navController
        ) {
            content()
        }
    }
}