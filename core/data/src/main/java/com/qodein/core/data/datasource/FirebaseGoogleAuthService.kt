package com.qodein.core.data.datasource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.qodein.core.data.R
import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseGoogleAuthService @Inject constructor(@ApplicationContext private val context: Context) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    private val serverClientId = context.getString(R.string.web_client_id)

    fun signIn(): Flow<User> =
        flow {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            try {
                val response = credentialManager.getCredential(context, request)
                val cred = response.credential

                if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user

                    val user = firebaseUser?.let { user ->
                        createUserFromFirebaseUser(user)
                    } ?: throw SecurityException("authentication failed: user account not found")

                    emit(user)
                } else {
                    throw SecurityException("authentication failed: invalid credentials received")
                }
            } catch (e: Exception) {
                when {
                    e is SecurityException -> throw e // Re-throw security exceptions as-is
                    e.message?.contains("network", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true ||
                        e.message?.contains("no internet", ignoreCase = true) == true -> {
                        throw IOException("connection error during authentication", e)
                    }
                    e.message?.contains("user_cancelled", ignoreCase = true) == true ||
                        e.message?.contains("cancelled", ignoreCase = true) == true -> {
                        throw IllegalArgumentException("authentication cancelled by user", e)
                    }
                    e.message?.contains("permission", ignoreCase = true) == true ||
                        e.message?.contains("unauthorized", ignoreCase = true) == true -> {
                        throw SecurityException("authentication failed: permission denied", e)
                    }
                    else -> {
                        throw SecurityException("authentication failed: ${e.message ?: "unknown error"}", e)
                    }
                }
            }
        }

    fun signOut(): Flow<Unit> =
        flow {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                auth.signOut()
                emit(Unit)
            } catch (e: Exception) {
                when {
                    e.message?.contains("network", ignoreCase = true) == true -> {
                        throw IOException("connection error during sign out", e)
                    }
                    else -> {
                        throw IllegalStateException("service unavailable: failed to sign out", e)
                    }
                }
            }
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
