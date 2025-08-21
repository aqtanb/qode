package com.qodein.core.analytics

import co.touchlab.kermit.Logger
import javax.inject.Inject

/**
 * Stub implementation of [AnalyticsHelper] that logs events to console.
 * Used for debug builds to avoid sending analytics to production.
 */
class StubAnalyticsHelper @Inject constructor() : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        Logger.d { "Analytics Event: ${event.type}, Extras: ${event.extras}" }
    }
}
