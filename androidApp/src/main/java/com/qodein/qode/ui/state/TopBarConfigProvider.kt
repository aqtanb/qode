package com.qodein.qode.ui.state

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.icon.QodeNavigationIcons
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
     * Get the appropriate TopBarConfig based on current app state
     */
    @Composable
    fun getTopBarConfig(appState: QodeAppState): TopBarConfig {
        val currentDestination = appState.currentTopLevelDestination
        val isHomeDestination = currentDestination == TopLevelDestination.HOME
        val isFeedDestination = currentDestination == TopLevelDestination.FEED
        val isProfileScreen = appState.isProfileScreen
        val isAuthScreen = appState.isAuthScreen

        return when {
            // Home screen - no top bar
            isHomeDestination -> TopBarConfig.None

            // Profile screen - no app-level top bar (screen handles its own beautiful transparent one)
            isProfileScreen -> TopBarConfig.None

            // Auth screens - no app-level top bar (screen handles its own)
            isAuthScreen -> TopBarConfig.None

            // Top level destinations - handle ALL of them explicitly
            isFeedDestination -> {
                TopBarConfig.MainWithTitle(
                    title = stringResource(R.string.feed_title),
                    actions = {
                        IconButton(
                            onClick = { /* TODO: Handle favorites click */ },
                        ) {
                            Icon(
                                imageVector = QodeNavigationIcons.Favorites,
                                contentDescription = stringResource(R.string.favorites),
                            )
                        }
                    },
                )
            }

            currentDestination == null -> {
                when {
                    appState.isSubmissionScreen -> {
                        TopBarConfig.Basic(title = "Submit Promo Code")
                    }
                    appState.isSettingsScreen -> {
                        TopBarConfig.Basic(title = stringResource(R.string.settings))
                    }
                    else -> {
                        TopBarConfig.None
                    }
                }
            }

            else -> TopBarConfig.Basic(title = "")
        }
    }

    /**
     * Determine if the current screen should show profile/settings buttons
     */
    @Composable
    fun shouldShowProfile(appState: QodeAppState): Boolean =
        when {
            appState.isNestedScreen -> false
            else -> true // Show on main screens
        }

    /**
     * Determine if the current screen should show settings button
     */
    @Composable
    fun shouldShowSettings(appState: QodeAppState): Boolean = shouldShowProfile(appState) && !appState.isSettingsScreen
}

/**
 * Extension functions for easy access from composables
 */
@Composable
fun QodeAppState.getTopBarConfig(): TopBarConfig = TopBarConfigProvider.getTopBarConfig(this)

@Composable
fun QodeAppState.shouldShowProfile(): Boolean = TopBarConfigProvider.shouldShowProfile(this)

@Composable
fun QodeAppState.shouldShowSettings(): Boolean = TopBarConfigProvider.shouldShowSettings(this)
