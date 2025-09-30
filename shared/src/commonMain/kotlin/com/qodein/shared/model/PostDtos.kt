@file:UseContextualSerialization(Instant::class)

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.Instant

// MARK:

/**
 * Firestore document DTO for Post - matches actual Firestore document structure
 */
@Serializable
data class PostDto(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(), // Simple string array for Firestore
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

/**
 * DTO for creating a new post via API
 */
@Serializable
data class CreatePostDto(
    val title: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) {
    init {
        require(title.isNotBlank()) { "Post title cannot be blank" }
        require(title.length <= 200) { "Post title cannot exceed 200 characters" }
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 2000) { "Post content cannot exceed 2000 characters" }
        require(tags.size <= 10) { "Post cannot have more than 10 tags" }
        require(imageUrls.size <= 5) { "Post cannot have more than 5 images" }
    }
}

/**
 * DTO for updating an existing post via API
 */
@Serializable
data class UpdatePostDto(
    val title: String? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null,
    val tags: List<String>? = null
) {
    init {
        title?.let {
            require(it.isNotBlank()) { "Post title cannot be blank" }
            require(it.length <= 200) { "Post title cannot exceed 200 characters" }
        }
        content?.let {
            require(it.isNotBlank()) { "Post content cannot be blank" }
            require(it.length <= 2000) { "Post content cannot exceed 2000 characters" }
        }
        tags?.let { require(it.size <= 10) { "Post cannot have more than 10 tags" } }
        imageUrls?.let { require(it.size <= 5) { "Post cannot have more than 5 images" } }
    }
}

// MARK:

/**
 * Firestore DTO for user-post interactions (stored in separate collection)
 * Path: /user_post_interactions/{userId}_{postId}
 */
@Serializable
data class UserPostInteractionDto(
    val userId: String = "",
    val postId: String = "",
    val voteType: String? = null, // "upvote", "downvote", or null
    val hasShared: Boolean = false,
    val lastInteractionAt: Instant? = null
)

/**
 * DTO for post voting operations
 */
@Serializable
data class PostVoteDto(val postId: String, val voteType: VoteType)

@Serializable
enum class VoteType {
    UPVOTE,
    DOWNVOTE,
    REMOVE_VOTE
}

// MARK:

/**
 * DTO for post responses to clients (combines post data with user interactions)
 */
@Serializable
data class PostWithUserInteractionDto(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val commentCount: Int = 0,
    val voteScore: Int = 0,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    // User-specific data (populated from separate interaction document)
    val userVoteType: String? = null, // "upvote", "downvote", or null
    val userHasShared: Boolean = false
)

/**
 * DTO for post summary in feed/list views
 */
@Serializable
data class PostSummaryDto(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val contentPreview: String, // Truncated content
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val commentCount: Int = 0,
    val voteScore: Int = 0,
    val createdAt: Instant? = null,
    val userVoteType: String? = null
)

// MARK:

/**
 * DTO for paginated post responses
 */
@Serializable
data class PostPageDto(val posts: List<PostSummaryDto>, val nextPageToken: String? = null, val hasMore: Boolean = false)

/**
 * DTO for post feed requests
 */
@Serializable
data class PostFeedRequestDto(
    val pageToken: String? = null,
    val limit: Int = 20,
    val tags: List<String> = emptyList(),
    val authorId: String? = null,
    val sortBy: PostSortOrder = PostSortOrder.CREATED_AT_DESC
) {
    init {
        require(limit in 1..100) { "Limit must be between 1 and 100" }
    }
}

@Serializable
enum class PostSortOrder {
    CREATED_AT_DESC,
    VOTE_SCORE_DESC,
    COMMENT_COUNT_DESC
}

// MARK:

/**
 * Convert domain Post to Firestore PostDto
 */
fun Post.toFirestoreDto(): PostDto =
    PostDto(
        id = id.value,
        authorId = authorId.value,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags.map { it.value },
        upvotes = upvotes,
        downvotes = downvotes,
        shares = shares,
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Convert Firestore PostDto to domain Post
 */
fun PostDto.toDomainPost(): Post =
    Post(
        id = PostId(id),
        authorId = UserId(authorId),
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags.map { Tag(it) },
        upvotes = upvotes,
        downvotes = downvotes,
        shares = shares,
        commentCount = commentCount,
        createdAt = createdAt ?: kotlin.time.Clock.System.now(),
        updatedAt = updatedAt ?: kotlin.time.Clock.System.now(),
    )

/**
 * Convert PostDto + UserPostInteractionDto to PostWithUserInteractionDto
 */
fun PostDto.withUserInteraction(interaction: UserPostInteractionDto?): PostWithUserInteractionDto =
    PostWithUserInteractionDto(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags,
        upvotes = upvotes,
        downvotes = downvotes,
        shares = shares,
        commentCount = commentCount,
        voteScore = upvotes - downvotes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userVoteType = interaction?.voteType,
        userHasShared = interaction?.hasShared ?: false,
    )

/**
 * Convert PostWithUserInteractionDto to PostSummaryDto with content truncation
 */
fun PostWithUserInteractionDto.toSummaryDto(maxContentLength: Int = 200): PostSummaryDto {
    val truncatedContent = if (content.length > maxContentLength) {
        content.take(maxContentLength) + "..."
    } else {
        content
    }

    return PostSummaryDto(
        id = id,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        contentPreview = truncatedContent,
        imageUrls = imageUrls,
        tags = tags,
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        voteScore = voteScore,
        createdAt = createdAt,
        userVoteType = userVoteType,
    )
}
