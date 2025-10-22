package com.qodein.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeinIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.User
import com.qodein.shared.model.UserStats

@Composable
fun ProfileScreen(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    TrackScreenViewEvent(screenName = "Profile")

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.EditProfileRequested -> {}
                is ProfileEvent.SignedOut -> onSignOut()
                is ProfileEvent.LeaderboardRequested -> {}
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val currentState = uiState) {
            is ProfileUiState.Success -> {
                ProfileContent(
                    user = currentState.user,
                    scrollState = scrollState,
                    onAction = viewModel::handleAction,
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ProfileUiState.Loading -> {
                val loadingDescription = stringResource(R.string.profile_loading_description)
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(SpacingTokens.lg)
                        .semantics {
                            contentDescription = loadingDescription
                        },
                )
            }

            is ProfileUiState.Error -> {
                QodeErrorCard(
                    error = currentState.errorType,
                    onRetry = { viewModel.handleAction(ProfileAction.RetryClicked) },
                    modifier = Modifier
                        .padding(SpacingTokens.lg),
                )
            }
        }
    }
}

// MARK: - Success Content

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user: User,
    scrollState: ScrollState,
    onAction: (ProfileAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local animation state - this composable controls its own animations
    var isContentVisible by remember { mutableStateOf(false) }

    // Trigger animations when this composable enters the composition
    LaunchedEffect(Unit) {
        isContentVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Spacer(modifier = Modifier.height(SpacingTokens.huge))

            AnimatedProfileHeader(
                user = user,
                onAction = onAction,
                isVisible = isContentVisible,
            )

            AnimatedActionsSection(
                onAction = onAction,
                isVisible = isContentVisible,
            )

            AnimatedSignOutButton(
                onAction = onAction,
                isVisible = isContentVisible,
            )
        }

        // Beautiful self-contained transparent top app bar with auto-hiding
        val autoHidingState = rememberAutoHidingState(scrollState = scrollState)
        AutoHidingContent(
            state = autoHidingState,
            direction = AutoHideDirection.DOWN,
        ) {
            QodeTopAppBar(
                title = "",
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onBackClick,
                variant = QodeTopAppBarVariant.Transparent,
                statusBarPadding = true,
            )
        }
    }
}

@Composable
private fun AnimatedProfileHeader(
    user: User,
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(animationSpec = tween(600)),
    ) {
        ProfileHeader(
            user = user,
            onAction = onAction,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AnimatedActionsSection(
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(animationSpec = tween(1000, 400)),
    ) {
        LeaderboardCard(
            onAction = onAction,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LeaderboardCard(
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        ActivityCard(
            title = "Leaderboard",
            content = "Here is the leaderboard, see how you rank!",
            icon = QodeinIcons.Leaderboard,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { onAction(ProfileAction.LeaderboardClicked) },
        )
    }
}

@Composable
private fun ActivityCard(
    title: String,
    content: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier,
            )
        }
    }
}

// MARK: Sign Out

@Composable
private fun AnimatedSignOutButton(
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)),
    ) {
        SignOutButton(
            onAction = onAction,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SignOutButton(
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeButton(
        text = stringResource(R.string.profile_sign_out_button),
        onClick = { onAction(ProfileAction.SignOutClicked) },
        size = ButtonSize.Large,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
            .fillMaxWidth()
            .testTag("sign_out_button"),
    )
}

// MARK: Header

@Composable
internal fun ProfileHeader(
    user: User,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        ProfileAvatar(
            user = user,
            size = SizeTokens.Avatar.sizeXLarge,
            modifier = Modifier.testTag("profile_avatar"),
        )
        UserInfo(user = user)
        UserStatsRow(userStats = user.stats, modifier = Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun UserInfo(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        UserName(
            firstName = user.profile.firstName,
            lastName = user.profile.lastName,
        )
    }
}

@Composable
private fun UserName(
    firstName: String,
    lastName: String?,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (lastName != null) {
            stringResource(R.string.profile_full_name_format, firstName, lastName)
        } else {
            firstName
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun UserStatsRow(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem(
            value = userStats.submittedPromocodesCount,
            contentDescription = stringResource(R.string.profile_promocodes_label),
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .width(ShapeTokens.Border.thin)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline),
        )

        StatItem(
            value = userStats.submittedPostsCount,
            contentDescription = stringResource(R.string.posts),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    value: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
        modifier = modifier,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = contentDescription,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileHeaderPreview() {
    QodeTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            ProfileHeader(
                user = UserPreviewData.powerUser,
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LeaderboardCardPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(SpacingTokens.lg),
        ) {
            LeaderboardCard(onAction = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileContentPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ProfileContent(
                user = UserPreviewData.powerUser,
                scrollState = rememberScrollState(),
                onAction = {},
                onBackClick = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileLoadingPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val loadingDescription = stringResource(R.string.profile_loading_description)
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(SpacingTokens.lg)
                        .semantics {
                            contentDescription = loadingDescription
                        },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileErrorPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                QodeErrorCard(
                    error = SystemError.Offline,
                    onRetry = {},
                    modifier = Modifier.padding(SpacingTokens.lg),
                )
            }
        }
    }
}
