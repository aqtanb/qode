package com.qodein.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeHeroGradient
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeSecurityIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.ComponentPreviews
import com.qodein.core.ui.DevicePreviews
import com.qodein.core.ui.FontScalePreviews
import com.qodein.core.ui.MobilePreviews
import com.qodein.core.ui.PreviewParameterData
import com.qodein.core.ui.TabletPreviews
import com.qodein.core.ui.ThemePreviews
import com.qodein.core.ui.UserPreviewParameterProvider
import com.qodein.core.ui.UserStatsPreviewParameterProvider
import com.qodein.core.ui.component.ComingSoonDialog
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.User
import com.qodein.shared.model.UserStats
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onEditProfile: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onUserJourneyClick: () -> Unit = {}
) {
    TrackScreenViewEvent(screenName = "Profile")

    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    var showComingSoon by remember { mutableStateOf(false) }

    // Collect and handle events
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

    // Handle different UI states directly in ProfileScreen
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Capture state in local variable to avoid smart cast issues with delegated properties
        val currentState = state
        when (currentState) {
            is ProfileUiState.Success -> {
                ProfileSuccessContent(
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
internal fun ProfileSuccessContent(
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
        QodeHeroGradient()

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

// MARK: Stats

@Composable
internal fun StatsSection(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SectionTitle(
            title = stringResource(R.string.profile_stats_title),
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )
        StatsCards(userStats = userStats)
    }
}

/**
 * Preview-optimized version of StatsSection that shows values immediately
 */
@Composable
internal fun StatsSectionPreview(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SectionTitle(
            title = stringResource(R.string.profile_stats_title),
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )
        StatsCardsPreview(userStats = userStats)
    }
}

@Composable
private fun StatsCards(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        StatCard(
            value = userStats.submittedCodes,
            label = stringResource(R.string.profile_promocodes_label),
            icon = QodeCommerceIcons.PromoCode,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCard(
            value = userStats.upvotesReceived,
            label = stringResource(R.string.profile_upvotes_label),
            icon = QodeStatusIcons.Recommended,
            gradientColors = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCard(
            value = userStats.downvotesReceived,
            label = stringResource(R.string.profile_downvotes_label),
            icon = QodeSecurityIcons.Denied,
            gradientColors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Preview-optimized version of StatsCards that shows values immediately without animation
 */
@Composable
private fun StatsCardsPreview(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        StatCardPreview(
            value = userStats.submittedCodes,
            label = stringResource(R.string.profile_promocodes_label),
            icon = QodeCommerceIcons.PromoCode,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCardPreview(
            value = userStats.upvotesReceived,
            label = stringResource(R.string.profile_upvotes_label),
            icon = QodeStatusIcons.Recommended,
            gradientColors = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCardPreview(
            value = userStats.downvotesReceived,
            label = stringResource(R.string.profile_downvotes_label),
            icon = QodeSecurityIcons.Denied,
            gradientColors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (isVisible) value else 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "stat_counter",
    )

    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }

    Card(
        modifier = modifier
            .height(140.dp)
            .let { cardModifier ->
                if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    // Light theme - no shadow to avoid weird effects
                    cardModifier
                } else {
                    // Dark theme - use gradient color shadow
                    cardModifier.shadow(
                        elevation = ElevationTokens.extraLarge,
                        shape = RoundedCornerShape(SpacingTokens.lg - SpacingTokens.xs),
                        ambientColor = gradientColors.first().copy(alpha = 0.3f),
                    )
                }
            },
        // Stats cards are non-clickable - they display info only
        shape = RoundedCornerShape(SpacingTokens.lg - SpacingTokens.xs),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        StatCardContent(
            animatedValue = animatedValue,
            label = label,
            icon = icon,
            gradientColors = gradientColors,
        )
    }
}

@Composable
private fun StatCardContent(
    animatedValue: Int,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors.map { it.copy(alpha = 0.9f) },
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
            )
            .padding(SpacingTokens.md),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(SizeTokens.Icon.sizeXLarge),
                tint = Color.White.copy(alpha = 0.9f),
            )

            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Preview-optimized version of StatCard that shows values immediately without animations
 */
@Composable
private fun StatCardPreview(
    value: Int,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp),
        // Stats cards are non-clickable - they display info only
        shape = RoundedCornerShape(SpacingTokens.lg - SpacingTokens.xs),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        StatCardContent(
            animatedValue = value, // Show value immediately, no animation
            label = label,
            icon = icon,
            gradientColors = gradientColors,
        )
    }
}

// MARK: Recent Activity

@Composable
internal fun ActivityFeed(
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SectionTitle(
            title = stringResource(R.string.profile_activity_title),
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        ActivityCard(
            title = stringResource(R.string.profile_achievements_title),
            content = stringResource(R.string.profile_achievements_empty),
            icon = QodeStatusIcons.Gold,
            iconTint = MaterialTheme.colorScheme.primary,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            onClick = { onAction(ProfileAction.AchievementsClicked) },
        )

        ActivityCard(
            title = stringResource(R.string.profile_user_journey_title),
            content = stringResource(R.string.profile_user_journey_empty),
            icon = QodeNavigationIcons.History,
            iconTint = MaterialTheme.colorScheme.secondary,
            ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            onClick = { onAction(ProfileAction.UserJourneyClicked) },
        )
    }
}

@Composable
private fun ActivityCard(
    title: String,
    content: String,
    icon: ImageVector,
    iconTint: Color,
    ambientColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ElevationTokens.large,
                shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
                ambientColor = ambientColor,
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            ActivityCardHeader(title = title, icon = icon, iconTint = iconTint)
            ActivityCardContent(content = content)
        }
    }
}

@Composable
private fun ActivityCardHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
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
}

@Composable
private fun ActivityCardContent(
    content: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun SectionTitle(
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

// MARK: - Preview Functions

/**
 * Preview-optimized version for ProfileScreen with proper state handling
 */
@Composable
private fun ProfileScreenPreview(
    state: ProfileUiState,
    modifier: Modifier = Modifier
) {
    when (state) {
        is ProfileUiState.Success -> {
            ProfileSuccessContentPreview(
                user = state.user,
                scrollState = rememberScrollState(),
                onAction = {}, // Empty for previews
                onBackClick = {}, // Empty for previews
                modifier = modifier.fillMaxSize(),
            )
        }
        is ProfileUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                QodeHeroGradient()

                // Center the loading indicator
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

                // Top app bar is now handled centrally in QodeApp
            }
        }
        is ProfileUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                QodeHeroGradient()

                // Center the error card
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    QodeErrorCard(
                        error = state.errorType,
                        onRetry = {}, // Empty for previews
                        modifier = Modifier.padding(SpacingTokens.lg),
                    )
                }

                // Top app bar is now handled centrally in QodeApp
            }
        }
    }
}

