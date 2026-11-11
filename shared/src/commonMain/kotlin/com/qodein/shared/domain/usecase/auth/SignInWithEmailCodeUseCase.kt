package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.user.EnsureUserExists
import com.qodein.shared.model.UserResolutionResult

class SignInWithEmailCodeUseCase(private val authRepository: AuthRepository, private val ensureUserExists: EnsureUserExists) {
    suspend operator fun invoke(
        email: String,
        code: String
    ): Result<UserResolutionResult, OperationError> {
        val cleanCode = code.filter { it.isDigit() }
        if (cleanCode.length != CODE_LENGTH) return Result.Error(UserError.AuthenticationFailure.InvalidCode)
        return when (val authResult = authRepository.signInWithEmailCode(email.trim().lowercase(), cleanCode)) {
            is Result.Error -> Result.Error(authResult.error)
            is Result.Success -> ensureUserExists(authResult.data)
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
