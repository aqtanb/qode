package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Pure UI component that conditionally renders content based on authentication state.
 *
 * This is a core UI component with no dependencies on feature modules.
 * The authentication state is provided from the calling feature module.
 *
 * @param isAuthenticated Whether the user is currently authenticated
 * @param authenticatedContent Content to show when user is authenticated
 * @param unauthenticatedContent Content to show when user needs authentication
 * @param modifier Modifier for the container
 */
@Composable
fun AuthRequiredContent(
    isAuthenticated: Boolean,
    authenticatedContent: @Composable () -> Unit,
    unauthenticatedContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isAuthenticated) {
        authenticatedContent()
    } else {
        unauthenticatedContent()
    }
}

/**
 * Pure UI component that provides an onClick handler that respects authentication state.
 *
 * This component doesn't know about authentication logic - it simply calls the appropriate
 * callback based on the provided authentication state.
 *
 * @param isAuthenticated Whether the user is currently authenticated
 * @param onAuthenticatedClick Called when user is authenticated and clicks
 * @param onUnauthenticatedClick Called when user is not authenticated and clicks
 * @param content The clickable content to display
 */
@Composable
fun AuthProtectedClickable(
    isAuthenticated: Boolean,
    onAuthenticatedClick: () -> Unit,
    onUnauthenticatedClick: () -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val onClick = if (isAuthenticated) onAuthenticatedClick else onUnauthenticatedClick
    content(onClick)
}
