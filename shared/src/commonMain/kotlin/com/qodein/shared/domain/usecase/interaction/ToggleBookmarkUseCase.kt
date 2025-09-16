package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
/**
 * Use case to toggle user's bookmark on content.
 */
class ToggleBookmarkUseCase(private val repository: UnifiedUserInteractionRepository) {
    suspend operator fun invoke(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction> =
        try {
            val result = repository.toggleBookmark(itemId, itemType, userId)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
}
