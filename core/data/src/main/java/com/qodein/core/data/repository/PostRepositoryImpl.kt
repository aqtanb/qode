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

    override fun createPost(post: Post): Flow<Result<Post, OperationError>> =
        flow {
            val result = dataSource.createPost(post)

            if (result is Result.Success) {
                userDataSource.incrementPostCount(post.authorId.value)
                // We don't emit the increment result - it's not critical for post creation
            }

            emit(result)
        }

    override fun getPosts(
        sortBy: PostSortBy,
        filterByTags: List<Tag>?,
        paginationRequest: PaginationRequest<PostSortBy>
    ): Flow<Result<PaginatedResult<Post, PostSortBy>, OperationError>> =
        flow {
            emit(dataSource.getPosts(sortBy, filterByTags, paginationRequest))
        }

    override fun getPostById(id: PostId): Flow<Result<Post?, OperationError>> =
        flow {
            emit(dataSource.getPostById(id))
        }
}
