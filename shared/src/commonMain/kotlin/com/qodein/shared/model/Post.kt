package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PostId(val value: String) {
    init {
        require(value.isNotBlank()) { "Post ID cannot be blank" }
    }
}

@Serializable
data class Tag(val id: String, val name: String, val color: String? = null) {
    init {
        require(id.isNotBlank()) { "Tag ID cannot be blank" }
        require(name.isNotBlank()) { "Tag name cannot be blank" }
        require(name.length <= 50) { "Tag name cannot exceed 50 characters" }
        color?.let {
            require(it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "Color must be a valid hex code" }
        }
    }

    companion object {
        fun create(
            name: String,
            color: String? = null
        ): Tag {
            val cleanName = name.trim().lowercase()
            return Tag(
                id = cleanName.replace(Regex("\\s+"), "_"),
                name = cleanName,
                color = color,
            )
        }
    }
}

@Serializable
data class Post(
    val id: PostId,
    val authorId: UserId,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val tags: List<Tag>,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val createdAt: Instant,
    val isLikedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
) {
    init {
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 2000) { "Post content cannot exceed 2000 characters" }
        require(authorUsername.isNotBlank()) { "Author username cannot be blank" }
        require(likes >= 0) { "Likes cannot be negative" }
        require(comments >= 0) { "Comments cannot be negative" }
        require(shares >= 0) { "Shares cannot be negative" }
        require(tags.size <= 10) { "Post cannot have more than 10 tags" }
    }

    val isRecent: Boolean get() =
        (Clock.System.now().epochSeconds - createdAt.epochSeconds) < 86400 // 24 hours

    val engagementScore: Int get() = likes + (comments * 2) + (shares * 3)

    companion object {
        fun create(
            authorId: UserId,
            authorUsername: String,
            content: String,
            tags: List<Tag> = emptyList(),
            authorAvatarUrl: String? = null
        ): Result<Post> =
            runCatching {
                Post(
                    id = PostId(generateId()),
                    authorId = authorId,
                    authorUsername = authorUsername.trim(),
                    authorAvatarUrl = authorAvatarUrl?.trim(),
                    content = content.trim(),
                    tags = tags,
                    createdAt = Clock.System.now(),
                )
            }

        private fun generateId(): String = "post_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
}

@Serializable
data class PostInteraction(val postId: PostId, val userId: UserId, val type: InteractionType, val createdAt: Instant = Clock.System.now())

@Serializable
enum class InteractionType {
    LIKE,
    UNLIKE,
    COMMENT,
    SHARE,
    BOOKMARK,
    UNBOOKMARK
}
