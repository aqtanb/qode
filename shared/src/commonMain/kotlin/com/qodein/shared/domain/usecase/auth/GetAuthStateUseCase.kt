package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.user.ResolveUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAuthStateUseCase(private val authRepository: AuthRepository, private val resolveUserUseCase: ResolveUserUseCase) {

    operator fun invoke(): Flow<AuthState> =
        authRepository.observeAuthState().map { googleUser ->
            if (googleUser == null) {
                AuthState.Unauthenticated
            } else {
                when (val user = resolveUserUseCase(googleUser)) {
                    is Result.Error -> AuthState.Unauthenticated
                    is Result.Success -> AuthState.Authenticated(user.data)
                }
            }
        }
}
