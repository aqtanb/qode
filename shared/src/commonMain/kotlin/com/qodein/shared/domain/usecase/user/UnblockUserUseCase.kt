package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import kotlinx.coroutines.flow.first

class UnblockUserUseCase(private val userRepository: UserRepository, private val authStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(blockedUserId: String): Result<Unit, OperationError> {
        return when (val authState = authStateUseCase().first()) {
            is AuthState.Authenticated -> {
                userRepository.unblockUser(authState.userId.value, blockedUserId)
            }
            AuthState.Unauthenticated -> return Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable)
        }
    }
}
