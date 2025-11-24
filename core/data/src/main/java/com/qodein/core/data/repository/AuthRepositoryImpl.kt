package com.qodein.core.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.qodein.core.data.datasource.FirebaseAuthDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.GoogleAuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

class AuthRepositoryImpl(private val dataSource: FirebaseAuthDataSource) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<GoogleAuthResult, OperationError> =
        try {
            val firebaseUser = dataSource.signInWithToken(idToken)
            Result.Success(
                GoogleAuthResult(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                ),
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Invalid credentials")
            Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e, "Invalid user")
            when (e.errorCode) {
                "ERROR_USER_DISABLED" -> Result.Error(UserError.AuthenticationFailure.AccountDisabled)
                else -> Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "Account exists with different credential")
            Result.Error(UserError.AuthenticationFailure.AccountConflict)
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error during sign-in")
            Result.Error(SystemError.Offline)
        } catch (e: FirebaseAuthException) {
            Timber.e(e, "Firebase auth error")
            when (e.errorCode) {
                "ERROR_TOO_MANY_REQUESTS" -> Result.Error(UserError.AuthenticationFailure.TooManyAttempts)
                else -> Result.Error(UserError.AuthenticationFailure.Unknown)
            }
        } catch (e: IOException) {
            Timber.e(e, "Network error during sign-in")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error during sign-in")
            Result.Error(SystemError.Unknown)
        }

    override fun signOut() {
        dataSource.signOut()
    }

    override fun observeAuthState(): Flow<GoogleAuthResult?> =
        dataSource.observeAuthState().map { firebaseUser ->
            if (firebaseUser == null) null
            else GoogleAuthResult(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
            )
        }
}
