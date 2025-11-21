package com.qodein.qode.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.qodein.qode.R

class GoogleIdTokenProvider(private val manager: CredentialManager) {
    suspend fun getIdToken(context: Context): String {
        // Define the type of authentication request to be sent to the provider.
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()

        // All the options to sign in
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = manager.getCredential(context, request)
        val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
        return credential.idToken
    }
}
