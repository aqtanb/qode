package com.qodein.feature.profile

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.designsystem.component.AutoHidingTopAppBar
import com.qodein.core.designsystem.component.QodeAvatar
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeHeroGradient
import com.qodein.core.designsystem.component.QodeRetryableErrorCard
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
import com.qodein.core.model.User
import com.qodein.core.model.UserStats
import com.qodein.core.ui.ComponentPreviews
import com.qodein.core.ui.DevicePreviews
import com.qodein.core.ui.FontScalePreviews
import com.qodein.core.ui.MobilePreviews
import com.qodein.core.ui.PreviewParameterData
import com.qodein.core.ui.TabletPreviews
import com.qodein.core.ui.ThemePreviews
import com.qodein.core.ui.UserPreviewParameterProvider
import com.qodein.core.ui.UserStatsPreviewParameterProvider
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onAchievementsClick: () -> Unit = {}, // TODO: Navigate to achievements screen
    onUserJourneyClick: () -> Unit = {} // TODO: Navigate to user journey screen (promocodes & comments history)
) {
    val state by viewModel.state.collectAsState()

    ProfileContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick,
        onSignOut = onSignOut,
        onAchievementsClick = onAchievementsClick,
        onUserJourneyClick = onUserJourneyClick,
    )
}

// MARK: UI States

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onUserJourneyClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is ProfileUiState.Success -> {
                ProfileLayout(
                    user = state.user,
                    onSignOutClick = {
                        onAction(ProfileAction.SignOutClicked)
                        onSignOut()
                    },
                    onBackClick = onBackClick,
                    onAchievementsClick = onAchievementsClick,
                    onUserJourneyClick = onUserJourneyClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.testTag("profile_loading"),
                )
            }

            is ProfileUiState.Error -> {
                QodeRetryableErrorCard(
                    message = state.exception.message ?: "Unknown error",
                    onRetry = { onAction(ProfileAction.RetryClicked) },
                )
            }
        }
    }
}

private const val ANIMATION_DELAY_MS = 100L

