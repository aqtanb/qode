@file:UseContextualSerialization(Instant::class)

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PostError
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class PostId(val value: String) {
    init {
        require(value.isNotBlank()) { "Post ID cannot be blank" }
        require(value.length == 32) { "Post ID must be 32 characters (UUID without hyphens)" }
        require(value.matches(Regex("^[a-f0-9]+$"))) { "Post ID must be lowercase hex (UUID format)" }
    }

    override fun toString(): String = value
}

@Serializable
@JvmInline
value class Tag(val value: String) {
    init {
        require(value.isNotBlank()) { "Tag cannot be blank" }
        require(value.length <= 50) { "Tag cannot exceed 50 characters" }
        require(value == value.lowercase()) { "Tag must be lowercase" }
        require(value.matches(Regex("^[a-z0-9_-]+$"))) { "Tag contains invalid characters" }
    }

    override fun toString(): String = value

    companion object {
        fun create(name: String): Result<Tag, PostError.CreationFailure> {
            val cleanName = name.trim()
                .lowercase()
                .replace(Regex("\\s+"), "_")
                .replace(Regex("[^a-z0-9_-]"), "")

            return if (cleanName.isBlank()) {
                Result.Error(PostError.CreationFailure.InvalidTagData)
            } else {
                Result.Success(Tag(cleanName))
            }
        }
    }
}

@Serializable
data class Post(
    val id: PostId,
    val authorId: UserId,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val title: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val commentCount: Int = 0, // Denormalized, updated via Cloud Functions
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(title.isNotBlank()) { "Post title cannot be blank" }
        require(title.length <= 200) { "Post title cannot exceed 200 characters" }
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 2000) { "Post content cannot exceed 2000 characters" }
        require(authorName.isNotBlank()) { "Author username cannot be blank" }
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(shares >= 0) { "Shares cannot be negative" }
        require(commentCount >= 0) { "Comment count cannot be negative" }
        require(tags.size <= 10) { "Post cannot have more than 10 tags" }
        require(imageUrls.size <= 5) { "Post cannot have more than 5 images" }
    }

    val voteScore: Int get() = upvotes - downvotes

    companion object {
        fun create(
            authorId: UserId,
            authorUsername: String,
            title: String,
            content: String,
            imageUrls: List<String> = emptyList(),
            tags: List<String> = emptyList(),
            authorAvatarUrl: String? = null
        ): Result<Post, PostError.CreationFailure> {
            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            val cleanAuthorName = authorUsername.trim()
            val cleanImageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() }

            // Validate inputs
            if (cleanTitle.isBlank()) {
                return Result.Error(PostError.CreationFailure.EmptyTitle)
            }
            if (cleanTitle.length > 200) {
                return Result.Error(PostError.CreationFailure.TitleTooLong)
            }
            if (cleanContent.isBlank()) {
                return Result.Error(PostError.CreationFailure.EmptyContent)
            }
            if (cleanContent.length > 2000) {
                return Result.Error(PostError.CreationFailure.ContentTooLong)
            }
            if (cleanAuthorName.isBlank()) {
                return Result.Error(PostError.CreationFailure.EmptyAuthorName)
            }
            if (tags.size > 10) {
                return Result.Error(PostError.CreationFailure.TooManyTags)
            }
            if (cleanImageUrls.size > 5) {
                return Result.Error(PostError.CreationFailure.TooManyImages)
            }

            // Process tags
            val cleanTags = mutableListOf<Tag>()
            for (tagName in tags) {
                when (val tagResult = Tag.create(tagName)) {
                    is Result.Success -> cleanTags.add(tagResult.data)
                    is Result.Error -> return Result.Error(tagResult.error)
                }
            }

            return Result.Success(
                Post(
                    id = PostId(generateRandomId()),
                    authorId = authorId,
                    authorName = cleanAuthorName,
                    authorAvatarUrl = authorAvatarUrl?.trim(),
                    title = cleanTitle,
                    content = cleanContent,
                    imageUrls = cleanImageUrls,
                    tags = cleanTags,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                ),
            )
        }

        @OptIn(ExperimentalUuidApi::class)
        private fun generateRandomId(): String = Uuid.random().toHexString()
    }
}
