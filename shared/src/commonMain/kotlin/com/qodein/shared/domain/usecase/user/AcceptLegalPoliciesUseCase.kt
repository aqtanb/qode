package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UserRepository
import kotlin.time.Clock

class AcceptLegalPoliciesUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<Unit, OperationError> =
        userRepository.updateUserConsent(
            userId = userId,
            legalPoliciesAcceptedAt = Clock.System.now().toEpochMilliseconds(),
        )
}
