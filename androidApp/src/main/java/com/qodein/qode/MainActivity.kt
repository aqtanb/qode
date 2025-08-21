package com.qodein.qode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
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
            val darkTheme = uiState.shouldUseDarkTheme(isSystemInDarkTheme())

            // Apply language changes immediately when state changes
            LaunchedEffect(uiState) {
                val currentState = uiState
                if (currentState is MainActivityUiState.Success) {
                    LocaleManager.setAppLocale(this@MainActivity, currentState.language)
                }
            }

            QodeTheme(
                darkTheme = darkTheme,
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
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
