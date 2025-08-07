package com.qodein.core.domain.repository

import com.qodein.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface AuthRepository {

    /**
     * Sign in with Google authentication.
     *
     * @return Flow that emits [User] on successful authentication
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Google Play Services unavailable
     * @throws SecurityException when authentication is rejected or cancelled
     * @throws RuntimeException for unexpected authentication errors
     */
    fun signInWithGoogle(): Flow<User>

    /**
     * Sign out the current user.
     *
     * @return Flow that emits [Unit] on successful sign out
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when no user is signed in
     */
    fun signOut(): Flow<Unit>

    /**
     * Get the currently authenticated user.
     *
     * @return Flow that emits current [User] or null if not authenticated
     * @throws java.io.IOException when unable to fetch user data from remote source
     * @throws IllegalStateException when local user data is corrupted
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Observe authentication state changes.
     *
     * @return Flow that emits [User] when signed in, null when signed out
     * This method doesn't throw exceptions - authentication state changes
     * are delivered as Flow emissions.
     */
    fun getAuthStateFlow(): Flow<User?>

    /**
     * Check if user is currently signed in.
     *
     * @return true if user is authenticated, false otherwise
     * Note: This is synchronous and doesn't throw exceptions
     */
    fun isSignedIn(): Boolean
}
