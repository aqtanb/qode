package com.qodein.feature.profile

// Animation
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.designsystem.component.QodeAvatar
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeScrollAwareTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import kotlinx.coroutines.delay

// MARK: - Screen Composables

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    ProfileContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
        onNavigateToAuth = onNavigateToAuth,
        onBackClick = onBackClick,
        onSignOut = onSignOut,
    )
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToAuth: () -> Unit,
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }

            is ProfileUiState.SignedIn -> {
                SignedInContentWithScrollBehavior(
                    user = state.user,
                    onSignOutClick = {
                        onAction(ProfileAction.SignOutClicked)
                        onSignOut()
                    },
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ProfileUiState.Error -> {
                ErrorContent(
                    message = state.exception.message ?: "Unknown error",
                    onRetryClick = { onAction(ProfileAction.RetryClicked) },
                )
            }
        }
    }
}

// MARK: - Constants

private const val ANIMATION_DELAY_MS = 100L
private val HERO_HEIGHT = SpacingTokens.xxxl * 5

// MARK: - Main Content

@Composable
private fun SignedInContent(
    user: User,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(ANIMATION_DELAY_MS)
        isVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        HeroGradientBackground()
        FloatingDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = SpacingTokens.lg)
                .padding(top = SpacingTokens.xxxl + SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
        ) {
            AnimatedProfileHeader(
                user = user,
                isVisible = isVisible,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            AnimatedStatsSection(
                userStats = user.stats,
                isVisible = isVisible,
            )

            AnimatedActivityFeed(isVisible = isVisible)

            AnimatedSignOutButton(
                onSignOutClick = onSignOutClick,
                isVisible = isVisible,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }
}

@Composable
private fun SignedInContentWithScrollBehavior(
    user: User,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(ANIMATION_DELAY_MS)
        isVisible = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        HeroGradientBackground()
        FloatingDecorations()

        ProfileContentWithScroll(
            user = user,
            onSignOutClick = onSignOutClick,
            isVisible = isVisible,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
        )

        // Add scroll-aware TopAppBar that hides/shows based on scroll
        QodeScrollAwareTopAppBar(
            scrollState = scrollState,
            navigationIcon = QodeActionIcons.Back,
            onNavigationClick = onBackClick,
            navigationIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

// MARK: Profile

@Composable
private fun ProfileContentWithScroll(
    user: User,
    onSignOutClick: () -> Unit,
    isVisible: Boolean,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = SpacingTokens.lg)
            .padding(top = SpacingTokens.xxxl + SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        AnimatedProfileHeader(
            user = user,
            isVisible = isVisible,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        AnimatedStatsSection(
            userStats = user.stats,
            isVisible = isVisible,
        )

        AnimatedActivityFeed(isVisible = isVisible)

        AnimatedSignOutButton(
            onSignOutClick = onSignOutClick,
            isVisible = isVisible,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.xxl))
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
        ActivityFeed(modifier = modifier.fillMaxWidth())
    }
}

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
            value = userStats.achievements.size,
            label = stringResource(R.string.profile_achievements_label),
            icon = QodeStatusIcons.Gold,
            gradientColors = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer,
            ),
            modifier = Modifier.weight(1f),
        )

        StatCard(
            value = userStats.commentsCount,
            label = stringResource(R.string.profile_comments_label),
            icon = QodeActionIcons.Comment,
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
            .aspectRatio(0.8f)
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
            }
            .clickable { /* TODO: Add click interaction */ },
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
private fun ActivityFeed(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SectionTitle(
            title = stringResource(R.string.profile_activity_title),
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        ActivityCard(
            title = stringResource(R.string.profile_recent_achievements),
            content = stringResource(R.string.profile_achievements_empty),
            icon = QodeStatusIcons.Gold,
            iconTint = MaterialTheme.colorScheme.primary,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )

        ActivityCard(
            title = stringResource(R.string.profile_recent_activity),
            content = stringResource(R.string.profile_activity_empty),
            icon = QodeCommerceIcons.PromoCode,
            iconTint = MaterialTheme.colorScheme.secondary,
            ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
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
            .clickable { /*TODO*/ },
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
private fun HeroGradientBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = 800f,
                ),
            ),
    )
}

@Composable
private fun FloatingDecorations(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        FloatingCircle(
            size = SizeTokens.IconButton.sizeMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            offset = Offset(
                x = (SpacingTokens.xxl + SpacingTokens.xs).value,
                y = (SpacingTokens.xxxl * 2 + SpacingTokens.lg).value,
            ),
        )

        FloatingCircle(
            size = SizeTokens.Avatar.sizeMedium + SpacingTokens.md,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            offset = Offset(
                x = (SpacingTokens.xxxl * 4 + SpacingTokens.lg).value,
                y = (SpacingTokens.xxxl * 3 + SpacingTokens.sm).value,
            ),
        )

        FloatingCircle(
            size = SizeTokens.Chip.height + SpacingTokens.xs,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
            offset = Offset(
                x = (SpacingTokens.xxxl * 5).value,
                y = (SpacingTokens.xxxl + SpacingTokens.xxl + SpacingTokens.sm).value,
            ),
        )
    }
}

@Composable
private fun FloatingCircle(
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    offset: Offset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .offset(x = offset.x.dp, y = offset.y.dp)
            .background(color, CircleShape),
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

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        variant = QodeCardVariant.Elevated,
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingTokens.xl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Text(
                text = stringResource(R.string.profile_error_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            QodeButton(
                text = stringResource(R.string.profile_retry_button),
                onClick = onRetryClick,
                variant = QodeButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.sm),
            )
        }
    }
}

@Preview(name = "Profile Signed In", showSystemUi = true)
@Composable
private fun ProfileSignedInPreview() {
    QodeTheme {
        ProfileContent(
            state = ProfileUiState.SignedIn(
                user = previewUser(),
            ),
            onAction = {},
            onNavigateToAuth = {},
        )
    }
}

private fun previewUser() =
    User(
        id = UserId("test"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            bio = "Android developer passionate about creating amazing user experiences!",
            photoUrl = null,
            birthday = null,
            gender = null,
            isGenerated = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats.initial(UserId("test")),
        preferences = UserPreferences.default(UserId("test")),
    )
