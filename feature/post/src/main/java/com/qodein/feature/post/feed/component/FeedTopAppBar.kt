package com.qodein.feature.post.feed.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.feature.post.R
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
    QodeTopAppBar(
        title = stringResource(R.string.feed_title),
        navigationIcon = null,
        onNavigationClick = null,
        variant = QodeTopAppBarVariant.CenterAligned,
        modifier = modifier,
        customActions = {
            FeedAppBarActions(
                user = user,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick,
            )
        },
    )
}

@Composable
private fun RowScope.FeedAppBarActions(
    user: User?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    IconButton(onClick = onProfileClick) {
        ProfileAvatar(
            user = user,
            size = SizeTokens.Icon.sizeXLarge,
            contentDescription = stringResource(R.string.cd_profile),
        )
    }

    IconButton(onClick = onSettingsClick) {
        Icon(
            imageVector = QodeNavigationIcons.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
