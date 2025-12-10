package com.qodein.qode.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        val logMessage = buildString {
            append("[")
            append(priorityToString(priority))
            append("] ")
            if (tag != null) {
                append(tag)
                append(": ")
            }
            append(message)
        }

        crashlytics.log(logMessage)

        if (t != null && priority >= Log.WARN) {
            crashlytics.recordException(t)
        }
    }

    private fun priorityToString(priority: Int): String =
        when (priority) {
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
}
