package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.qodein.shared.model.Tag.Companion.MAX_TAGS_SELECTED

// MARK: - POST DTOs

data class PostDto(
    @DocumentId
    val id: String = "",

    @PropertyName("authorId")
    val authorId: String = "",

    @PropertyName("authorName")
    val authorName: String = "",

    @PropertyName("authorAvatarUrl")
    val authorAvatarUrl: String? = null,

    @PropertyName("title")
    val title: String = "",

    @PropertyName("content")
    val content: String? = null,

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @PropertyName("tags")
    val tags: List<String> = emptyList(),

    @PropertyName("upvotes")
    val upvotes: Int = 0,

    @PropertyName("downvotes")
    val downvotes: Int = 0,

    @PropertyName("shares")
    val shares: Int = 0,

    @PropertyName("commentCount")
    val commentCount: Int = 0,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    constructor() : this(
        id = "",
        authorId = "",
        authorName = "",
        authorAvatarUrl = null,
        title = "",
        content = "",
        imageUrls = emptyList(),
        tags = emptyList(),
        upvotes = 0,
        downvotes = 0,
        shares = 0,
        commentCount = 0,
        createdAt = null,
        updatedAt = null,
    )
}

// MARK: - API REQUEST DTOs

data class CreatePostRequest(
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
        require(tags.size <= MAX_TAGS_SELECTED) { "Post cannot have more than 5 tags" }
        require(imageUrls.size <= 5) { "Post cannot have more than 5 images" }
    }
}

// MARK: - CLIENT RESPONSE DTOs

data class PostWithInteractionDto(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val content: String?,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val commentCount: Int = 0,
    val voteScore: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val userVoteState: String = "NONE",
    val userBookmarked: Boolean = false
)

data class PostSummaryDto(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val contentPreview: String? = null,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val commentCount: Int = 0,
    val voteScore: Int = 0,
    val createdAt: Timestamp? = null,
    val userVoteState: String = "NONE"
)

// MARK: - PAGINATION DTOs

data class PostPageDto(val posts: List<PostSummaryDto>, val nextPageToken: String? = null, val hasMore: Boolean = false)

// MARK: - TAG DTOs

/**
 * Firestore document model for Tag.
 * Represents tags used for post categorization.
 * Document ID = tag value (lowercase, sanitized [a-z0-9_-])
 */
data class TagDto(
    @DocumentId
    val documentId: String = "", // The tag value itself

    @PropertyName("postCount")
    val postCount: Int = 0, // Denormalized count of posts using this tag

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null
) {
    // No-args constructor required for Firestore deserialization
    constructor() : this("", 0, null)
}
