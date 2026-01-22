package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class GetAuthStateUseCase(private val authRepository: AuthRepository, private val scope: CoroutineScope) {
    operator fun invoke(): Flow<AuthState> =
        authRepository
            .observeAuthState()
            .map { googleUser ->
                if (googleUser == null) {
                    AuthState.Unauthenticated
                } else {
                    AuthState.Authenticated(UserId(googleUser.uid))
                }
            }
            .distinctUntilChanged()
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1,
            )
}
