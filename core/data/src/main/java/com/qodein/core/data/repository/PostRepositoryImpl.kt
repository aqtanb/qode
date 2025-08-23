package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestorePostDataSource
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PostSortBy
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(private val dataSource: FirestorePostDataSource) : PostRepository {

    override fun createPost(post: Post): Flow<Post> =
        flow {
            emit(dataSource.createPost(post))
        }

    override fun getPosts(
        query: String?,
        sortBy: PostSortBy,
        filterByTag: String?,
        filterByAuthor: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Post>> =
        flow {
            emit(
                dataSource.getPosts(
                    query = query,
                    sortBy = sortBy,
                    filterByTag = filterByTag,
                    filterByAuthor = filterByAuthor,
                    limit = limit,
                    offset = offset,
                ),
            )
        }

    override fun getPostById(id: PostId): Flow<Post?> =
        flow {
            emit(dataSource.getPostById(id))
        }

    override fun updatePost(post: Post): Flow<Post> =
        flow {
            emit(dataSource.updatePost(post))
        }

    override fun deletePost(
        id: PostId,
        authorId: UserId
    ): Flow<Unit> =
        flow {
            dataSource.deletePost(id, authorId)
            emit(Unit)
        }

    override fun voteOnPost(
        postId: PostId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Post> =
        flow {
            emit(dataSource.voteOnPost(postId, userId, isUpvote))
        }

    override fun removePostVote(
        postId: PostId,
        userId: UserId
    ): Flow<Post> =
        flow {
            emit(dataSource.removePostVote(postId, userId))
        }

    override fun bookmarkPost(
        postId: PostId,
        userId: UserId,
        isBookmarked: Boolean
    ): Flow<Post> =
        flow {
            emit(dataSource.bookmarkPost(postId, userId, isBookmarked))
        }

    override fun incrementShareCount(id: PostId): Flow<Unit> =
        flow {
            dataSource.incrementShareCount(id)
            emit(Unit)
        }

    override fun getPostsByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<Post>> =
        flow {
            emit(dataSource.getPostsByUser(userId, limit, offset))
        }

    override fun getPostsByTag(
        tagName: String,
        limit: Int,
        offset: Int
    ): Flow<List<Post>> =
        flow {
            emit(dataSource.getPostsByTag(tagName, limit, offset))
        }

    override fun getBookmarkedPosts(
        userId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<Post>> =
        flow {
            emit(dataSource.getBookmarkedPosts(userId, limit, offset))
        }

    override fun getTrendingPosts(
        timeWindow: Int,
        limit: Int
    ): Flow<List<Post>> =
        flow {
            emit(dataSource.getTrendingPosts(timeWindow, limit))
        }

    override fun observePosts(ids: List<PostId>): Flow<List<Post>> = dataSource.observePosts(ids)
}
