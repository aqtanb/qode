package com.qodein.shared.data.datasource

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class SupabaseAuthDataSource(private val auth: Auth) {
    suspend fun signInWithGoogle(
        idToken: String,
        nonce: String
    ): UserInfo? {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            this.nonce = nonce
            provider = Google
        }
        return auth.currentUserOrNull()
    }

    suspend fun signOut() {
        auth.clearSession()
    }

    fun observeAuthState(): Flow<UserInfo?> =
        auth.sessionStatus.transform { status ->
            when (status) {
                is SessionStatus.Authenticated -> emit(status.session.user ?: auth.currentUserOrNull())
                is SessionStatus.NotAuthenticated -> emit(null)
                else -> Unit
            }
        }
}
