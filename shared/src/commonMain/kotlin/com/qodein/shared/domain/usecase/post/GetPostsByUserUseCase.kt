package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.SortBy
import com.qodein.shared.model.UserId

class GetPostsByUserUseCase(private val postRepository: PostRepository) {
    suspend operator fun invoke(
        userId: UserId,
        cursor: Any? = null
    ): Result<PaginatedResult<Post, SortBy>, OperationError> =
        postRepository.getPostsByUser(
            userId = userId,
            cursor = cursor,
            limit = PaginationRequest.DEFAULT_PAGE_SIZE,
        )
}