/**
 * Comprehensive device previews following NIA patterns
 */
@DevicePreviews
@Composable
fun ProfileScreenDevicePreviews() {
    QodeTheme {
        ProfilePreview(
            user = PreviewParameterData.powerUser,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Theme variations preview
 */
@ThemePreviews
@Composable
fun ProfileScreenThemePreviews() {
    QodeTheme {
        ProfilePreview(
            user = PreviewParameterData.sampleUser,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Font scale accessibility testing
 */
@FontScalePreviews
@Composable
fun ProfileScreenFontScalePreviews() {
    QodeTheme {
        ProfilePreview(
            user = PreviewParameterData.sampleUser,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Preview-optimized version of ProfileSuccessContent that bypasses animations
 * for immediate visibility in static previews
 */
@Composable
internal fun ProfileSuccessContentPreview(
    user: User,
    scrollState: ScrollState = rememberScrollState(),
    onAction: (ProfileAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        QodeHeroGradient()

        // Main content with scroll - no animations, content is immediately visible
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxxl))

            // Static header (no animation wrapper)
            ProfileHeader(
                user = user,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Static stats section (no animation wrapper)
            StatsSectionPreview(
                userStats = user.stats,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Static activity feed (no animation wrapper)
            ActivityFeed(
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Static sign out button (no animation wrapper)
            QodeButton(
                text = stringResource(R.string.profile_sign_out_button),
                onClick = { onAction(ProfileAction.SignOutClicked) },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button"),
            )
        }

        // Top app bar is now handled centrally in QodeApp
    }
}

/**
 * Preview-optimized version for ProfileScreen content
 */
@Composable
private fun ProfilePreview(
    user: User,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ProfileSuccessContentPreview(
            user = user,
            onAction = {}, // Empty for previews
            onBackClick = {}, // Empty for previews
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Mobile-specific layouts
 */
@MobilePreviews
@Composable
fun ProfileScreenMobilePreviews() {
    QodeTheme {
        ProfilePreview(
            user = PreviewParameterData.sampleUser,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Tablet-optimized layouts
 */
@TabletPreviews
@Composable
fun ProfileScreenTabletPreviews() {
    QodeTheme {
        ProfilePreview(
            user = PreviewParameterData.powerUser,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Loading state preview following enterprise patterns
 */
@ComponentPreviews
@Composable
fun ProfileScreenLoadingStatePreview() {
    QodeTheme {
        ProfileScreenPreview(
            state = ProfileUiState.Loading,
        )
    }
}

/**
 * Error state preview with proper error handling UI
 */
@ComponentPreviews
@Composable
fun ProfileScreenErrorStatePreview() {
    QodeTheme {
        ProfileScreenPreview(
            state = ProfileUiState.Error(
                errorType = SystemError.Offline,
                isRetryable = true,
                shouldShowSnackbar = false,
                errorCode = "NET_001",
            ),
        )
    }
}

/**
 * Success state preview following enterprise patterns
 */
@ComponentPreviews
@Composable
fun ProfileScreenSuccessStatePreview() {
    QodeTheme {
        ProfileScreenPreview(
            state = ProfileUiState.Success(user = PreviewParameterData.sampleUser),
        )
    }
}

/**
 * Individual component previews following NIA component testing patterns
 */
@ComponentPreviews
@Composable
fun ProfileHeaderComponentPreview(@PreviewParameter(UserPreviewParameterProvider::class) user: User) {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        ),
                    ),
                )
                .padding(SpacingTokens.lg),
        ) {
            ProfileHeader(
                user = user,
                onAction = {}, // Empty for previews
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Stats section with different data scenarios
 */
@ComponentPreviews
@Composable
fun StatsSectionComponentPreview(@PreviewParameter(UserStatsPreviewParameterProvider::class) userStats: UserStats) {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.lg),
        ) {
            StatsSection(
                userStats = userStats,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Activity feed component preview
 */
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

/**
 * Sign out button component preview
 */
@ComponentPreviews
@Composable
fun SignOutButtonComponentPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.lg),
        ) {
            AnimatedSignOutButton(
                onAction = {}, // Empty for previews
                isVisible = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
