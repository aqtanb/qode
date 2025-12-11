package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GetAuthStateUseCase(private val authRepository: AuthRepository) {

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
}
