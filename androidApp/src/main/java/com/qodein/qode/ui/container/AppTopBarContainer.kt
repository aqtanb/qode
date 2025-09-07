package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.ui.component.QodeAppTopAppBar
import com.qodein.core.ui.component.TopAppBarScreenType
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.qode.ui.state.TopBarConfig
import com.qodein.shared.domain.AuthState
import com.qodein.shared.model.User

/**
 * Hybrid top bar container that renders different configurations.
 *
 * Handles the smart defaults system:
 * - None: No rendering
 * - ScrollAware: Transparent/scroll-aware for immersive screens
 * - Basic: Standard title + back button (most nested screens)
 * - Custom: Completely custom content
 */
@Composable
fun AppTopBarContainer(
    config: TopBarConfig,
    appState: QodeAppState,
    authState: AuthState,
    onEvent: (AppUiEvents) -> Unit,
    // Optional overrides for main screens that need profile/settings
    showProfile: Boolean = false,
    showSettings: Boolean = false,
    onProfileClick: ((User?) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    when (config) {
        TopBarConfig.None -> {
            // No top bar rendered
        }

        TopBarConfig.ScrollAware -> {
            // Get the current scroll state from app state
            val currentScrollableState by appState.currentScrollableState

            // Create the auto-hiding state using the generic utility function
            val autoHidingState = rememberAutoHidingState(scrollableState = currentScrollableState)

            if (autoHidingState != null) {
                // Use the generic wrapper for auto-hiding animation
                AutoHidingContent(
                    state = autoHidingState,
                    direction = AutoHideDirection.DOWN,
                ) {
                    QodeAppTopAppBar(
                        title = "",
                        screenType = TopAppBarScreenType.ScrollAware,
                        user = (authState as? AuthState.Authenticated)?.user,
                        navigationIcon = QodeActionIcons.Back,
                        onNavigationClick = {
                            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateBack))
                        },
                        onProfileClick = onProfileClick ?: { _ ->
                            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToProfile))
                        },
                        onSettingsClick = onSettingsClick ?: {
                            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToSettings))
                        },
                        showProfile = showProfile,
                        showSettings = showSettings,
                        scrollState = null, // Scroll handling is done by the wrapper
                    )
                }
            } else {
                // Fallback: always visible transparent top bar if no scrollable content
                QodeAppTopAppBar(
                    title = "",
                    screenType = TopAppBarScreenType.Nested,
                    user = (authState as? AuthState.Authenticated)?.user,
                    navigationIcon = QodeActionIcons.Back,
                    onNavigationClick = {
                        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateBack))
                    },
                    onProfileClick = onProfileClick ?: { _ ->
                        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToProfile))
                    },
                    onSettingsClick = onSettingsClick ?: {
                        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToSettings))
                    },
                    showProfile = showProfile,
                    showSettings = showSettings,
                    scrollState = null,
                )
            }
        }

        is TopBarConfig.MainWithTitle -> {
            QodeAppTopAppBar(
                title = config.title,
                screenType = TopAppBarScreenType.Main,
                user = (authState as? AuthState.Authenticated)?.user,
                navigationIcon = null, // No back button for main screens
                onNavigationClick = null,
                onProfileClick = onProfileClick ?: { _ ->
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToProfile))
                },
                onSettingsClick = onSettingsClick ?: {
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToSettings))
                },
                showProfile = showProfile,
                showSettings = showSettings,
                scrollState = null,
                // TODO: Handle subtitle and custom actions when QodeAppTopAppBar supports them
            )
        }

        is TopBarConfig.Basic -> {
            QodeAppTopAppBar(
                title = config.title,
                screenType = TopAppBarScreenType.Nested,
                user = (authState as? AuthState.Authenticated)?.user,
                navigationIcon = config.navigationIcon,
                onNavigationClick = {
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateBack))
                },
                actions = config.actions,
                onProfileClick = onProfileClick ?: { _ ->
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToProfile))
                },
                onSettingsClick = onSettingsClick ?: {
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToSettings))
                },
                showProfile = showProfile,
                showSettings = showSettings,
                scrollState = null,
            )
        }

        is TopBarConfig.Custom -> {
            // Render completely custom content
            config.content()
        }
    }
}
