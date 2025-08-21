package com.qodein.core.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for providing [AnalyticsHelper] in Compose UI.
 * Defaults to [NoOpAnalyticsHelper] for previews and tests.
 */
val LocalAnalyticsHelper = staticCompositionLocalOf<AnalyticsHelper> {
    NoOpAnalyticsHelper()
}
