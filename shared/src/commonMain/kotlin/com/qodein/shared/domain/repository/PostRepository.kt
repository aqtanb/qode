package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Post operations.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Phase 1 (MVP): CREATE and READ only, tag filtering only.
 */
interface PostRepository {

    /**
     * Create a new post.
     */
    fun createPost(post: Post): Flow<Result<Post, OperationError>>

    /**
     * Get posts with optional tag filtering and sorting using cursor-based pagination.
     */
    fun getPosts(
        sortBy: PostSortBy = PostSortBy.POPULARITY,
        filterByTags: List<Tag>? = null,
        paginationRequest: PaginationRequest<PostSortBy> = PaginationRequest.firstPage()
    ): Flow<Result<PaginatedResult<Post, PostSortBy>, OperationError>>

    /**
     * Get a specific post by ID.
     */
    fun getPostById(id: PostId): Flow<Result<Post?, OperationError>>
}
