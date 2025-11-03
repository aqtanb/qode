package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction

/**
 * Use case to get user interaction for specific content and user.
 * Returns null if no interaction exists.
 */
class GetUserInteractionUseCase(private val repository: UnifiedUserInteractionRepository) {
    suspend operator fun invoke(
        itemId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError> = repository.getUserInteraction(itemId, userId)
}
