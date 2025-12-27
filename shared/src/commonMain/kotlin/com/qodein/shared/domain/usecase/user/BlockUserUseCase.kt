package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.first

class BlockUserUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(blockedUserId: UserId): Result<Unit, OperationError> {
        val authState = getAuthStateUseCase().first()
        val currentUserId = when (authState) {
            is AuthState.Authenticated -> authState.userId.value
            AuthState.Unauthenticated -> return Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable)
        }

        return userRepository.blockUser(currentUserId, blockedUserId.value)
    }
}
