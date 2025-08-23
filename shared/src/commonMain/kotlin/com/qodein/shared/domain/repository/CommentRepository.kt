package com.qodein.shared.domain.repository

import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Comment operations.
 * Comments are stored in subcollections: /promocodes/{id}/comments/{commentId} or /posts/{id}/comments/{commentId}
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface CommentRepository {

    /**
     * Create a new comment on content (promo code or post).
     *
     * @param parentId The ID of the content being commented on (PromoCodeId.value or PostId.value)
     * @param parentType The type of parent content (PROMO_CODE or POST)
     * @param authorId The user ID creating the comment
     * @param authorUsername The username (denormalized for display)
     * @param authorAvatarUrl The avatar URL (denormalized, optional)
     * @param authorCountry The user's country (denormalized, optional)
     * @param content The comment text content
     * @param imageUrls Optional image URLs for the comment
     * @return Flow that emits [Comment] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun createComment(
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId,
        authorUsername: String,
        authorAvatarUrl: String? = null,
        authorCountry: String? = null,
        content: String,
        imageUrls: List<String> = emptyList()
    ): Flow<Comment>

    /**
     * Get comments for specific content with pagination.
     *
     * @param parentId The ID of the content (PromoCodeId.value or PostId.value)
     * @param parentType The type of parent content (PROMO_CODE or POST)
     * @param limit Maximum number of comments to retrieve
     * @param offset Pagination offset
     * @return Flow that emits List<[Comment]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getComments(
        parentId: String,
        parentType: CommentParentType,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Comment>>

    /**
     * Get a specific comment by ID.
     *
     * @param commentId The comment ID
     * @param parentId The parent content ID (for subcollection access)
     * @param parentType The type of parent content
     * @return Flow that emits [Comment] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getCommentById(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType
    ): Flow<Comment?>

    /**
     * Update a comment (author only).
     *
     * @param comment The updated comment
     * @return Flow that emits updated [Comment] on successful update
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     * @throws java.lang.SecurityException when user lacks permission to update
     */
    fun updateComment(comment: Comment): Flow<Comment>

    /**
     * Delete a comment (author only).
     *
     * @param commentId The comment ID
     * @param parentId The parent content ID (for subcollection access)
     * @param parentType The type of parent content
     * @param authorId The user requesting deletion (for permission check)
     * @return Flow that emits [Unit] on successful deletion
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws java.lang.SecurityException when user lacks permission to delete
     */
    fun deleteComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        authorId: UserId
    ): Flow<Unit>

    /**
     * Vote on a comment (upvote or downvote).
     *
     * @param commentId The comment ID
     * @param parentId The parent content ID (for subcollection access)
     * @param parentType The type of parent content
     * @param userId The user ID casting the vote
     * @param isUpvote true for upvote, false for downvote
     * @return Flow that emits updated [Comment] with new vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when user already voted
     */
    fun voteOnComment(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Comment>

    /**
     * Remove a user's vote from a comment.
     *
     * @param commentId The comment ID
     * @param parentId The parent content ID (for subcollection access)
     * @param parentType The type of parent content
     * @param userId The user ID removing the vote
     * @return Flow that emits updated [Comment] with updated vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removeCommentVote(
        commentId: CommentId,
        parentId: String,
        parentType: CommentParentType,
        userId: UserId
    ): Flow<Comment>

    /**
     * Get comments created by a specific user across all content.
     *
     * @param authorId The user ID
     * @param limit Maximum number of comments
     * @param offset Pagination offset
     * @return Flow that emits List<[Comment]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getCommentsByUser(
        authorId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Comment>>

    /**
     * Get real-time updates for comments on specific content.
     * Useful for live comment feeds.
     *
     * @param parentId The parent content ID
     * @param parentType The type of parent content
     * @return Flow that emits List<[Comment]> on any changes to comments
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun observeComments(
        parentId: String,
        parentType: CommentParentType
    ): Flow<List<Comment>>
}
