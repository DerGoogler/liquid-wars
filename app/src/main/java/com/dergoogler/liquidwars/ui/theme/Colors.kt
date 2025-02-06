package com.dergoogler.liquidwars.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.liquidwars.BuildCompat
import com.dergoogler.liquidwars.ui.theme.color.AlmondBlossomDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.AlmondBlossomLightScheme
import com.dergoogler.liquidwars.ui.theme.color.JeufosseDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.JeufosseLightScheme
import com.dergoogler.liquidwars.ui.theme.color.PlainAuversDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.PlainAuversLightScheme
import com.dergoogler.liquidwars.ui.theme.color.PoppyFieldDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.PoppyFieldLightScheme
import com.dergoogler.liquidwars.ui.theme.color.PourvilleDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.PourvilleLightScheme
import com.dergoogler.liquidwars.ui.theme.color.SoleilLevantDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.SoleilLevantLightScheme
import com.dergoogler.liquidwars.ui.theme.color.WildRosesDarkScheme
import com.dergoogler.liquidwars.ui.theme.color.WildRosesLightScheme

sealed class Colors(
    val id: Int,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
) {
    @RequiresApi(Build.VERSION_CODES.S)
    class Dynamic(context: Context) : Colors(
        id = id,
        lightColorScheme = dynamicLightColorScheme(context),
        darkColorScheme = dynamicDarkColorScheme(context)
    ) {
        companion object {
            @Suppress("ConstPropertyName")
            const val id = -1
        }
    }
    data object Pourville : Colors(
        id = 0,
        lightColorScheme = PourvilleLightScheme,
        darkColorScheme = PourvilleDarkScheme
    )
    data object SoleilLevant : Colors(
        id = 1,
        lightColorScheme = SoleilLevantLightScheme,
        darkColorScheme = SoleilLevantDarkScheme
    )
    data object Jeufosse: Colors(
        id = 2,
        lightColorScheme = JeufosseLightScheme,
        darkColorScheme = JeufosseDarkScheme
    )
    data object PoppyField: Colors(
        id = 3,
        lightColorScheme = PoppyFieldLightScheme,
        darkColorScheme = PoppyFieldDarkScheme
    )
    data object AlmondBlossom: Colors(
        id = 4,
        lightColorScheme = AlmondBlossomLightScheme,
        darkColorScheme = AlmondBlossomDarkScheme
    )
    data object PlainAuvers: Colors(
        id = 5,
        lightColorScheme = PlainAuversLightScheme,
        darkColorScheme = PlainAuversDarkScheme
    )
    data object WildRoses: Colors(
        id = 6,
        lightColorScheme = WildRosesLightScheme,
        darkColorScheme = WildRosesDarkScheme
    )

    companion object {
        private val mColors get() = listOf(
            Pourville,
            SoleilLevant,
            Jeufosse,
            PoppyField,
            AlmondBlossom,
            PlainAuvers,
            WildRoses
        )

        fun getColorIds(): List<Int> {
            return mColors.map { it.id }
        }

        @Composable
        fun getColor(id: Int): Colors {
            val context = LocalContext.current

            return if (BuildCompat.atLeastS && id == Dynamic.id) {
                Dynamic(context)
            } else {
                mColors[id]
            }
        }
    }
}