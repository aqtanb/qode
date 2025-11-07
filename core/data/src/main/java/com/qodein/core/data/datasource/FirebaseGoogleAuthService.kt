package com.qodein.core.data.datasource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import co.touchlab.kermit.Logger
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.qodein.core.data.R
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await

class FirebaseGoogleAuthService constructor(@ApplicationContext private val context: Context) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    private val serverClientId = context.getString(R.string.web_client_id)

    companion object {
        private const val TAG = "FirebaseGoogleAuthService"
    }

    suspend fun signIn(): Result<User, OperationError> =
        try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
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
                val firebaseUser = authResult.user

                if (firebaseUser == null) {
                    Logger.e(TAG) { "Sign in failed: user account not found" }
                    Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
                } else {
                    val user = createUserFromFirebaseUser(firebaseUser)
                    Logger.i(TAG) { "Successfully signed in: userId=${user.id.value}" }
                    Result.Success(user)
                }
            } else {
                Logger.e(TAG) { "Sign in failed: invalid credential type received" }
                Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
            }
        } catch (e: GetCredentialCancellationException) {
            Logger.i(TAG) { "Sign in cancelled by user" }
            Result.Error(UserError.AuthenticationFailure.Cancelled)
        } catch (e: GoogleIdTokenParsingException) {
            Logger.e(TAG, e) { "Sign in failed: invalid Google ID token" }
            Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Logger.i(TAG) { "Sign in cancelled by user" }
                    Result.Error(UserError.AuthenticationFailure.Cancelled)
                }
                CommonStatusCodes.NETWORK_ERROR, CommonStatusCodes.TIMEOUT -> {
                    Logger.e(TAG, e) { "Sign in failed: network error" }
                    Result.Error(SystemError.Offline)
                }
                else -> {
                    Logger.e(TAG, e) { "Sign in failed: API error (code=${e.statusCode})" }
                    Result.Error(UserError.AuthenticationFailure.ServiceUnavailable)
                }
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_NETWORK_REQUEST_FAILED" -> {
                    Logger.e(TAG, e) { "Sign in failed: network error" }
                    Result.Error(SystemError.Offline)
                }
                "ERROR_USER_DISABLED", "ERROR_USER_NOT_FOUND" -> {
                    Logger.e(TAG, e) { "Sign in failed: user account issue (${e.errorCode})" }
                    Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
                }
                "ERROR_INVALID_CREDENTIAL" -> {
                    Logger.e(TAG, e) { "Sign in failed: invalid credentials" }
                    Result.Error(UserError.AuthenticationFailure.InvalidCredentials)
                }
                else -> {
                    Logger.e(TAG, e) { "Sign in failed: Firebase auth error (${e.errorCode})" }
                    Result.Error(UserError.AuthenticationFailure.ServiceUnavailable)
                }
            }
        } catch (e: FirebaseNetworkException) {
            Logger.e(TAG, e) { "Sign in failed: network error" }
            Result.Error(SystemError.Offline)
        } catch (e: FirebaseTooManyRequestsException) {
            Logger.e(TAG, e) { "Sign in failed: too many requests" }
            Result.Error(UserError.AuthenticationFailure.TooManyAttempts)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Sign in failed: security exception" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Sign in failed: unexpected error" }
            Result.Error(SystemError.Unknown)
        }

    suspend fun signOut(): Result<Unit, OperationError> =
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            auth.signOut()
            Logger.i(TAG) { "Successfully signed out" }
            Result.Success(Unit)
        } catch (e: FirebaseNetworkException) {
            Logger.e(TAG, e) { "Sign out failed: network error" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Sign out failed: unexpected error" }
            Result.Error(SystemError.Unknown)
        }

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun createUserFromFirebaseUser(firebaseUser: FirebaseUser): User {
        val email = firebaseUser.email ?: throw SecurityException("authentication failed: user email not available")
        val userId = UserId(firebaseUser.uid)

        val profile = UserProfile(
            firstName = extractFirstName(firebaseUser.displayName) ?: "User",
            lastName = extractLastName(firebaseUser.displayName),
            bio = null,
            photoUrl = firebaseUser.photoUrl?.toString(),
        )

        return User(
            id = userId,
            email = Email(email),
            profile = profile,
            stats = UserStats.initial(userId),
            country = "KZ", // Default to Kazakhstan for your market
        )
    }

    private fun extractFirstName(displayName: String?): String? = displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }

    private fun extractLastName(displayName: String?): String? {
        val parts = displayName?.trim()?.split(" ") ?: return null
        return if (parts.size > 1) {
            parts.drop(1).joinToString(" ").takeIf { it.isNotBlank() }
        } else {
            null
        }
    }
}
