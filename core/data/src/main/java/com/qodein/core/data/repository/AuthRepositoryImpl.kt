package com.qodein.core.data.repository

import timber.log.Timber
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.qodein.core.data.datasource.FirebaseGoogleAuthService
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val firebaseGoogleAuthService: FirebaseGoogleAuthService,
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun signInWithGoogle(): Result<User, OperationError> {
        try {
            val firebaseUser = firebaseGoogleAuthService.signIn()
            return Result.Success(result)
        } catch (e: Exception) {
            Timber.e(e)
            return Result.Error(OperationError.UnknownError)
        }
    }


    override suspend fun signOut(): Result<Unit, OperationError> {

    }

    override fun getAuthStateFlow(): Flow<User?> =
        callbackFlow {
            val auth = Firebase.auth

            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val firebaseUser = firebaseAuth.currentUser
                val user = firebaseUser?.let { firebaseGoogleAuthService.createUserFromFirebaseUser(it) }
                trySend(user)
            }

            // Add listener and send initial state
            auth.addAuthStateListener(authStateListener)

            // Remove listener when Flow is cancelled
            awaitClose {
                auth.removeAuthStateListener(authStateListener)
            }
        }
}
