package com.qodein.qode

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.LocalAnalyticsHelper
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.util.LocaleManager
import com.qodein.qode.ui.QodeApp
import com.qodein.qode.ui.rememberQodeAppState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
