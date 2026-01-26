package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.UserId

interface PostRepository {
    suspend fun createPost(post: Post): Result<Unit, OperationError>

    suspend fun getPosts(
        limit: Int,
        blockedUserIds: Set<UserId>,
        reportedPostIds: Set<PostId>,
        cursor: Any?
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError>

    suspend fun getPostById(id: PostId): Result<Post, OperationError>

    suspend fun getPostsByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError>
}
