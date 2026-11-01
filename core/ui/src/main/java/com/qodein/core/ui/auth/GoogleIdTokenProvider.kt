package com.qodein.core.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.qodein.core.ui.R
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.GoogleIdToken
import java.security.MessageDigest
import java.util.UUID

class GoogleIdTokenProvider(private val manager: CredentialManager) : IdTokenProvider {
    override suspend fun getIdToken(activityContext: Context): Result<GoogleIdToken, OperationError> =
        try {
            val nonce = UUID.randomUUID().toString()
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(activityContext.getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setNonce(sha256Hex(nonce))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = manager.getCredential(activityContext, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.Success(GoogleIdToken(credential.idToken, nonce))
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    private fun sha256Hex(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
