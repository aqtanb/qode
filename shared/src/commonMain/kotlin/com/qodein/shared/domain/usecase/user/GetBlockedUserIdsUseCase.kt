package com.qodein.shared.domain.usecase.user

import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.first

class GetBlockedUserIdsUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(): Set<UserId> =
        when (val authState = getAuthStateUseCase().first()) {
            is AuthState.Authenticated ->
                userRepository.getBlockedUserIds(authState.userId)
            AuthState.Unauthenticated -> emptySet()
        }
}
