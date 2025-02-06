package com.dergoogler.liquidwars.ext

import java.net.URLDecoder
import java.net.URLEncoder


private fun isUrlEncoded(url: String): Boolean {
    return try {
        val decoded = URLDecoder.decode(url, "UTF-8")
        val reEncoded = URLEncoder.encode(decoded, "UTF-8")
        url == reEncoded
    } catch (e: Exception) {
        false
    }
}

fun String.toDecodedUrl(force: Boolean = false): String = if (force || isUrlEncoded(this)) {
    URLDecoder.decode(this, "UTF-8")
} else {
    this
}


fun String.toEncodedUrl(): String = if (isUrlEncoded(this)) {
    this
} else {
    URLEncoder.encode(this, "UTF-8")
}
