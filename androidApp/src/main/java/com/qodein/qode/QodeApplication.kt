package com.qodein.qode

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.qodein.core.analytics.di.analyticsModule
import com.qodein.core.data.di.coreDataModule
import com.qodein.core.ui.di.coreUiModule
import com.qodein.feature.auth.di.authModule
import com.qodein.feature.block.di.blockModule
import com.qodein.feature.report.di.reportModule
import com.qodein.qode.di.appModule
import com.qodein.qode.logging.CrashlyticsTree
import com.qodein.qode.logging.KermitTimberWriter
import com.qodein.shared.data.di.sharedDataModule
import com.qodein.shared.domain.di.domainModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for Qode.
 *
 * Firebase initialization is handled by FirebaseInitializer via androidx.startup
 * to ensure it's initialized before any Dagger/Hilt components are created.
 */
@HiltAndroidApp
class QodeApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        initializeKoin()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@QodeApplication)
            modules(
                appModule,
                coreDataModule,
                coreUiModule,
                analyticsModule,
                sharedDataModule,
                domainModule,
                authModule,
                blockModule,
                reportModule,
            )
        }
        Timber.d("Koin initialized")
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
            // Release builds: Send logs to Crashlytics for production monitoring
            Timber.plant(CrashlyticsTree())
        }

        // Configure Kermit for shared module logging, bridged to Timber
        Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Error)
        Logger.setLogWriters(KermitTimberWriter()) // Bridge shared module logs to Timber

        Timber.d("Logging initialized: Kermit -> Timber bridge active")
    }
}
