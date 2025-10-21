package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseGoogleAuthService: FirebaseGoogleAuthService,
    private val userRepository: UserRepository
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override fun signInWithGoogle(): Flow<Result<User, OperationError>> =
        flow {
            Logger.d(TAG) { "Signing in with Google" }
            val result = firebaseGoogleAuthService.signIn()

            when (result) {
                is Result.Success -> {
                    val user = result.data
                    Logger.d(TAG) { "Syncing user to Firestore: ${user.id.value}" }

                    userRepository.createUserIfNew(user).collect { syncResult ->
                        when (syncResult) {
                            is Result.Success -> {
                                Logger.i(TAG) { "User synced successfully to Firestore" }
                            }
                            is Result.Error -> {
                                Logger.w(TAG) { "Failed to sync user to Firestore: ${syncResult.error}" }
                            }
                        }
                    }

                    emit(result)
                }
                is Result.Error -> {
                    emit(result)
                }
            }
        }

    override fun signOut(): Flow<Result<Unit, OperationError>> =
        flow {
            Logger.d(TAG) { "Signing out" }
            val result = firebaseGoogleAuthService.signOut()
            emit(result)
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
