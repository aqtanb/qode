package com.qodein.qode.ui.state

import android.R.attr.contentDescription
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.icon.QodeActionIcons
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
        val isSearchDestination = currentDestination == TopLevelDestination.SEARCH
        val isInboxDestination = currentDestination == TopLevelDestination.INBOX
        val isProfileScreen = appState.isProfileScreen
        val isAuthScreen = appState.isAuthScreen

        return when {
            // Home screen - no top bar
            isHomeDestination -> TopBarConfig.None

            // Profile and auth screens - transparent/scroll-aware
            isProfileScreen || isAuthScreen -> TopBarConfig.ScrollAware

            // Top level destinations - handle ALL of them explicitly
            isSearchDestination -> {
                TopBarConfig.MainWithTitle(
                    title = stringResource(R.string.search_title),
                    actions = {
                        // Favorites button for search screen
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

            isInboxDestination -> {
                TopBarConfig.MainWithTitle(title = stringResource(R.string.inbox_title))
            }

            // Nested screens - only when NOT on top-level destinations
            currentDestination == null -> {
                when {
                    appState.isSubmissionScreen -> {
                        // TODO: Implement proper dynamic titles per wizard step
                        // The wizard has 4 steps that should show different titles:
                        // Step 1: "Select Service" (SERVICE_AND_TYPE)
                        // Step 2: "Enter Details" (TYPE_DETAILS)
                        // Step 3: "Set Dates" (DATE_SETTINGS)
                        // Step 4: "Final Details" (OPTIONAL_DETAILS)
                        // Currently using generic title since wizard step state is not accessible here
                        TopBarConfig.Basic(title = "Create Promo Code")
                    }
                    appState.isSettingsScreen -> {
                        TopBarConfig.Basic(title = stringResource(R.string.settings))
                    }
                    appState.isPromocodeDetailScreen -> {
                        TopBarConfig.Basic(
                            title = "Promocode Details",
                            actions = {
                                IconButton(
                                    onClick = { /* TODO: Handle bookmark/favorite click */ },
                                ) {
                                    Icon(
                                        imageVector = QodeActionIcons.Bookmark,
                                        contentDescription = "Bookmark promocode",
                                    )
                                }
                            },
                        )
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
