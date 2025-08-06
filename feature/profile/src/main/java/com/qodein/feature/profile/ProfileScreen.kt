package com.qodein.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
    onSignInClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    ProfileContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::handleAction,
        onSignInClick = onSignInClick,
    )
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.md),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }

            is ProfileUiState.NotSignedIn -> {
                NotSignedInContent(onSignInClick = onSignInClick)
            }

            is ProfileUiState.SignedIn -> {
                SignedInContent(
                    user = state.user,
                    onSignOutClick = { onAction(ProfileAction.SignOutClicked) },
                )
            }

            is ProfileUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetryClick = { onAction(ProfileAction.RetryClicked) },
                )
            }
        }
    }
}

@Composable
private fun NotSignedInContent(
    onSignInClick: () -> Unit,
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
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.profile_not_signed_in_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.profile_not_signed_in_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            QodeButton(
                text = stringResource(R.string.profile_sign_in_button),
                onClick = onSignInClick,
                variant = QodeButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.sm),
            )
        }
    }
}

@Composable
private fun SignedInContent(
    user: User,
    onSignOutClick: () -> Unit,
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
            // Profile Picture
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // User Name
            Text(
                text = "${user.profile.firstName} ${user.profile.lastName ?: ""}".trim(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // User Email
            Text(
                text = user.email.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Username
            Text(
                text = "@${user.profile.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            // Sign Out Button
            QodeButton(
                text = stringResource(R.string.profile_sign_out_button),
                onClick = onSignOutClick,
                variant = QodeButtonVariant.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.md),
            )
        }
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

@Preview(name = "Profile Not Signed In", showSystemUi = true)
@Composable
private fun ProfileNotSignedInPreview() {
    QodeTheme {
        ProfileContent(
            state = ProfileUiState.NotSignedIn,
            onAction = {},
            onSignInClick = {},
        )
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
                        bio = null,
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
            onSignInClick = {},
        )
    }
}
