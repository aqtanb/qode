package com.qodein.qode.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.qode.R
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState

/**
 * Provides TopBarConfig based on current app state.
 *
 * This centralizes the logic for determining which top bar configuration
 * each screen should use, while keeping the flexibility of the hybrid system.
 */
object TopBarConfigProvider {

    /**
     * Get the appropriate TopBarConfig based on current app state.
     *
     * Default: TopBarConfig.None (most screens handle their own top bars)
     * Only explicitly configured screens get app-level top bars
     */
    @Composable
    fun getTopBarConfig(appState: QodeAppState): TopBarConfig {
        val currentDestination = appState.currentTopLevelDestination

        return when {
            currentDestination == TopLevelDestination.FEED -> {
                TopBarConfig.MainWithTitle(
                    title = stringResource(R.string.feed_title),
                )
            }

            appState.isSettingsScreen -> {
                TopBarConfig.Basic(title = stringResource(R.string.settings))
            }

            appState.isPromocodeSubmissionScreen -> {
                TopBarConfig.Basic(title = "Submit Promo Code")
            }

            else -> TopBarConfig.None
        }
    }
}

/**
 * Extension function for easy access from composables
 */
@Composable
fun QodeAppState.getTopBarConfig(): TopBarConfig = TopBarConfigProvider.getTopBarConfig(this)
