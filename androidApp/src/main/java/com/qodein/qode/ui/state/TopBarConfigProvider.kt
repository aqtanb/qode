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

            // Auth screens - transparent/scroll-aware
            isAuthScreen -> TopBarConfig.ScrollAware

            // Top level destinations - handle ALL of them explicitly
            isFeedDestination -> {
                TopBarConfig.MainWithTitle(
                    title = stringResource(R.string.feed_title),
                    actions = {
                        // Favorites button for feed screen
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

            // Nested screens - only when NOT on top-level destinations
            currentDestination == null -> {
                when {
                    appState.isSubmissionScreen -> {
                        // TODO: Implement proper dynamic titles per wizard step
                        // The wizard has 2 steps that should show different titles:
                        // Step 1: "Core Details" (CORE_DETAILS)
                        // Step 2: "Set Dates" (DATE_SETTINGS)
                        // Currently using generic title since wizard step state is not accessible here
                        TopBarConfig.Basic(title = "Create Promo Code")
                    }
                    appState.isSettingsScreen -> {
                        TopBarConfig.Basic(title = stringResource(R.string.settings))
                    }
                    appState.isPromocodeDetailScreen -> {
                        // Screen handles its own top bar to avoid architecture coupling
                        TopBarConfig.None
                    }
                    else -> {
                        // During startup, navigation isn't fully initialized yet
                        // App starts on Home screen, so return None during this phase
                        TopBarConfig.None
                    }
                }
            }

            // Fallback
            else -> TopBarConfig.Basic(title = "")
        }
    }

    /**
     * Determine if the current screen should show profile/settings buttons
     */
    @Composable
    fun shouldShowProfile(appState: QodeAppState): Boolean =
        when {
            appState.isProfileScreen -> false // Don't show profile on profile screen
            appState.isAuthScreen -> false // Don't show profile on auth screen
            appState.isNestedScreen -> false // Don't show on nested screens (like settings)
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
