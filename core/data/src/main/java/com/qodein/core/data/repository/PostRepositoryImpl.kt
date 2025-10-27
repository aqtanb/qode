package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PostRepository using Firestore as the data source.
 * DataSource already returns Result, so repository simply wraps in Flow.
 * Orchestrates multiple data sources for complex operations.
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val dataSource: FirestorePostDataSource,
    private val userDataSource: FirestoreUserDataSource
) : PostRepository {

    override suspend fun createPost(post: Post): Result<Post, OperationError> {
        val result = dataSource.createPost(post)

        if (result is Result.Success) {
            userDataSource.incrementPostCount(post.authorId.value)
        }

        return result
    }

    override fun getPosts(
        sortBy: PostSortBy,
        filterByTags: List<Tag>?,
        paginationRequest: PaginationRequest<PostSortBy>
    ): Flow<Result<PaginatedResult<Post, PostSortBy>, OperationError>> =
        flow {
            emit(dataSource.getPosts(sortBy, filterByTags, paginationRequest))
        }

    override suspend fun getPostById(id: PostId): Result<Post, OperationError> = dataSource.getPostById(id)
}
