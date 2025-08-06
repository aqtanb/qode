package com.qodein.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    ProfileContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
        onNavigateToAuth = onNavigateToAuth,
    )
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToAuth: () -> Unit
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
                onNavigateToAuth()
            }

            is ProfileUiState.SignedIn -> {
                SignedInContent(
                    user = state.user,
                    onSignOutClick = { onAction(ProfileAction.SignOutClicked) },
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Header section with primaryContainer background covering everything
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            QodeTopAppBar(
                title = null,
                navigationIcon = QodeActionIcons.Back,
            )
            ProfileHeaderSection(
                user = user,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = 200.dp, // Account for header height
                    start = SpacingTokens.md,
                    end = SpacingTokens.md,
                    bottom = SpacingTokens.md,
                ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Stats section
            ProfileStatsSection(
                userStats = user.stats,
                modifier = Modifier.fillMaxWidth(),
            )

            // Actions section
            ProfileActionsSection(
                modifier = Modifier.fillMaxWidth(),
            )

            // Sign out button
            QodeButton(
                text = stringResource(R.string.profile_sign_out_button),
                onClick = onSignOutClick,
                variant = QodeButtonVariant.Error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SpacingTokens.md),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Left side - User info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "@${user.profile.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Text(
                    text = "${user.profile.firstName} ${user.profile.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                user.profile.bio?.let { bio ->
                    if (bio.isNotBlank()) {
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = SpacingTokens.xs),
                        )
                    }
                }
            }

            // Right side - Profile picture
            if (user.profile.photoUrl != null) {
                AsyncImage(
                    model = user.profile.photoUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Edit profile button
        QodeButton(
            onClick = { /*TODO*/ },
            text = "Edit Profile",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProfileStatsSection(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Text(
            text = stringResource(R.string.profile_stats_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                value = userStats.commentsCount.toString(),
                label = stringResource(R.string.profile_comments_label),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                value = userStats.submittedCodes.toString(),
                label = stringResource(R.string.profile_promocodes_label),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                value = userStats.achievements.size.toString(),
                label = stringResource(R.string.profile_achievements_label),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileActionsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Text(
            text = stringResource(R.string.profile_actions_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        QodeButton(
            text = stringResource(R.string.profile_view_achievements),
            onClick = { /*TODO*/ },
            variant = QodeButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )

        QodeButton(
            text = stringResource(R.string.profile_view_comments),
            onClick = { /*TODO*/ },
            variant = QodeButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )

        QodeButton(
            text = stringResource(R.string.profile_view_promocodes),
            onClick = { /*TODO*/ },
            variant = QodeButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
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
