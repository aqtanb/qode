package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.GoogleIdToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(signedInAs: GoogleAuthResult? = null) : AuthRepository {
    private val authState = MutableStateFlow(signedInAs)

    fun signIn(uid: String) {
        authState.value = GoogleAuthResult(uid = uid, email = null, displayName = null, photoUrl = null)
    }

    override suspend fun signInWithGoogle(token: GoogleIdToken): Result<GoogleAuthResult, OperationError> = error("Not used in tests")

    override suspend fun signOut() {
        authState.value = null
    }

    override fun observeAuthState(): Flow<GoogleAuthResult?> = authState
}
