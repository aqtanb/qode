package com.qodein.feature.profile

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.feature.profile.component.ProfileSceleton
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
                ProfileEvent.SignedOut -> onSignOut()
                ProfileEvent.NavigateToAuth -> onBackClick()
                ProfileEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
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
                onBackClick = onBackClick,
                onAction = onAction,
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
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is ProfileUiState.Loading -> {
                    ProfileSceleton()
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileHeader(user = user)
    }
}

@Composable
private fun ProfileHeader(
    user: User,
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
                onAction = {},
                uiState = ProfileUiState.Error(SystemError.Unknown),
            )
        }
    }
}
