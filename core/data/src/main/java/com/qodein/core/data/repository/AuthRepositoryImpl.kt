package com.qodein.core.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.qodein.core.data.datasource.FirebaseGoogleAuthService
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(private val firebaseGoogleAuthService: FirebaseGoogleAuthService) : AuthRepository {

    override fun signInWithGoogle(): Flow<Result<User, OperationError>> =
        flow {
            try {
                firebaseGoogleAuthService.signIn().collect { user ->
                    emit(Result.Success(user))
                }
            } catch (e: SecurityException) {
                emit(Result.Error(UserError.AuthenticationFailure.InvalidCredentials))
            } catch (e: IllegalStateException) {
                emit(Result.Error(UserError.AuthenticationFailure.ServiceUnavailable))
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
        }

    override fun signOut(): Flow<Result<Unit, OperationError>> =
        flow {
            try {
                firebaseGoogleAuthService.signOut().collect { unit ->
                    emit(Result.Success(unit))
                }
            } catch (e: IllegalStateException) {
                emit(Result.Error(UserError.AuthenticationFailure.ServiceUnavailable))
            } catch (e: IOException) {
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                emit(Result.Error(SystemError.Unknown))
            }
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
