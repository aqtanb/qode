package com.qodein.shared.domain.repository

import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Post operations.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface PostRepository {

    /**
     * Create a new post.
     *
     * @param post The post to create
     * @return Flow that emits [Post] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun createPost(post: Post): Flow<Post>

    /**
     * Get posts with filtering and sorting.
     *
     * @param query Search query text (optional)
     * @param sortBy Sort criteria (newest, popular, etc.)
     * @param filterByTag Filter by specific tag (optional)
     * @param filterByAuthor Filter by author username (optional)
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Post]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPosts(
        query: String? = null,
        sortBy: PostSortBy = PostSortBy.NEWEST,
        filterByTag: String? = null,
        filterByAuthor: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Post>>

    /**
     * Get a specific post by ID.
     *
     * @param id The post ID
     * @return Flow that emits [Post] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPostById(id: PostId): Flow<Post?>

    /**
     * Update a post.
     *
     * @param post The updated post
     * @return Flow that emits [Post] on successful update
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     * @throws java.lang.SecurityException when user lacks permission to update
     */
    fun updatePost(post: Post): Flow<Post>

    /**
     * Delete a post.
     *
     * @param id The post ID to delete
     * @param authorId The user requesting deletion (for permission check)
     * @return Flow that emits [Unit] on successful deletion
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws java.lang.SecurityException when user lacks permission to delete
     */
    fun deletePost(
        id: PostId,
        authorId: UserId
    ): Flow<Unit>

    /**
     * Vote on a post (upvote or downvote).
     *
     * @param postId The post ID
     * @param userId The user ID casting the vote
     * @param isUpvote true for upvote, false for downvote
     * @return Flow that emits updated [Post] with new vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when user already voted
     */
    fun voteOnPost(
        postId: PostId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Post>

    /**
     * Remove a user's vote from a post.
     *
     * @param postId The post ID
     * @param userId The user ID removing the vote
     * @return Flow that emits updated [Post] with updated vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removePostVote(
        postId: PostId,
        userId: UserId
    ): Flow<Post>

    /**
     * Bookmark/unbookmark a post for a user.
     *
     * @param postId The post ID
     * @param userId The user ID
     * @param isBookmarked true to bookmark, false to remove bookmark
     * @return Flow that emits updated [Post] with bookmark status
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun bookmarkPost(
        postId: PostId,
        userId: UserId,
        isBookmarked: Boolean
    ): Flow<Post>

    /**
     * Increment share count for a post.
     *
     * @param id The post ID
     * @return Flow that emits [Unit] on successful increment
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun incrementShareCount(id: PostId): Flow<Unit>

    /**
     * Get posts created by a specific user.
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Post]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPostsByUser(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Post>>

    /**
     * Get posts by tag.
     *
     * @param tagName The tag name
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Post]> with the specified tag
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPostsByTag(
        tagName: String,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Post>>

    /**
     * Get bookmarked posts for a user.
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Post]> bookmarked by the user
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getBookmarkedPosts(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Post>>

    /**
     * Get real-time updates for specific posts.
     * Useful for live post feeds.
     *
     * @param ids List of post IDs to observe
     * @return Flow that emits List<[Post]> on any changes
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun observePosts(ids: List<PostId>): Flow<List<Post>>

    /**
     * Get trending posts based on recent activity.
     *
     * @param timeWindow Hours to look back for trending calculation (default 24h)
     * @param limit Maximum number of results
     * @return Flow that emits List<[Post]> sorted by trending score
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getTrendingPosts(
        timeWindow: Int = 24,
        limit: Int = 20
    ): Flow<List<Post>>
}

/**
 * Enum for post sorting options.
 */
enum class PostSortBy {
    NEWEST, // Sort by creation date (newest first)
    OLDEST, // Sort by creation date (oldest first)
    POPULAR, // Sort by vote score (upvotes - downvotes)
    MOST_SHARED, // Sort by share count
    TRENDING // Sort by recent activity and engagement
}
