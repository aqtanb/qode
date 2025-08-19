package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import com.qodein.core.domain.AuthState
import com.qodein.core.model.User
import com.qodein.core.ui.component.QodeAppTopAppBar
import com.qodein.core.ui.component.TopAppBarScreenType
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.qode.ui.state.TopBarConfig

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
            QodeAppTopAppBar(
                title = "", // ScrollAware screens typically have no title
                screenType = TopAppBarScreenType.ScrollAware,
                user = (authState as? AuthState.Authenticated)?.user,
                navigationIcon = null, // Could be customized if needed
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
                scrollState = appState.profileScrollState,
            )
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

        is TopBarConfig.Custom -> {
            // Render completely custom content
            config.content()
        }
    }
}

/**
 * Convenience composable for main screens that need profile/settings
 */
@Composable
fun AppTopBarWithProfile(
    config: TopBarConfig,
    appState: QodeAppState,
    authState: AuthState,
    onEvent: (AppUiEvents) -> Unit
) {
    AppTopBarContainer(
        config = config,
        appState = appState,
        authState = authState,
        onEvent = onEvent,
        showProfile = true,
        showSettings = true,
    )
}

/**
 * Convenience composable for nested screens (no profile/settings)
 */
@Composable
fun AppTopBarBasic(
    config: TopBarConfig,
    appState: QodeAppState,
    authState: AuthState,
    onEvent: (AppUiEvents) -> Unit
) {
    AppTopBarContainer(
        config = config,
        appState = appState,
        authState = authState,
        onEvent = onEvent,
        showProfile = false,
        showSettings = false,
    )
}
