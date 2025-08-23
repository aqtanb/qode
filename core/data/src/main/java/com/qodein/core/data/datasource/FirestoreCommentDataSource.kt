package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.CommentMapper
import com.qodein.core.data.model.CommentDto
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCommentDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreCommentDS"
        private const val PROMOCODES_COLLECTION = "promocodes"
        private const val POSTS_COLLECTION = "posts"
        private const val COMMENTS_SUBCOLLECTION = "comments"
        private const val VOTES_SUBCOLLECTION = "votes"
    }

    suspend fun createComment(
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId,
        authorUsername: String,
        authorAvatarUrl: String?,
        authorCountry: String?,
        content: String,
        imageUrls: List<String>
    ): Comment {
        val commentId = generateCommentId()

        val comment = Comment(
            id = CommentId(commentId),
            parentId = parentId,
            parentType = parentType,
            authorId = authorId,
            authorUsername = authorUsername,
            authorAvatarUrl = authorAvatarUrl,
            authorCountry = authorCountry,
            content = content,
            imageUrls = imageUrls,
        )

        val dto = CommentMapper.toDto(comment)
        val parentCollection = getParentCollection(parentType)

        try {
            firestore.collection(parentCollection)
                .document(parentId)
                .collection(COMMENTS_SUBCOLLECTION)
                .document(commentId)
                .set(dto)
                .await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in createComment")

            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                    throw SecurityException("permission denied: cannot create comment", e)
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw IOException("connection error while creating comment", e)
                }
                else -> {
                    throw IllegalStateException("service unavailable: failed to create comment", e)
                }
            }
        }

        return comment
    }

    suspend fun getComments(
        parentId: String,
        parentType: CommentParentType,
        limit: Int,
        offset: Int
    ): List<Comment> {
        val parentCollection = getParentCollection(parentType)

        var query: Query = firestore.collection(parentCollection)
            .document(parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        // Note: Firestore doesn't have offset(), implement cursor-based pagination if needed

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<CommentDto>()?.let { dto ->
                    CommentMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse comment ${document.id}")
                null
            }
        }
    }

    suspend fun getCommentById(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType
    ): Comment? {
        val parentCollection = getParentCollection(parentType)

        val document = firestore.collection(parentCollection)
            .document(parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(commentId.value)
            .get()
            .await()

        return document.toObject<CommentDto>()?.let { dto ->
            CommentMapper.toDomain(dto)
        }
    }

    suspend fun updateComment(comment: Comment): Comment {
        val dto = CommentMapper.toDto(comment)
        val parentCollection = getParentCollection(comment.parentType)

        firestore.collection(parentCollection)
            .document(comment.parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(comment.id.value)
            .set(dto)
            .await()

        return comment
    }

    suspend fun deleteComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId
    ) {
        val parentCollection = getParentCollection(parentType)
        val commentRef = firestore.collection(parentCollection)
            .document(parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(commentId.value)

        // Check if user owns the comment
        val commentDoc = commentRef.get().await()
        val commentDto = commentDoc.toObject<CommentDto>()
            ?: throw IllegalArgumentException("comment not found: ${commentId.value}")

        if (commentDto.authorId != authorId.value) {
            throw SecurityException("permission denied: cannot delete comment owned by another user")
        }

        commentRef.delete().await()
    }

    suspend fun voteOnComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId,
        isUpvote: Boolean
    ): Comment {
        val parentCollection = getParentCollection(parentType)
        val commentRef = firestore.collection(parentCollection)
            .document(parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(commentId.value)

        val voteId = "${userId.value}_${commentId.value}"
        val voteRef = commentRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Use batch write to ensure atomicity
        val batch = firestore.batch()

        // Add/update vote
        val voteData = mapOf(
            "userId" to userId.value,
            "commentId" to commentId.value,
            "isUpvote" to isUpvote,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        batch.set(voteRef, voteData)

        // Update vote counts on comment
        if (isUpvote) {
            batch.update(commentRef, "upvotes", FieldValue.increment(1))
        } else {
            batch.update(commentRef, "downvotes", FieldValue.increment(1))
        }

        batch.commit().await()

        // Return updated comment
        val updatedDoc = commentRef.get().await()
        val updatedDto = updatedDoc.toObject<CommentDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated comment")

        return CommentMapper.toDomain(updatedDto)
    }

    suspend fun removeCommentVote(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId
    ): Comment {
        val parentCollection = getParentCollection(parentType)
        val commentRef = firestore.collection(parentCollection)
            .document(parentId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(commentId.value)

        val voteId = "${userId.value}_${commentId.value}"
        val voteRef = commentRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Get existing vote to know which counter to decrement
        val existingVoteSnapshot = voteRef.get().await()
        val existingVote = existingVoteSnapshot.data

        if (existingVote != null) {
            val isUpvote = existingVote["isUpvote"] as Boolean
            val batch = firestore.batch()

            // Remove vote
            batch.delete(voteRef)

            // Update vote counts on comment
            if (isUpvote) {
                batch.update(commentRef, "upvotes", FieldValue.increment(-1))
            } else {
                batch.update(commentRef, "downvotes", FieldValue.increment(-1))
            }

            batch.commit().await()
        }

        // Return updated comment
        val updatedDoc = commentRef.get().await()
        val updatedDto = updatedDoc.toObject<CommentDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated comment")

        return CommentMapper.toDomain(updatedDto)
    }

    suspend fun getCommentsByUser(
        authorId: UserId,
        limit: Int,
        offset: Int
    ): List<Comment> {
        // Note: Firestore doesn't support collection group queries with complex filters easily
        // For production, consider using a separate comments collection with parent references
        // For now, this is a simplified implementation that searches both collections

        val promoCodeComments = searchCommentsInCollection(PROMOCODES_COLLECTION, authorId, limit / 2)
        val postComments = searchCommentsInCollection(POSTS_COLLECTION, authorId, limit - promoCodeComments.size)

        return (promoCodeComments + postComments)
            .sortedByDescending { it.createdAt }
            .take(limit)
    }

    fun observeComments(
        parentId: String,
        parentType: CommentParentType
    ): Flow<List<Comment>> =
        callbackFlow {
            val parentCollection = getParentCollection(parentType)

            val listener = firestore.collection(parentCollection)
                .document(parentId)
                .collection(COMMENTS_SUBCOLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        val wrappedException = when {
                            error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                SecurityException("permission denied: cannot observe comments", error)
                            error.message?.contains("network", ignoreCase = true) == true ->
                                IOException("connection error while observing comments", error)
                            else -> IllegalStateException("service unavailable: failed to observe comments", error)
                        }
                        close(wrappedException)
                        return@addSnapshotListener
                    }

                    val comments = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject<CommentDto>()?.let { dto ->
                                CommentMapper.toDomain(dto)
                            }
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to parse comment in snapshot ${document.id}")
                            null
                        }
                    } ?: emptyList()

                    trySend(comments)
                }

            awaitClose {
                listener.remove()
            }
        }

    // Helper methods
    private fun getParentCollection(parentType: CommentParentType): String =
        when (parentType) {
            CommentParentType.PROMO_CODE -> PROMOCODES_COLLECTION
            CommentParentType.POST -> POSTS_COLLECTION
        }

    private fun generateCommentId(): String = "comment_${System.currentTimeMillis()}_${(0..999).random()}"

    private suspend fun searchCommentsInCollection(
        collectionName: String,
        authorId: UserId,
        limit: Int
    ): List<Comment> {
        // This is a simplified approach - in production, consider a flattened comments collection
        // or use Firestore's collection group queries with proper indexing

        try {
            // Get all documents in the parent collection first (not scalable for large datasets)
            val parentDocs = firestore.collection(collectionName).limit(100).get().await()

            val comments = mutableListOf<Comment>()

            for (parentDoc in parentDocs.documents) {
                if (comments.size >= limit) break

                val commentsQuery = firestore.collection(collectionName)
                    .document(parentDoc.id)
                    .collection(COMMENTS_SUBCOLLECTION)
                    .whereEqualTo("authorId", authorId.value)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit((limit - comments.size).toLong())
                    .get()
                    .await()

                val userComments = commentsQuery.documents.mapNotNull { document ->
                    try {
                        document.toObject<CommentDto>()?.let { dto ->
                            CommentMapper.toDomain(dto)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                comments.addAll(userComments)
            }

            return comments.take(limit)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error searching comments in collection $collectionName")
            return emptyList()
        }
    }
}
