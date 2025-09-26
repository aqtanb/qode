package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction

/**
 * Use case to get all bookmarked content for a user.
 */
class GetUserBookmarksUseCase(private val repository: UnifiedUserInteractionRepository) {
    suspend operator fun invoke(userId: UserId): Result<List<UserInteraction>, OperationError> = repository.getUserBookmarks(userId)
}
