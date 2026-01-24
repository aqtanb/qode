package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostSortBy

class GetPostsUseCase(private val postRepository: PostRepository, private val getBlockedUserIdsUseCase: GetBlockedUserIdsUseCase) {
    suspend operator fun invoke(cursor: Any? = null): Result<PaginatedResult<Post, PostSortBy>, OperationError> {
        val blockedUserIds = getBlockedUserIdsUseCase()
        return postRepository.getPosts(
            cursor = cursor,
            blockedUserIds = blockedUserIds,
            limit = PaginationRequest.DEFAULT_PAGE_SIZE,
        )
    }
}
