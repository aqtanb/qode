package com.qodein.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

/**
 * Implementation of [AnalyticsHelper] using Firebase Analytics.
 */
class FirebaseAnalyticsHelper(private val firebaseAnalytics: FirebaseAnalytics) : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.type) {
            for (extra in event.extras) {
                param(extra.key, extra.value)
            }
        }
    }
}
