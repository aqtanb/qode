package com.qodein.core.analytics

/**
 * No-op implementation of [AnalyticsHelper] that does nothing.
 * Used for tests and previews to avoid analytics tracking.
 */
class NoOpAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(event: AnalyticsEvent) = Unit
}
