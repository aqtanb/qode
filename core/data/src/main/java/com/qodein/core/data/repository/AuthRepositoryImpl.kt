package com.qodein.core.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.qodein.core.data.datasource.GoogleAuthService
import com.qodein.core.domain.repository.AuthRepository
import com.qodein.core.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(private val googleAuthService: GoogleAuthService) : AuthRepository {

    // Direct Flow delegation - no unnecessary wrappers
    override fun signInWithGoogle(): Flow<User> = googleAuthService.signIn()

    override fun signOut(): Flow<Unit> = googleAuthService.signOut()

    override fun getCurrentUser(): Flow<User?> = googleAuthService.getCurrentUser()

    override fun getAuthStateFlow(): Flow<User?> =
        callbackFlow {
            val auth = Firebase.auth

            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val firebaseUser = firebaseAuth.currentUser
                val user = firebaseUser?.let { googleAuthService.createUserFromFirebaseUser(it) }
                trySend(user)
            }

            // Add listener and send initial state
            auth.addAuthStateListener(authStateListener)

            // Remove listener when Flow is cancelled
            awaitClose {
                auth.removeAuthStateListener(authStateListener)
            }
        }

    override fun isSignedIn(): Boolean = googleAuthService.isSignedIn()
}
