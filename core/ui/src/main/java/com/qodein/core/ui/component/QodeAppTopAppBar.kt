package com.qodein.core.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.AutoHideConfig
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.shared.model.User

/**
 * Screen types that determine top app bar behavior and appearance
 */
enum class TopAppBarScreenType {
    /** No top app bar shown (submission wizard, onboarding) */
    None,

    /** Main app screens with profile/settings actions */
    Main,

    /** Nested screens with back navigation and transparent background */
    Nested,

    /** Scroll-aware top bar that hides on scroll down */
    ScrollAware
}

/**
 * Unified top app bar component for the entire Qode application.
 * Handles all screen types and behaviors in one consistent component.
 *
 * @param title Screen title to display
 * @param screenType Type of screen determining behavior and appearance
 * @param user Current user for profile avatar (null for unauthenticated)
 * @param modifier Modifier for styling
 * @param navigationIcon Optional navigation icon (typically back arrow or menu)
 * @param onNavigationClick Navigation icon click handler
 * @param actions Additional action buttons
 * @param onProfileClick Profile avatar click handler
 * @param onSettingsClick Settings action click handler
 * @param showProfile Whether to show profile action (default true for Main screens)
 * @param showSettings Whether to show settings action (default true for Main screens)
 * @param scrollState Required for ScrollAware screen type
 * @param autoHideConfig Configuration for scroll-aware behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeAppTopAppBar(
    title: String,
    screenType: TopAppBarScreenType,
    modifier: Modifier = Modifier,
    user: User? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onProfileClick: ((User?) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    showProfile: Boolean = true,
    showSettings: Boolean = true,
    scrollState: ScrollState? = null,
    autoHideConfig: AutoHideConfig = AutoHideConfig.Default
) {
    when (screenType) {
        TopAppBarScreenType.None -> {
            // No top bar rendered
        }

        TopAppBarScreenType.Main -> {
            MainTopAppBar(
                title = title,
                user = user,
                modifier = modifier,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick,
                showProfile = showProfile,
                showSettings = showSettings,
            )
        }

        TopAppBarScreenType.Nested -> {
            QodeTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                customActions = actions,
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIconTint = MaterialTheme.colorScheme.onSurface,
                titleColor = MaterialTheme.colorScheme.onSurface,
                actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = modifier,
            )
        }

        TopAppBarScreenType.ScrollAware -> {
            if (scrollState != null) {
                // Traditional ScrollAware with internal auto-hiding
                val autoHidingState = rememberAutoHidingState(
                    scrollState = scrollState,
                    config = autoHideConfig,
                )

                AutoHidingContent(
                    state = autoHidingState,
                    direction = AutoHideDirection.DOWN,
                    modifier = modifier,
                ) {
                    QodeTopAppBar(
                        title = title,
                        navigationIcon = navigationIcon,
                        onNavigationClick = onNavigationClick,
                        variant = QodeTopAppBarVariant.Transparent,
                        statusBarPadding = true,
                        navigationIconTint = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurface,
                        actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        customActions = {
                            actions?.invoke(this)
                            if (showProfile && onProfileClick != null) {
                                IconButton(onClick = { onProfileClick(user) }) {
                                    ProfileAvatar(
                                        user = user,
                                        size = SizeTokens.Icon.sizeXLarge,
                                        contentDescription = "Profile",
                                    )
                                }
                            }
                            if (showSettings && onSettingsClick != null) {
                                IconButton(onClick = onSettingsClick) {
                                    Icon(
                                        imageVector = QodeNavigationIcons.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                    )
                }
            } else {
                // ScrollAware with external auto-hiding - just render the transparent top bar
                QodeTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    onNavigationClick = onNavigationClick,
                    variant = QodeTopAppBarVariant.Transparent,
                    statusBarPadding = true,
                    navigationIconTint = MaterialTheme.colorScheme.onSurface,
                    titleColor = MaterialTheme.colorScheme.onSurface,
                    actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    backgroundColor = Color.Transparent,
                    customActions = {
                        actions?.invoke(this)
                        if (showProfile && onProfileClick != null) {
                            IconButton(onClick = { onProfileClick(user) }) {
                                ProfileAvatar(
                                    user = user,
                                    size = SizeTokens.Icon.sizeXLarge,
                                    contentDescription = "Profile",
                                )
                            }
                        }
                        if (showSettings && onSettingsClick != null) {
                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    imageVector = QodeNavigationIcons.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}
