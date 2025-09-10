package com.qodein.core.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.qodein.shared.domain.AuthState as DomainAuthState

/**
 * Core authentication gate - the ONLY way to protect actions behind authentication.
 *
 * Uses the content lambda pattern for clean, reusable auth protection.
 * Integrates with AuthStateManager and SignInWithGoogleUseCase directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationGate(
    action: AuthPromptAction,
    onAuthenticated: (User?) -> Unit = {},
    modifier: Modifier = Modifier,
    authStateManager: AuthStateManager,
    signInWithGoogleUseCase: SignInWithGoogleUseCase,
    isDarkTheme: Boolean,
    content: @Composable (requireAuth: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val authState by authStateManager.getAuthState().collectAsStateWithLifecycle(initialValue = DomainAuthState.Loading)

    var showAuthSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authenticatedUser = (authState as? DomainAuthState.Authenticated)?.user
    val isAuthenticated = authenticatedUser != null

    // Function to trigger authentication check
    val requireAuth = {
        if (isAuthenticated) {
            onAuthenticated(authenticatedUser)
        } else {
            showAuthSheet = true
        }
    }

    // Function to handle sign-in
    val handleSignIn = {
        scope.launch {
            isLoading = true
            errorMessage = null

            signInWithGoogleUseCase()
                .onEach { result ->
                    when (result) {
                        is Result.Loading -> {
                            isLoading = true
                        }
                        is Result.Success -> {
                            isLoading = false
                            // AuthStateManager will automatically update, triggering recomposition
                        }
                        is Result.Error -> {
                            isLoading = false
                            if (result.exception.shouldShowSnackbar()) {
                                errorMessage = result.exception.message ?: "Sign-in failed"
                            }
                        }
                    }
                }
                .launchIn(scope)
        }
        Unit
    }

    // Render the content with auth requirement callback
    content(requireAuth)

    // Show authentication bottom sheet when needed
    if (showAuthSheet) {
        AuthenticationBottomSheet(
            action = action,
            onSignInClick = handleSignIn,
            onDismiss = {
                showAuthSheet = false
                errorMessage = null
                isLoading = false
            },
            modifier = modifier,
            isLoading = isLoading,
            errorType = errorMessage?.let {
                RuntimeException(it).toErrorType()
            },
            onErrorDismissed = {
                errorMessage = null
            },
            isDarkTheme = isDarkTheme,
        )
    }

    // Listen for successful authentication
    LaunchedEffect(authState, showAuthSheet) {
        if (showAuthSheet && isAuthenticated) {
            // User successfully signed in
            showAuthSheet = false
            onAuthenticated(authenticatedUser)
        }
    }
}

/**
 * Simple authenticated content wrapper - for basic show/hide based on auth state.
 */
@Composable
fun AuthenticatedContent(
    authenticatedContent: @Composable (User) -> Unit,
    unauthenticatedContent: @Composable () -> Unit = {},
    authStateManager: AuthStateManager
) {
    val authState by authStateManager.getAuthState().collectAsStateWithLifecycle(initialValue = DomainAuthState.Loading)

    when (val currentAuthState = authState) {
        is DomainAuthState.Authenticated -> authenticatedContent(currentAuthState.user)
        is DomainAuthState.Unauthenticated -> unauthenticatedContent()
        is DomainAuthState.Loading -> {} // Could add loading state here if needed
    }
}
