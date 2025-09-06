package com.qodein.feature.auth.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthRequiredContent
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.feature.auth.AuthAction
import com.qodein.feature.auth.AuthUiState
import com.qodein.feature.auth.AuthViewModel

/**
 * Feature-level authentication gate that uses core UI components.
 *
 * This component belongs in the feature module because it contains business logic
 * and depends on AuthViewModel. Core UI components are used for pure UI rendering.
 *
 * @param action The authentication action that provides contextual messaging
 * @param onAuthenticated Called when user is authenticated and can perform the action
 * @param modifier Modifier for the authentication bottom sheet
 * @param authViewModel ViewModel for handling authentication logic
 * @param isDarkTheme Whether to use dark theme styling (from app preferences)
 * @param content Content that should be displayed (usually a button or interactive element)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationGate(
    action: AuthPromptAction,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    isDarkTheme: Boolean,
    content: @Composable (requireAuth: () -> Unit) -> Unit
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }

    val isAuthenticated = authState is AuthUiState.Success

    // Function to trigger authentication check
    val requireAuth = {
        if (isAuthenticated) {
            onAuthenticated()
        } else {
            showAuthSheet = true
        }
    }

    // Render the content with auth requirement callback
    content(requireAuth)

    // Show authentication bottom sheet when needed
    if (showAuthSheet) {
        AuthenticationBottomSheet(
            action = action,
            onSignInClick = {
                authViewModel.handleAction(AuthAction.SignInWithGoogleClicked)
            },
            onDismiss = {
                showAuthSheet = false
            },
            modifier = modifier,
            isLoading = authState is AuthUiState.Loading,
            errorType = (authState as? AuthUiState.Error)?.takeIf { it.shouldShowSnackbar }?.errorType,
            onErrorDismissed = {
                authViewModel.handleAction(AuthAction.DismissErrorClicked)
            },
            isDarkTheme = isDarkTheme,
        )
    }

    // Listen for successful authentication
    LaunchedEffect(authState) {
        if (showAuthSheet && isAuthenticated) {
            // User successfully signed in
            showAuthSheet = false
            onAuthenticated()
        }
    }
}

/**
 * Feature-level authentication utilities using core UI components.
 *
 * This provides a clean interface for feature modules to protect actions
 * behind authentication without violating architectural boundaries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun requireAuthentication(
    action: AuthPromptAction,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    isDarkTheme: Boolean
): () -> Unit {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }

    val isAuthenticated = authState is AuthUiState.Success

    // Show authentication bottom sheet when needed
    if (showAuthSheet) {
        AuthenticationBottomSheet(
            action = action,
            onSignInClick = {
                authViewModel.handleAction(AuthAction.SignInWithGoogleClicked)
            },
            onDismiss = {
                showAuthSheet = false
            },
            modifier = modifier,
            isLoading = authState is AuthUiState.Loading,
            errorType = (authState as? AuthUiState.Error)?.takeIf { it.shouldShowSnackbar }?.errorType,
            onErrorDismissed = {
                authViewModel.handleAction(AuthAction.DismissErrorClicked)
            },
            isDarkTheme = isDarkTheme,
        )
    }

    // Listen for successful authentication
    LaunchedEffect(authState) {
        if (showAuthSheet && isAuthenticated) {
            // User successfully signed in
            showAuthSheet = false
            onAuthenticated()
        }
    }

    // Return function that checks auth and either proceeds or shows bottom sheet
    return {
        if (isAuthenticated) {
            onAuthenticated()
        } else {
            showAuthSheet = true
        }
    }
}

/**
 * Feature-level composable that uses core AuthRequiredContent with auth state.
 */
@Composable
fun FeatureAuthenticatedContent(
    authenticatedContent: @Composable () -> Unit,
    unauthenticatedContent: @Composable () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val isAuthenticated = authState is AuthUiState.Success

    AuthRequiredContent(
        isAuthenticated = isAuthenticated,
        authenticatedContent = authenticatedContent,
        unauthenticatedContent = unauthenticatedContent,
    )
}
