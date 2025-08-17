package com.qodein.qode.ui.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.component.QodeProfileAvatar
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Compact screen-specific top app bar with navigation actions
 * Layout: Favorites (left) | Screen Title (center) | Profile + Settings (right)
 *
 * Note: This component uses CenterAlignedTopAppBar directly because it has unique requirements
 * for a custom profile avatar in the actions area that doesn't fit the standard TopAppBarAction pattern.
 * Future enhancement: Extend QodeTopAppBar to support custom actions composables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onFavoritesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    profileImageUrl: String? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onFavoritesClick) {
                Icon(
                    imageVector = QodeNavigationIcons.Favorites,
                    contentDescription = "Favorites",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            // Profile Avatar Button
            IconButton(onClick = onProfileClick) {
                QodeProfileAvatar(
                    imageUrl = profileImageUrl,
                    size = SizeTokens.Icon.sizeXLarge,
                    contentDescription = "Profile",
                )
            }

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = QodeNavigationIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
