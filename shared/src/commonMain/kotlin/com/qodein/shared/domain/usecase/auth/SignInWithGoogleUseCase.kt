package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.user.ResolveUserUseCase

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val resolveUserUseCase: ResolveUserUseCase
) {
    suspend operator fun invoke(idToken: String): Result<Unit, OperationError> =
        when (val authResult = authRepository.signInWithGoogle(idToken)) {
            is Result.Error -> Result.Error(authResult.error)
            is Result.Success ->
                when (val resolvedUser = resolveUserUseCase(authResult.data)) {
                    is Result.Success -> Result.Success(Unit)
                    is Result.Error -> Result.Error(resolvedUser.error)
                }
        }
}
