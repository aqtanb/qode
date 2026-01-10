package com.qodein.qode

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.totalBytesToDownload
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.LocalAnalyticsHelper
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.util.LocaleManager
import com.qodein.qode.ui.QodeApp
import com.qodein.qode.ui.rememberQodeAppState
import com.qodein.shared.common.Result
import com.qodein.shared.domain.repository.AppUpdateConfigRepository
import com.qodein.shared.model.AppUpdateConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val appUpdateConfigRepository: AppUpdateConfigRepository by inject()
    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private var updateListener: InstallStateUpdatedListener? = null

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Timber.d("Update flow failed. Result code: %d", result.resultCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdates()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Don't render UI until preferences are loaded to prevent theme flashing
            if (uiState.shouldKeepSplashScreen()) return@setContent

            val darkTheme = uiState.shouldUseDarkTheme(isSystemInDarkTheme())

            LaunchedEffect(uiState) {
                val currentState = uiState
                if (currentState is MainActivityUiState.Success) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        LocaleManager.setAppLocale(this@MainActivity, currentState.language)
                    }
                }
            }

            QodeTheme(
                darkTheme = darkTheme,
            ) {
                CompositionLocalProvider(
                    LocalAnalyticsHelper provides analyticsHelper,
                ) {
                    QodeApp(
                        appState = rememberQodeAppState(),
                        modifier = Modifier,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForStalledUpdates()
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            val config = when (val configResult = appUpdateConfigRepository.getAppUpdateConfig()) {
                is Result.Success -> configResult.data
                is Result.Error -> {
                    Timber.w("Failed to fetch update config: ${configResult.error}, using defaults")
                    AppUpdateConfig()
                }
            }

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    handleUpdateAvailable(appUpdateInfo, config)
                }
            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to check for update availability")
            }
        }
    }

    private fun handleUpdateAvailable(
        appUpdateInfo: AppUpdateInfo,
        config: AppUpdateConfig
    ) {
        val currentVersion = BuildConfig.VERSION_CODE
        val staleDays = appUpdateInfo.clientVersionStalenessDays() ?: 0

        when {
            currentVersion < config.minimumVersionCode -> {
                Timber.i("Forcing IMMEDIATE update: current=$currentVersion, minimum=${config.minimumVersionCode}")
                startImmediateUpdate(appUpdateInfo)
            }

            staleDays >= config.flexibleUpdateStaleDays -> {
                Timber.i("Starting FLEXIBLE update: staleDays=$staleDays, minimum=${config.flexibleUpdateStaleDays}")
                startFlexibleUpdate(appUpdateInfo)
            }

            else -> {
                Timber.d("Update available, but not urgent yet")
            }
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
        )
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        updateListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val progress = (state.bytesDownloaded * 100 / state.totalBytesToDownload).toInt()
                    Timber.d("Downloading update: %d%%", progress)
                }
                InstallStatus.DOWNLOADED -> {
                    Timber.i("Update downloaded, installing automatically")
                    appUpdateManager.completeUpdate()
                    updateListener?.let { appUpdateManager.unregisterListener(it) }
                }
                InstallStatus.FAILED -> {
                    Timber.e("Update download failed: errorCode=%d", state.installErrorCode())
                    updateListener?.let { appUpdateManager.unregisterListener(it) }
                }
                else -> {
                    Timber.d("Update install status: %s", state.installStatus())
                }
            }
        }

        updateListener?.let { appUpdateManager.registerListener(it) }
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }

    private fun checkForStalledUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            // Resume immediate update if user backed out
            if (appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                Timber.w("Resuming stalled immediate update")
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }

            // Auto-install if flexible update already downloaded
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Timber.i("Update already downloaded, installing now")
                appUpdateManager.completeUpdate()
            }
        }
    }
}
