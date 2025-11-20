package com.qodein.core.data.datasource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialException
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.qodein.core.data.R
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException

class FirebaseAuthDataSource(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) {
    suspend fun signIn(): Result<FirebaseUser, OperationError> =
        try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val response = credentialManager.getCredential(context, request)
            val cred = response.credential

            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()

                authResult.user?.let {
                    Result.Success(it)
                } ?: Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
            } else {
                Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
            }
        } catch (e: GetCredentialCancellationException) {
            Timber.d(e, "User cancelled sign-in")
            Result.Error(UserError.AuthenticationFailure.Cancelled)
        } catch (e: NoCredentialException) {
            Timber.e(e, "No credentials available")
            Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable)
        } catch (e: GetCredentialProviderConfigurationException) {
            Timber.e(e, "Credential provider misconfigured")
            Result.Error(UserError.AuthenticationFailure.ConfigurationError)
        } catch (e: GetCredentialInterruptedException) {
            Timber.e(e, "Credential request interrupted")
            Result.Error(UserError.AuthenticationFailure.Cancelled)
        } catch (e: GetCredentialUnsupportedException) {
            Timber.e(e, "Credential type unsupported")
            Result.Error(UserError.AuthenticationFailure.UnsupportedCredential)
        } catch (e: GetCredentialException) {
            Timber.e(e, "Credential error during sign-in")
            Result.Error(UserError.AuthenticationFailure.Unknown)
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
            Timber.e(e, "Firebase network error during sign-in")
            Result.Error(SystemError.Offline)
        } catch (e: FirebaseAuthException) {
            Timber.e(e, "Firebase auth error during sign-in")
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

    suspend fun signOut(): Result<Unit, OperationError> =
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error during sign-out")
            Result.Error(SystemError.Unknown)
        }

    fun getAuthStateFlow(): Flow<FirebaseUser?> =
        callbackFlow {
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth -> trySend(firebaseAuth.currentUser) }
            auth.addAuthStateListener(authStateListener)
            awaitClose { auth.removeAuthStateListener(authStateListener) }
        }
}
