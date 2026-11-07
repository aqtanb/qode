package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.UserId

class FakePostRepository : PostRepository {
    val createdPosts = mutableListOf<Post>()

    /** When set, createPost fails with this error. */
    var createError: OperationError? = null

    override suspend fun createPost(post: Post): Result<Unit, OperationError> {
        createError?.let { return Result.Error(it) }
        createdPosts += post
        return Result.Success(Unit)
    }

    override suspend fun getPosts(
        limit: Int,
        blockedUserIds: Set<UserId>,
        reportedPostIds: Set<PostId>,
        cursor: Any?
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> = error("Not used in tests")

    override suspend fun getPostById(id: PostId): Result<Post, OperationError> =
        createdPosts.firstOrNull { it.id == id }?.let { Result.Success(it) }
            ?: Result.Error(PostError.RetrievalFailure.NotFound)

    override suspend fun getPostsByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> = error("Not used in tests")
}
