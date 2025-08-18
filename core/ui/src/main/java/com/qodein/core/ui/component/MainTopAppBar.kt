package com.qodein.core.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.model.User

/**
 * Model-aware main top app bar with flexible navigation and hardcoded profile/settings actions.
 * Uses QodeTopAppBar design system with center-aligned layout.
 *
 * @param title The title to display
 * @param user User object for profile avatar (null for anonymous users)
 * @param modifier Modifier for styling
 * @param navigationIcon Optional navigation icon (any action)
 * @param onNavigationClick Navigation icon click handler
 * @param onProfileClick Profile action click handler (receives user)
 * @param onSettingsClick Settings action click handler
 * @param showProfile Whether to show profile action (default true)
 * @param showSettings Whether to show settings action (default true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    user: User? = null,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    onProfileClick: ((User?) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    showProfile: Boolean = true,
    showSettings: Boolean = true
) {
    QodeTopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        onNavigationClick = onNavigationClick,
        variant = QodeTopAppBarVariant.CenterAligned,
        modifier = modifier,
        customActions = {
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
