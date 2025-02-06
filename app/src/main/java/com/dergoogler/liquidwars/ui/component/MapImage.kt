package com.dergoogler.liquidwars.ui.component

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.liquidwars.R
import java.io.IOException
import java.io.InputStream

@Composable
fun MapImage(
    modifier: Modifier = Modifier,
    selection: Int,
) {
    val context = LocalContext.current

    val imageBitmap = remember(selection) {
        loadImagePainterFromAssets(context, selection)
    }

    Image(
        painter = imageBitmap,
        contentDescription = "Map Image",
        modifier = modifier
    )
}

private fun loadImagePainterFromAssets(context: Context, selection: Int): Painter {
    val assetManager = context.assets
    var inputStream: InputStream? = null

    return try {
        inputStream = try {
            if (selection == -1) assetManager.open("maps/random-map.png")
            else assetManager.open("maps/$selection-image.png")
        } catch (e: IOException) {
            try {
                assetManager.open("maps/$selection-map.png")
            } catch (ex: IOException) {
                null
            }
        }

        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IOException("Failed to decode bitmap from assets")

        BitmapPainter(bitmap.asImageBitmap())
    } catch (e: IOException) {
        e.printStackTrace()
        val fallbackBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.arrow_right)
            ?: throw IllegalStateException("Fallback drawable not found")
        BitmapPainter(fallbackBitmap.asImageBitmap())
    } finally {
        inputStream?.close()
    }
}