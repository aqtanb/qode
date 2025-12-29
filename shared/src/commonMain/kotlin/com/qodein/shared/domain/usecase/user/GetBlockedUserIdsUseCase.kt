package com.qodein.shared.domain.usecase.user

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetBlockedUserIdsUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    operator fun invoke(): Flow<Set<String>> =
        getAuthStateUseCase()
            .flatMapLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated ->
                        userRepository.getBlockedUserIds(authState.userId.value)
                    AuthState.Unauthenticated -> flowOf(emptySet())
                }
            }
}
