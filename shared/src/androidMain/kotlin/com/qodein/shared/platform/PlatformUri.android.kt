package com.qodein.shared.platform

import android.net.Uri

/**
 * Android implementation of PlatformUri wrapping android.net.Uri.
 */
actual class PlatformUri(val uri: Uri) {
    actual override fun toString(): String = uri.toString()

    companion object {
        /**
         * Parse string to PlatformUri.
         */
        fun parse(uriString: String): PlatformUri = PlatformUri(Uri.parse(uriString))
    }
}

/**
 * Extension to convert Android Uri to PlatformUri.
 */
fun Uri.toPlatformUri(): PlatformUri = PlatformUri(this)
