package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreCommentDataSource
import com.qodein.shared.domain.repository.CommentRepository
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(private val dataSource: FirestoreCommentDataSource) : CommentRepository {

    override fun createComment(
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId,
        authorUsername: String,
        authorAvatarUrl: String?,
        authorCountry: String?,
        content: String,
        imageUrls: List<String>
    ): Flow<Comment> =
        flow {
            emit(
                dataSource.createComment(
                    parentId = parentId,
                    parentType = parentType,
                    authorId = authorId,
                    authorUsername = authorUsername,
                    authorAvatarUrl = authorAvatarUrl,
                    authorCountry = authorCountry,
                    content = content,
                    imageUrls = imageUrls,
                ),
            )
        }

    override fun getComments(
        parentId: String,
        parentType: CommentParentType,
        limit: Int,
        offset: Int
    ): Flow<List<Comment>> =
        flow {
            emit(dataSource.getComments(parentId, parentType, limit, offset))
        }

    override fun getCommentById(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType
    ): Flow<Comment?> =
        flow {
            emit(dataSource.getCommentById(commentId, parentId, parentType))
        }

    override fun updateComment(comment: Comment): Flow<Comment> =
        flow {
            emit(dataSource.updateComment(comment))
        }

    override fun deleteComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId
    ): Flow<Unit> =
        flow {
            dataSource.deleteComment(commentId, parentId, parentType, authorId)
            emit(Unit)
        }

    override fun voteOnComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Comment> =
        flow {
            emit(
                dataSource.voteOnComment(
                    commentId = commentId,
                    parentId = parentId,
                    parentType = parentType,
                    userId = userId,
                    isUpvote = isUpvote,
                ),
            )
        }

    override fun removeCommentVote(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId
    ): Flow<Comment> =
        flow {
            emit(
                dataSource.removeCommentVote(
                    commentId = commentId,
                    parentId = parentId,
                    parentType = parentType,
                    userId = userId,
                ),
            )
        }

    override fun getCommentsByUser(
        authorId: UserId,
        limit: Int,
        offset: Int
    ): Flow<List<Comment>> =
        flow {
            emit(dataSource.getCommentsByUser(authorId, limit, offset))
        }

    override fun observeComments(
        parentId: String,
        parentType: CommentParentType
    ): Flow<List<Comment>> = dataSource.observeComments(parentId, parentType)
}