// MARK: - Profile Layout
@Composable
private fun ProfileLayout(
    user: User,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onUserJourneyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(ANIMATION_DELAY_MS)
        isVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        QodeHeroGradient()

        // Content with proper top padding to account for TopAppBar
        ProfileDetails(
            user = user,
            onSignOutClick = onSignOutClick,
            onAchievementsClick = onAchievementsClick,
            onUserJourneyClick = onUserJourneyClick,
            isVisible = isVisible,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxSize(),
        )

        // Add scroll-aware TopAppBar that overlays on top
        AutoHidingTopAppBar(
            scrollState = scrollState,
            navigationIcon = QodeActionIcons.Back,
            onNavigationClick = onBackClick,
            navigationIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

// MARK: Main Content

@Composable
private fun ProfileDetails(
    user: User,
    onSignOutClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onUserJourneyClick: () -> Unit,
    isVisible: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.xxxl))

        AnimatedProfileHeader(
            user = user,
            isVisible = isVisible,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        AnimatedStatsSection(
            userStats = user.stats,
            isVisible = isVisible,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        AnimatedActivityFeed(
            isVisible = isVisible,
            onAchievementsClick = onAchievementsClick,
            onUserJourneyClick = onUserJourneyClick,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        AnimatedSignOutButton(
            onSignOutClick = onSignOutClick,
            isVisible = isVisible,
        )
    }
}

@Composable
private fun AnimatedProfileHeader(
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
        ProfileHeader(
            user = user,
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
    isVisible: Boolean,
    onAchievementsClick: () -> Unit,
    onUserJourneyClick: () -> Unit,
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
            onAchievementsClick = onAchievementsClick,
            onUserJourneyClick = onUserJourneyClick,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

// MARK: Sign Out

@Composable
private fun AnimatedSignOutButton(
    onSignOutClick: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1200, 600)),
    ) {
        QodeButton(
            text = stringResource(R.string.profile_sign_out_button),
            onClick = onSignOutClick,
            variant = QodeButtonVariant.Error,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

// MARK: Header

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
        QodeAvatar(
            photoUrl = user.profile.photoUrl,
            size = SizeTokens.Avatar.sizeXLarge,
        )
        UserInfo(user = user)
        EditProfileButton()
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
        Username(username = user.profile.username)
        user.profile.bio?.let { bio ->
            if (bio.isNotBlank()) {
                UserBio(bio = bio)
            }
        }
    }
}

// MARK: Edit Profile

@Composable
private fun EditProfileButton(modifier: Modifier = Modifier) {
    QodeButton(
        onClick = { /*TODO*/ },
        text = stringResource(R.string.edit_profile_button),
        variant = QodeButtonVariant.Primary,
        modifier = modifier
            .fillMaxWidth(0.6f)
            .shadow(
                elevation = ElevationTokens.large,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            ),
    )
}

// MARK: Stats

@Composable
private fun StatsSection(
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
            value = userStats.upvotes,
            label = stringResource(R.string.profile_upvotes_label),
            icon = QodeStatusIcons.Recommended,
            gradientColors = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCard(
            value = userStats.downvotes,
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

// MARK: Recent Activity

@Composable
private fun ActivityFeed(
    onAchievementsClick: () -> Unit,
    onUserJourneyClick: () -> Unit,
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
            onClick = onAchievementsClick,
        )

        ActivityCard(
            title = stringResource(R.string.profile_user_journey_title),
            content = stringResource(R.string.profile_user_journey_empty),
            icon = QodeNavigationIcons.History,
            iconTint = MaterialTheme.colorScheme.secondary,
            ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            onClick = onUserJourneyClick,
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
        text = "$firstName ${lastName ?: ""}".trim(),
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
        text = "@$username",
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
 * Comprehensive device previews following NIA patterns
 */
@DevicePreviews
@Composable
fun ProfileScreenDevicePreviews() {
    QodeTheme {
        SignedInContentPreview(
            user = PreviewParameterData.powerUser,
            onSignOutClick = {},
            onBackClick = {},
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
        SignedInContentPreview(
            user = PreviewParameterData.powerUser,
            onSignOutClick = {},
            onBackClick = {},
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
        SignedInContentPreview(
            user = PreviewParameterData.sampleUser,
            onSignOutClick = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Preview-optimized version without animation delays
 */
@Composable
private fun SignedInContentPreview(
    user: User,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        QodeHeroGradient()

        // Content with proper top padding to account for TopAppBar - no animation delay
        ProfileDetails(
            user = user,
            onSignOutClick = onSignOutClick,
            onAchievementsClick = {}, // Empty for previews
            onUserJourneyClick = {}, // Empty for previews
            isVisible = true, // Always visible in previews
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
        )

        // Add scroll-aware TopAppBar that overlays on top
        AutoHidingTopAppBar(
            scrollState = scrollState,
            navigationIcon = QodeActionIcons.Back,
            onNavigationClick = onBackClick,
            navigationIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
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
        SignedInContentPreview(
            user = PreviewParameterData.sampleUser,
            onSignOutClick = {},
            onBackClick = {},
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
        SignedInContentPreview(
            user = PreviewParameterData.powerUser,
            onSignOutClick = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * All possible UI states preview (like NIA)
 */
@ComponentPreviews
@Composable
fun ProfileScreenStates() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // Loading State
            Text(
                text = "Loading State",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(SpacingTokens.md),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                ProfileContent(
                    state = ProfileUiState.Loading,
                    onAction = {},
                    onBackClick = {},
                    onSignOut = {},
                )
            }

            // Error State
            Text(
                text = "Error State",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(SpacingTokens.md),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                ProfileContent(
                    state = ProfileUiState.Error(
                        exception = Exception("Network connection failed"),
                    ),
                    onAction = {},
                    onBackClick = {},
                    onSignOut = {},
                )
            }
        }
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
 * Individual stat cards with different states
 */
@ComponentPreviews
@Composable
fun StatCardVariationsPreview() {
    QodeTheme {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Low values
            item {
                StatCard(
                    value = 0,
                    label = "New User",
                    icon = QodeCommerceIcons.PromoCode,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }

            // Medium values
            item {
                StatCard(
                    value = 42,
                    label = "Active User",
                    icon = QodeStatusIcons.Gold,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                )
            }

            // High values
            item {
                StatCard(
                    value = 999,
                    label = "Power User",
                    icon = QodeActionIcons.Comment,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }

            // Very high values (1000+)
            item {
                StatCard(
                    value = 2847,
                    label = "Legend",
                    icon = QodeStatusIcons.Diamond,
                    gradientColors = listOf(
                        Color(0xFFFF6B35),
                        Color(0xFFFFB347),
                    ),
                )
            }
        }
    }
}

/**
 * Activity feed component variations
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
                onAchievementsClick = {},
                onUserJourneyClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * User info component with different name lengths and bio scenarios
 */
@ComponentPreviews
@Composable
fun UserInfoVariationsPreview() {
    QodeTheme {
        LazyColumn(
            contentPadding = PaddingValues(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
        ) {
            item {
                Text(
                    text = "Short Name",
                    style = MaterialTheme.typography.labelMedium,
                )
                UserInfo(
                    user = PreviewParameterData.newUser,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = "Long Name & Bio",
                    style = MaterialTheme.typography.labelMedium,
                )
                UserInfo(
                    user = PreviewParameterData.userWithLongBio,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = "Power User",
                    style = MaterialTheme.typography.labelMedium,
                )
                UserInfo(
                    user = PreviewParameterData.powerUser,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Edge cases and error scenarios
 */
@ComponentPreviews
@Composable
fun ProfileScreenEdgeCasesPreview() {
    QodeTheme {
        LazyColumn(
            contentPadding = PaddingValues(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            item {
                Text(
                    text = "New User (Zero Stats)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = SpacingTokens.sm),
                )
                StatsCards(
                    userStats = PreviewParameterData.newUserStats,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = "No Bio User",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = SpacingTokens.sm),
                )
                UserInfo(
                    user = PreviewParameterData.newUser,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = "High Values User",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = SpacingTokens.sm),
                )
                StatsCards(
                    userStats = PreviewParameterData.powerUserStats,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Dark theme specific preview
 */
@Preview(
    name = "Profile Dark Theme Showcase",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=480",
)
@Composable
fun ProfileScreenDarkThemeShowcase() {
    QodeTheme {
        SignedInContentPreview(
            user = PreviewParameterData.powerUser,
            onSignOutClick = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Light theme specific preview
 */
@Preview(
    name = "Profile Light Theme Showcase",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=480",
)
@Composable
fun ProfileScreenLightThemeShowcase() {
    QodeTheme {
        SignedInContentPreview(
            user = PreviewParameterData.powerUser,
            onSignOutClick = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Performance testing preview with complex layout
 */
@Preview(
    name = "Profile Performance Test",
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=480",
)
@Composable
fun ProfileScreenPerformancePreview() {
    QodeTheme {
        // Simulate heavy content for performance testing
        val users = listOf(
            PreviewParameterData.sampleUser,
            PreviewParameterData.powerUser,
            PreviewParameterData.userWithLongBio,
            PreviewParameterData.newUser,
        )

        LazyColumn {
            items(users) { user ->
                ProfileHeader(
                    user = user,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.md),
                )
            }
        }
    }
}

/**
 * Interactive preview for testing touch targets and animations
 */
@Preview(
    name = "Profile Interactive Elements",
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=480",
)
@Composable
fun ProfileScreenInteractivePreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text(
                text = "Interactive Elements",
                style = MaterialTheme.typography.headlineSmall,
            )

            EditProfileButton()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                StatCard(
                    value = 42,
                    label = "Tap Me",
                    icon = QodeCommerceIcons.PromoCode,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                )

                StatCard(
                    value = 15,
                    label = "Touch Test",
                    icon = QodeStatusIcons.Gold,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            QodeButton(
                text = "Sign Out Button",
                onClick = {},
                variant = QodeButtonVariant.Error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
