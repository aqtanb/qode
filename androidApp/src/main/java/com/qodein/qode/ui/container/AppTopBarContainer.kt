package com.qodein.qode.ui.container

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.qode.ui.state.TopBarConfig
import com.qodein.shared.domain.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarContainer(
    config: TopBarConfig,
    appState: QodeAppState,
    authState: AuthState,
    onEvent: (AppUiEvents) -> Unit
) {
    when (config) {
        TopBarConfig.None -> {
            // No top bar rendered
        }

        is TopBarConfig.MainWithTitle -> {
            val user = (authState as? AuthState.Authenticated)?.user

            QodeTopAppBar(
                title = config.title,
                navigationIcon = null,
                onNavigationClick = null,
                variant = QodeTopAppBarVariant.CenterAligned,
                customActions = {
                    config.actions?.invoke(this)

                    IconButton(onClick = {
                        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToProfile))
                    }) {
                        ProfileAvatar(
                            user = user,
                            size = SizeTokens.Icon.sizeXLarge,
                            contentDescription = "Profile",
                        )
                    }

                    IconButton(onClick = {
                        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToSettings))
                    }) {
                        Icon(
                            imageVector = QodeNavigationIcons.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }

        is TopBarConfig.Basic -> {
            QodeTopAppBar(
                title = config.title,
                navigationIcon = config.navigationIcon,
                onNavigationClick = {
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateBack))
                },
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIconTint = MaterialTheme.colorScheme.onSurface,
                titleColor = MaterialTheme.colorScheme.onSurface,
                actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.surface,
                customActions = config.actions,
            )
        }

        is TopBarConfig.Custom -> {
            config.content()
        }
    }
}
