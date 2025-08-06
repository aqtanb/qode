package com.qodein.core.data.repository

import com.qodein.core.data.datasource.GoogleAuthService
import com.qodein.core.domain.repository.AuthRepository
import com.qodein.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(private val googleAuthService: GoogleAuthService) : AuthRepository {

    // Direct Flow delegation - no unnecessary wrappers
    override fun signInWithGoogle(): Flow<User> = googleAuthService.signIn()

    override fun signOut(): Flow<Unit> = googleAuthService.signOut()

    override fun getCurrentUser(): Flow<User?> = googleAuthService.getCurrentUser()

    override fun getAuthStateFlow(): Flow<User?> {
        // For now, delegate to getCurrentUser - can implement Firebase auth state listener later
        return getCurrentUser()
    }

    override fun isSignedIn(): Boolean = googleAuthService.isSignedIn()
}
