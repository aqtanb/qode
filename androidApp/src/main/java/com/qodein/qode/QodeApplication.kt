package com.qodein.qode

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.qodein.qode.logging.KermitTimberWriter
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Qode.
 *
 * Firebase initialization is handled by FirebaseInitializer via androidx.startup
 * to ensure it's initialized before any Dagger/Hilt components are created.
 */
@HiltAndroidApp
class QodeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    private fun initializeLogging() {
        val isDebug = BuildConfig.DEBUG

        // Initialize Timber for Android-specific logging
        if (isDebug) {
            // Debug builds: Enable all logs with detailed tree
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    // Add line numbers for easier debugging
                    return "${super.createStackElementTag(element)}:${element.lineNumber}"
                }
            })
        } else {
            // Release builds: Only plant trees for crashes/errors
            // TODO: Add Crashlytics logging tree when Firebase Analytics is added
            Timber.plant(object : Timber.Tree() {
                override fun log(
                    priority: Int,
                    tag: String?,
                    message: String,
                    t: Throwable?
                ) {
                    // In release builds, only log errors and crashes
                    if (priority >= android.util.Log.ERROR) {
                        // TODO: Send to Crashlytics
                    }
                }
            })
        }

        // Configure Kermit for shared module logging, bridged to Timber
        Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Error)
        Logger.setLogWriters(KermitTimberWriter()) // Bridge shared module logs to Timber

        Timber.d("Logging initialized: Kermit -> Timber bridge active")
    }
}
