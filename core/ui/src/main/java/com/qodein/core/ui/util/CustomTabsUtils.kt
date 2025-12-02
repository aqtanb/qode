package com.qodein.core.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import timber.log.Timber

/**
 * Utility functions for launching URLs with deep-link awareness and Custom Tabs.
 */
object CustomTabsUtils {

    /**
     * Launches a URL with basic deep-link handling:
     * - http/https: opens Custom Tab
     * - other schemes: generic VIEW intent (lets the OS route to installed app or browser)
     */
    fun launchSmartUrl(
        context: Context,
        url: String,
        toolbarColor: Int? = null
    ) {
        if (url.isBlank()) {
            Timber.w("Cannot launch with blank URL")
            return
        }

        val uri = try {
            Uri.parse(url)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to parse URL: %s", url)
            return
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme == "http" || scheme == "https") {
            launchCustomTab(context, uri, toolbarColor)
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (exception: ActivityNotFoundException) {
                Timber.w(exception, "No handler for %s", url)
            } catch (exception: Exception) {
                Timber.w(exception, "Failed to launch VIEW intent for %s", url)
            }
        }
    }

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

        toolbarColor?.let { color ->
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()
            builder.setDefaultColorSchemeParams(colorSchemeParams)
        }

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

        builder.setShowTitle(true)

        return builder.build()
    }

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

    private fun isValidHttpUrl(uri: Uri): Boolean = uri.scheme?.lowercase() in listOf("http", "https")
}
