package com.qodein.shared.domain.usecase.user

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import kotlinx.coroutines.flow.first

class GetBlockedUserIdsUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(): Set<String> {
        val authState = getAuthStateUseCase().first()

        return when (authState) {
            is AuthState.Authenticated ->
                userRepository.getBlockedUserIds(authState.userId.value)
            AuthState.Unauthenticated -> emptySet()
        }
    }
}
