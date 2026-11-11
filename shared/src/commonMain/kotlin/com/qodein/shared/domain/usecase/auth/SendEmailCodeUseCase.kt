package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository

class SendEmailCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit, OperationError> {
        val cleanEmail = email.trim().lowercase()
        if (!EMAIL_REGEX.matches(cleanEmail)) return Result.Error(UserError.AuthenticationFailure.InvalidEmail)
        return authRepository.sendEmailCode(cleanEmail)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
