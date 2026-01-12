package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction

/**
 * Repository interface for unified user interactions (votes + bookmarks).
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface UnifiedUserInteractionRepository {

    suspend fun getUserInteraction(
        documentId: String,
        userId: UserId
    ): Result<UserInteraction?, OperationError>

    suspend fun toggleVote(interaction: UserInteraction): Result<UserInteraction, OperationError>
}
