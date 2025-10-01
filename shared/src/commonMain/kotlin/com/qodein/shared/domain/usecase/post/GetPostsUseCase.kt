package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching posts with pagination, sorting, and optional tag filtering.
 *
 * Delegates to repository for data fetching.
 */
class GetPostsUseCase(private val postRepository: PostRepository) {

    operator fun invoke(
        sortBy: PostSortBy = PostSortBy.POPULARITY,
        filterByTags: List<Tag>? = null,
        paginationRequest: PaginationRequest<PostSortBy> = PaginationRequest.firstPage()
    ): Flow<Result<PaginatedResult<Post, PostSortBy>, OperationError>> =
        postRepository.getPosts(
            sortBy = sortBy,
            filterByTags = filterByTags,
            paginationRequest = paginationRequest,
        )
}
