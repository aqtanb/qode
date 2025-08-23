package com.qodein.shared.domain.usecase.comment

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.CommentRepository
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentParentType
import kotlinx.coroutines.flow.Flow

class GetCommentsUseCase constructor(private val commentRepository: CommentRepository) {
    operator fun invoke(
        parentId: String,
        parentType: CommentParentType,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<Result<List<Comment>>> {
        require(parentId.isNotBlank()) { "Parent ID cannot be blank" }
        require(limit in 1..100) { "Limit must be between 1 and 100" }
        require(offset >= 0) { "Offset must be non-negative" }

        return commentRepository.getComments(
            parentId = parentId,
            parentType = parentType,
            limit = limit,
            offset = offset,
        ).asResult()
    }
}
