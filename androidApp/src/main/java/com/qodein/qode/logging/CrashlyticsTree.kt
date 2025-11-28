package com.qodein.qode.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Custom Timber tree that sends logs to Firebase Crashlytics.
 * All logs are sent as breadcrumbs to provide context for crash reports.
 * Errors and warnings are additionally recorded as non-fatal exceptions.
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        // Send all logs as breadcrumbs for crash context
        val logMessage = if (tag != null) "[$tag] $message" else message
        crashlytics.log(logMessage)

        // Record exceptions for ERROR and WARN priorities
        if (priority >= Log.WARN) {
            t?.let { crashlytics.recordException(it) }
        }
    }
}
