package com.qodein.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import kotlinx.coroutines.delay

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

            is ProfileUiState.SignedOut -> {
                // This state should not occur with smart routing
                // If user reaches here, show a loading indicator as fallback
                CircularProgressIndicator()
            }

            is ProfileUiState.SignedIn -> {
                SignedInContent(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignedInContent(
    user: User,
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Gradient hero section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
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

        // Floating decorative elements
        FloatingDecorations()

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
        ) {
            Spacer(modifier = Modifier.height(100.dp)) // Space for transparent top bar

            // Modern profile header
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
                ModernProfileHeader(
                    user = user,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Interactive stats cards
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
                InteractiveStatsSection(
                    userStats = user.stats,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Modern activity feed
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
                ModernActivityFeed(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Stylish sign out button
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1200, 600)),
            ) {
                QodeButton(
                    text = stringResource(R.string.profile_sign_out_button),
                    onClick = onSignOutClick,
                    variant = QodeButtonVariant.Error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }
}

@Composable
private fun ModernProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Glassmorphism profile picture
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                ),
        ) {
            if (user.profile.photoUrl != null) {
                AsyncImage(
                    model = user.profile.photoUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                ),
                            ),
                        ),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile picture",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // User information with modern styling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Text(
                text = "${user.profile.firstName} ${user.profile.lastName ?: ""}".trim(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "@${user.profile.username}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            user.profile.bio?.let { bio ->
                if (bio.isNotBlank()) {
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = SpacingTokens.lg),
                    )
                }
            }
        }

        // Modern edit profile button
        QodeButton(
            onClick = { /*TODO*/ },
            text = stringResource(R.string.edit_profile_button),
            variant = QodeButtonVariant.Outlined,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
        )
    }
}

@Composable
private fun InteractiveStatsSection(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = stringResource(R.string.profile_stats_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Promocodes (first)
            InteractiveStatCard(
                value = userStats.submittedCodes,
                label = stringResource(R.string.profile_promocodes_label),
                icon = Icons.Default.LocalOffer,
                gradientColors = listOf(
                    Color(0xFF667eea),
                    Color(0xFF764ba2),
                ),
                modifier = Modifier.weight(1f),
            )

            // Achievements (second)
            InteractiveStatCard(
                value = userStats.achievements.size,
                label = stringResource(R.string.profile_achievements_label),
                icon = Icons.Default.EmojiEvents,
                gradientColors = listOf(
                    Color(0xFFf093fb),
                    Color(0xFFf5576c),
                ),
                modifier = Modifier.weight(1f),
            )

            // Comments (third)
            InteractiveStatCard(
                value = userStats.commentsCount,
                label = stringResource(R.string.profile_comments_label),
                icon = Icons.Default.QuestionAnswer,
                gradientColors = listOf(
                    Color(0xFF4facfe),
                    Color(0xFF00f2fe),
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InteractiveStatCard(
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
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
            )
            .clickable { /* TODO: Add click interaction */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.9f) },
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
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
                    modifier = Modifier.size(32.dp),
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
}

@Composable
private fun ModernActivityFeed(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = stringResource(R.string.profile_activity_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        // Achievement showcase card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )
                .clickable { /*TODO*/ },
            shape = RoundedCornerShape(16.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.profile_recent_achievements),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(R.string.profile_achievements_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Recent activity card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                )
                .clickable { /*TODO*/ },
            shape = RoundedCornerShape(16.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.profile_recent_activity),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(R.string.profile_activity_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FloatingDecorations() {
    // Add some floating decorative elements
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Floating circle 1
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = 50.dp, y = 150.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape,
                ),
        )

        // Floating circle 2
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 280.dp, y = 200.dp)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    CircleShape,
                ),
        )

        // Floating circle 3
        Box(
            modifier = Modifier
                .size(30.dp)
                .offset(x = 320.dp, y = 120.dp)
                .background(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    CircleShape,
                ),
        )
    }
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
                user = User(
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
                ),
            ),
            onAction = {},
            onNavigateToAuth = {},
        )
    }
}
