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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.ShimmerCircle
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.icon.UIIcons
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "Profile")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.SignedOut -> onSignOut()
                is ProfileEvent.NavigateToAuth -> onBackClick()
            }
        }
    }

    ProfileScreen(
        onBackClick = onBackClick,
        onNavigateToBlockedUsers = onNavigateToBlockedUsers,
        onAction = viewModel::onAction,
        uiState = uiState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    onBackClick: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
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
        containerColor = MaterialTheme.colorScheme.background,
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
                        onNavigateToBlockedUsers = onNavigateToBlockedUsers,
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

@Composable
private fun ProfileContent(
    user: User,
    scrollState: ScrollState,
    onNavigateToBlockedUsers: () -> Unit,
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
        ProfileHeader(
            user = user,
            isVisible = isContentVisible,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        ActionsSection(
            onNavigateToBlockedUsers = onNavigateToBlockedUsers,
            isVisible = isContentVisible,
        )

        SignOutButton(
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
private fun ProfileHeader(
    user: User,
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
}

@Composable
private fun ActionsSection(
    onNavigateToBlockedUsers: () -> Unit,
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
        QodeButton(
            onClick = onNavigateToBlockedUsers,
            text = stringResource(R.string.profile_blocked_title),
            modifier = modifier.fillMaxWidth(),
            leadingIcon = UIIcons.Block,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SignOutButton(
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)),
    ) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            QodeButton(
                text = stringResource(R.string.profile_sign_out_button),
                onClick = { onAction(ProfileAction.SignOutClicked) },
                size = ButtonSize.Large,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = modifier
                    .fillMaxWidth(0.8f)
                    .testTag("sign_out_button"),
            )
        }
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
        Text(
            text = user.displayName ?: "",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = modifier,
        )
    }
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
private fun ProfileScreenPreview() {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onNavigateToBlockedUsers = {},
                onAction = {},
                uiState = ProfileUiState.Success(user = UserPreviewData.powerUser),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileLoadingPreview() {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onNavigateToBlockedUsers = {},
                onAction = {},
                uiState = ProfileUiState.Loading,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileErrorPreview() {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onNavigateToBlockedUsers = {},
                onAction = {},
                uiState = ProfileUiState.Error(SystemError.Unknown),
            )
        }
    }
}
