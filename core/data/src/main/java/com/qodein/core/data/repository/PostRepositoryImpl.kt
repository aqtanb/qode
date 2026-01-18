package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.core.data.mapper.PostMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.UserId

/**
 * Implementation of PostRepository using Firestore as the data source.
 * DataSource already returns Result, so repository simply wraps in Flow.
 * Orchestrates multiple data sources for complex operations.
 */

class PostRepositoryImpl(private val dataSource: FirestorePostDataSource) : PostRepository {
    override suspend fun createPost(post: Post): Result<Unit, OperationError> {
        try {
            Logger.d { "Creating post: $post" }
            dataSource.createPost(PostMapper.toDto(post))
            Logger.d { "Successfully created post: $post" }
            return Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Logger.e(e) { "Error creating post: $post" }
            return Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: Exception) {
            Logger.e(e) { "Error creating post: $post" }
            return Result.Error(SystemError.Unknown)
        }
    }

    override suspend fun getPosts(
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> {
        try {
            Logger.d { "Fetching posts: cursor=$cursor, limit=$limit" }
            val snapshot = (cursor as? PaginationCursor<*>)?.value as? DocumentSnapshot
            val pagedData = dataSource.getPosts(
                limit = limit,
                startAfter = snapshot,
            )
            val posts = pagedData.items.map { PostMapper.toDomain(it) }
            val nextCursor = pagedData.nextCursor?.let { PaginationCursor(it, PostSortBy.NEWEST) }
            val result = PaginatedResult(posts, nextCursor)

            Logger.d { "Successfully fetched posts by user: $result" }
            return Result.Success(result)
        } catch (e: FirebaseFirestoreException) {
            Logger.e(e) { "Error fetching posts" }
            return Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching posts" }
            return Result.Error(SystemError.Unknown)
        }
    }

    override suspend fun getPostById(id: PostId): Result<Post, OperationError> {
        try {
            Logger.d { "Fetching post by id: $id" }
            val dto = dataSource.getPostById(id.value) ?: return Result.Error(FirestoreError.NotFound)
            val post = PostMapper.toDomain(dto)
            Logger.d { "Successfully fetched post by id: $post" }
            return Result.Success(post)
        } catch (e: FirebaseFirestoreException) {
            Logger.e(e) { "Error fetching post by id: $id" }
            return Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching post by id: $id" }
            return Result.Error(SystemError.Unknown)
        }
    }

    override suspend fun getPostsByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> {
        try {
            Logger.d { "Fetching posts by user: userId=$userId, cursor=$cursor, limit=$limit" }

            val snapshot = (cursor as? PaginationCursor<*>)?.value as? DocumentSnapshot
            val pagedData = dataSource.getPostsByUser(
                userId = userId.value,
                limit = limit,
                startAfter = snapshot,
            )
            val posts = pagedData.items.map { PostMapper.toDomain(it) }
            val nextCursor = pagedData.nextCursor?.let { PaginationCursor(it, PostSortBy.NEWEST) }
            val result = PaginatedResult(posts, nextCursor)

            Logger.d { "Successfully fetched posts by user: $result" }
            return Result.Success(result)
        } catch (e: FirebaseFirestoreException) {
            Logger.e(e) { "Error fetching posts by user" }
            return Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching posts by user" }
            return Result.Error(SystemError.Unknown)
        }
    }
}
