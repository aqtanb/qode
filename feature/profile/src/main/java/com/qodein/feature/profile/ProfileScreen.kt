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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.ComponentPreviews
import com.qodein.core.ui.component.ComingSoonDialog
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.feature.profile.component.StatsSection
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
    val uriHandler = LocalUriHandler.current
    var showComingSoon by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.EditProfileRequested -> showComingSoon = true
                ProfileEvent.SignedOut -> onSignOut()
                ProfileEvent.AchievementsRequested -> showComingSoon = true
                ProfileEvent.UserJourneyRequested -> showComingSoon = true
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

    // Show Coming Soon dialog when user tries to access coming soon features
    if (showComingSoon) {
        ComingSoonDialog(
            onDismiss = { showComingSoon = false },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }
}

// MARK: - Success Content

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileContent(
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
        // Main content with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxxl))

            AnimatedProfileHeader(
                user = user,
                onAction = onAction,
                isVisible = isContentVisible,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            AnimatedStatsSection(
                userStats = user.stats,
                isVisible = isContentVisible,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            AnimatedActivityFeed(
                onAction = onAction,
                isVisible = isContentVisible,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

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
private fun AnimatedStatsSection(
    userStats: UserStats,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(animationSpec = tween(800, 200)),
    ) {
        StatsSection(
            userStats = userStats,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AnimatedActivityFeed(
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
        ActivityFeed(
            onAction = onAction,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

// MARK: Sign Out

@Composable
internal fun AnimatedSignOutButton(
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)),
    ) {
        QodeButton(
            text = stringResource(R.string.profile_sign_out_button),
            onClick = { onAction(ProfileAction.SignOutClicked) },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = modifier
                .fillMaxWidth()
                .testTag("sign_out_button"),
        )
    }
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
        EditProfileButton(onAction = onAction)
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

// MARK: Edit Profile

@Composable
private fun EditProfileButton(
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeButton(
        onClick = { onAction(ProfileAction.EditProfileClicked) },
        text = stringResource(R.string.edit_profile_button),
        modifier = modifier
            .widthIn(min = 120.dp, max = 280.dp)
            .shadow(
                elevation = ElevationTokens.large,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            ),
    )
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
private fun Username(
    username: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.profile_username_format, username),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun UserBio(
    bio: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = bio,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = SpacingTokens.lg),
    )
}

@Composable
internal fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@ComponentPreviews
@Composable
fun ActivityFeedComponentPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.lg),
        ) {
            ActivityFeed(
                onAction = {}, // Empty for previews
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
