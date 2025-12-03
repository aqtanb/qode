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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.ShimmerCircle
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.icon.QodeinIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.feature.profile.component.ProfileTopAppBar
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.User
import com.qodein.shared.model.UserStats

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Profile")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.SignedOut -> onSignOut()
                is ProfileEvent.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    ProfileScreen(
        onBackClick = onBackClick,
        onAction = viewModel::onAction,
        uiState = uiState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    onBackClick: () -> Unit,
    onAction: (ProfileAction) -> Unit,
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileTopAppBar(
                scrollState = scrollState,
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingTokens.lg),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is ProfileUiState.Success -> {
                    ProfileContent(
                        user = uiState.user,
                        scrollState = scrollState,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is ProfileUiState.Loading -> {
                    ProfileLoadingState()
                }

                is ProfileUiState.Error -> {
                    QodeErrorCard(
                        error = uiState.errorType,
                        onRetry = { onAction(ProfileAction.RetryClicked) },
                    )
                }
            }
        }
    }
}

// MARK: - Success Content

@Composable
private fun ProfileContent(
    user: User,
    scrollState: ScrollState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isContentVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.huge))

        AnimatedProfileHeader(
            user = user,
            onAction = onAction,
            isVisible = isContentVisible,
        )

        AnimatedSignOutButton(
            onAction = onAction,
            isVisible = isContentVisible,
        )
    }
}

@Composable
private fun ProfileLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.huge))

        ShimmerCircle(size = SizeTokens.Avatar.sizeXLarge)

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        ShimmerLine(width = 180.dp, height = 32.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                ShimmerLine(width = 40.dp, height = 28.dp)
                ShimmerLine(width = 60.dp, height = 14.dp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                ShimmerLine(width = 40.dp, height = 28.dp)
                ShimmerLine(width = 60.dp, height = 14.dp)
            }
        }
        ShimmerLine(width = 180.dp, height = 32.dp)
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
            onClick = { },
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
            displayName = user.displayName,
        )
    }
}

@Composable
private fun UserName(
    displayName: String?,
    modifier: Modifier = Modifier
) {
    Text(
        text = displayName ?: "",
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

@ThemePreviews
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

@ThemePreviews
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

@ThemePreviews
@Composable
private fun ProfileContentPreview() {
    QodeTheme {
        ProfileContent(
            user = UserPreviewData.powerUser,
            scrollState = rememberScrollState(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ProfileLoadingPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ProfileLoadingState()
        }
    }
}

@ThemePreviews
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
