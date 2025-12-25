package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.user.EnsureUserExists
import com.qodein.shared.model.UserResolutionResult

class SignInWithGoogleUseCase(private val authRepository: AuthRepository, private val ensureUserExists: EnsureUserExists) {
    suspend operator fun invoke(idToken: String): Result<UserResolutionResult, OperationError> =
        when (val authResult = authRepository.signInWithGoogle(idToken)) {
            is Result.Error -> Result.Error(authResult.error)
            is Result.Success -> ensureUserExists(authResult.data)
        }
}
