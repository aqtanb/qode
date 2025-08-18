package com.qodein.core.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import timber.log.Timber

/**
 * Utility functions for launching Custom Chrome Tabs following NIA patterns.
 * Provides branded browser experience with app theming integration.
 */
object CustomTabsUtils {

    /**
     * Launches a Custom Chrome Tab with branded theming.
     * Falls back to default browser if Custom Tabs not available.
     *
     * @param context Android context
     * @param uri The URI to open
     * @param toolbarColor Toolbar color for branding (optional)
     */
    fun launchCustomTab(
        context: Context,
        uri: Uri,
        toolbarColor: Int? = null
    ) {
        try {
            val customTabsIntent = buildCustomTabsIntent(context, toolbarColor)
            customTabsIntent.launchUrl(context, uri)
        } catch (exception: Exception) {
            Timber.w(exception, "Failed to launch Custom Tab, falling back to browser")
            launchFallbackBrowser(context, uri)
        }
    }

    /**
     * Launches a Custom Chrome Tab with URL validation.
     *
     * @param context Android context
     * @param url The URL string to open
     * @param toolbarColor Toolbar color for branding (optional)
     */
    fun launchCustomTab(
        context: Context,
        url: String,
        toolbarColor: Int? = null
    ) {
        if (url.isBlank()) {
            Timber.w("Cannot launch Custom Tab with blank URL")
            return
        }

        try {
            val uri = Uri.parse(url)
            if (uri.scheme.isNullOrBlank() || !isValidHttpUrl(uri)) {
                Timber.w("Invalid URL scheme: $url")
                return
            }
            launchCustomTab(context, uri, toolbarColor)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to parse URL: $url")
        }
    }

    /**
     * Builds a CustomTabsIntent with app branding and theming.
     */
    private fun buildCustomTabsIntent(
        context: Context,
        toolbarColor: Int?
    ): CustomTabsIntent {
        val builder = CustomTabsIntent.Builder()

        // Apply toolbar color if provided
        toolbarColor?.let { color ->
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()
            builder.setDefaultColorSchemeParams(colorSchemeParams)
        }

        // Add smooth animations
        builder.setStartAnimations(
            context,
            android.R.anim.fade_in,
            android.R.anim.fade_out,
        )
        builder.setExitAnimations(
            context,
            android.R.anim.fade_in,
            android.R.anim.fade_out,
        )

        // Show URL bar for transparency
        builder.setShowTitle(true)

        return builder.build()
    }

    /**
     * Fallback to default browser if Custom Tabs not available.
     */
    private fun launchFallbackBrowser(
        context: Context,
        uri: Uri
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to launch fallback browser for: $uri")
        }
    }

    /**
     * Validates if the URI has a valid HTTP/HTTPS scheme.
     */
    private fun isValidHttpUrl(uri: Uri): Boolean = uri.scheme?.lowercase() in listOf("http", "https")
}
