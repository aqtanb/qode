@file:OptIn(ExperimentalTime::class)

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PostError
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class PostId(val value: String) {
    init {
        require(value.isNotBlank()) { "Post ID cannot be blank" }
    }

    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun generate(): PostId {
            val raw = Uuid.random().toHexString()
            return PostId(raw)
        }
    }
}

@ConsistentCopyVisibility
@Serializable
data class Post private constructor(
    val id: PostId,
    val authorId: UserId,
    val authorName: String,
    val authorAvatarUrl: String?,
    val title: String,
    val content: String,
    val imageUrls: List<String>,
    val tags: List<String>,
    val upvotes: Int,
    val downvotes: Int,
    val voteScore: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        const val TITLE_MAX_LENGTH = 50
        const val CONTENT_MAX_LENGTH = 1000
        const val MAX_TAGS = 5
        const val TAG_MAX_LENGTH = 15
        const val MAX_IMAGES = 5

        fun create(
            authorId: UserId,
            authorName: String,
            title: String,
            content: String,
            imageUrls: List<String>,
            tags: List<String>,
            authorAvatarUrl: String?
        ): Result<Post, PostError.CreationFailure> {
            val cleanTitle = filterTitle(title.trim())
            if (cleanTitle.isBlank()) return Result.Error(PostError.CreationFailure.EmptyTitle)

            val cleanContent = filterContent(content.trim())

            if (tags.size > MAX_TAGS) return Result.Error(PostError.CreationFailure.TooManyTags)
            val cleanTags = tags.map { filterTagInput(it).trim() }.filter { it.isNotBlank() }.distinct()

            val cleanImageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() }
            if (cleanImageUrls.size > MAX_IMAGES) return Result.Error(PostError.CreationFailure.TooManyImages)

            val now = Clock.System.now()
            return Result.Success(
                Post(
                    id = PostId.generate(),
                    authorId = authorId,
                    authorName = authorName,
                    authorAvatarUrl = authorAvatarUrl,
                    title = cleanTitle,
                    content = cleanContent,
                    imageUrls = cleanImageUrls,
                    tags = cleanTags,
                    upvotes = 0,
                    downvotes = 0,
                    voteScore = 0,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        fun fromDto(
            id: PostId,
            authorId: UserId,
            authorName: String,
            authorAvatarUrl: String?,
            title: String,
            content: String,
            imageUrls: List<String>,
            tags: List<String>,
            upvotes: Int,
            downvotes: Int,
            voteScore: Int,
            createdAt: Instant,
            updatedAt: Instant
        ): Post =
            Post(
                id, authorId, authorName, authorAvatarUrl, title, content,
                imageUrls, tags, upvotes, downvotes, voteScore, createdAt, updatedAt,
            )

        fun filterTitle(input: String): String = input.take(TITLE_MAX_LENGTH)
        fun filterContent(input: String): String = input.take(CONTENT_MAX_LENGTH)
        fun filterTagInput(input: String): String =
            input.lowercase()
                .filter { it.isLetterOrDigit() || it == '_' }
                .take(TAG_MAX_LENGTH)
    }
}
