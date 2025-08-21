package com.qodein.core.analytics

/**
 * Interface for logging analytics events following NIA patterns.
 */
interface AnalyticsHelper {
    /**
     * Log an analytics event with the given event data.
     */
    fun logEvent(event: AnalyticsEvent)
}
