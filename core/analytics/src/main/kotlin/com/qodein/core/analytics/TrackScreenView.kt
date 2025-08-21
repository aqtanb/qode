package com.qodein.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Composable function that tracks screen view events.
 * Based on Now in Android patterns.
 *
 * @param screenName The name of the screen being viewed
 * @param screenClass Optional screen class for additional context
 */
@Composable
fun TrackScreenViewEvent(
    screenName: String,
    screenClass: String? = null,
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current
) {
    DisposableEffect(screenName) {
        analyticsHelper.logScreenView(screenName, screenClass)
        onDispose { }
    }
}
