package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.UserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetAuthStateUseCase(private val authRepository: AuthRepository, private val userRepository: UserRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<AuthState> =
        authRepository
            .observeAuthState()
            .flatMapLatest { googleUser ->
                if (googleUser == null) {
                    flowOf<AuthState>(AuthState.Unauthenticated)
                } else {
                    userRepository.observeUser(googleUser.uid)
                        .map { result ->
                            when (result) {
                                is Result.Success -> AuthState.Authenticated(UserId(googleUser.uid))
                                is Result.Error -> AuthState.Unauthenticated
                            }
                        }
                }
            }
            .distinctUntilChanged()
}
