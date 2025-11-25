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

class GoogleIdTokenProvider(private val manager: CredentialManager) : IdTokenProvider {
    override suspend fun getIdToken(activityContext: Context): Result<String, OperationError> =
        try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(activityContext.getString(R.string.web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = manager.getCredential(activityContext, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.Success(credential.idToken)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }
}
