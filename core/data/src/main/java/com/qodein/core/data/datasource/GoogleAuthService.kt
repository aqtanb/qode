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
import com.qodein.shared.model.UserPreferences
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(@ApplicationContext private val context: Context) {
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

            val response = credentialManager.getCredential(context, request)
            val cred = response.credential

            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user

                val user = firebaseUser?.let { user ->
                    createUserFromFirebaseUser(user)
                } ?: throw IllegalStateException("Null user after authentication")

                emit(user)
            } else {
                throw IllegalArgumentException("Invalid credentials")
            }
        }

    fun signOut(): Flow<Unit> =
        flow {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            auth.signOut()
            emit(Unit)
        }

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun createUserFromFirebaseUser(firebaseUser: FirebaseUser): User {
        val email = firebaseUser.email ?: throw IllegalStateException("Google Auth user must have an email")
        val userId = UserId(firebaseUser.uid)
        val baseUsername = generateUsernameFromEmail(email)

        val profile = UserProfile(
            username = baseUsername,
            firstName = extractFirstName(firebaseUser.displayName) ?: baseUsername,
            lastName = extractLastName(firebaseUser.displayName),
            bio = null,
            photoUrl = firebaseUser.photoUrl?.toString(),
            birthday = null,
            gender = null,
            isGenerated = firebaseUser.displayName.isNullOrBlank(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        return User(
            id = userId,
            email = Email(email),
            profile = profile,
            stats = UserStats.initial(userId),
            preferences = UserPreferences.default(userId),
        )
    }

    private fun generateUsernameFromEmail(email: String): String {
        val localPart = email.substringBefore("@")
        return localPart.replace("[^a-zA-Z0-9]".toRegex(), "")
            .lowercase()
            .take(20)
            .ifEmpty { "user" }
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
