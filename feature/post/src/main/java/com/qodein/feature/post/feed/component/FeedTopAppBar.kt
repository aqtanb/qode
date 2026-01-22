package com.qodein.feature.post.feed.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.shared.model.User

/**
 * Top app bar for the Feed screen.
 *
 * Displays:
 * - Centered "Feed" title
 * - Profile avatar button (navigates to profile)
 * - Settings icon button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedTopAppBar(
    user: User?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinTopAppBar(
        title = stringResource(R.string.ui_feed),
        navigationIcon = null,
        modifier = modifier,
        actions = {
            IconButton(onClick = onProfileClick, modifier = Modifier.size(SizeTokens.IconButton.sizeLarge)) {
                ProfileAvatar(
                    user = user,
                    size = SizeTokens.Icon.sizeXLarge,
                    contentDescription = stringResource(R.string.ui_profile),
                )
            }

            IconButton(onClick = onSettingsClick, modifier = Modifier.size(SizeTokens.IconButton.sizeLarge)) {
                Icon(
                    imageVector = NavigationIcons.Settings,
                    contentDescription = stringResource(R.string.ui_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            }
        },
    )
}
