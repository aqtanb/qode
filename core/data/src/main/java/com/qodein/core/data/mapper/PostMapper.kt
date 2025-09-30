package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.PostDto
import com.qodein.core.data.model.PostSummaryDto
import com.qodein.core.data.model.PostWithInteractionDto
import com.qodein.core.data.model.UserInteractionDto
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

object PostMapper {

    fun toDomain(dto: PostDto): Post =
        Post(
            id = PostId(dto.id),
            authorId = UserId(dto.authorId),
            authorName = dto.authorName,
            authorAvatarUrl = dto.authorAvatarUrl,
            title = dto.title,
            content = dto.content,
            imageUrls = dto.imageUrls,
            tags = dto.tags.map { Tag(value = it, postCount = 0) },
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            shares = dto.shares,
            commentCount = dto.commentCount,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Instant.fromEpochSeconds(0),
            updatedAt = dto.updatedAt?.toInstant()?.toKotlinInstant() ?: Instant.fromEpochSeconds(0),
        )

    fun toDto(post: Post): PostDto =
        PostDto(
            id = post.id.value,
            authorId = post.authorId.value,
            authorName = post.authorName,
            authorAvatarUrl = post.authorAvatarUrl,
            title = post.title,
            content = post.content,
            imageUrls = post.imageUrls,
            tags = post.tags.map { it.value },
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            shares = post.shares,
            commentCount = post.commentCount,
            createdAt = Timestamp(post.createdAt.epochSeconds, 0),
            updatedAt = Timestamp(post.updatedAt.epochSeconds, 0),
        )

    fun withInteraction(
        dto: PostDto,
        interaction: UserInteractionDto?
    ): PostWithInteractionDto =
        PostWithInteractionDto(
            id = dto.id,
            authorId = dto.authorId,
            authorName = dto.authorName,
            authorAvatarUrl = dto.authorAvatarUrl,
            title = dto.title,
            content = dto.content,
            imageUrls = dto.imageUrls,
            tags = dto.tags,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            shares = dto.shares,
            commentCount = dto.commentCount,
            voteScore = dto.upvotes - dto.downvotes,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            userVoteState = interaction?.voteState ?: "NONE",
            userBookmarked = interaction?.isBookmarked ?: false,
        )

    fun toSummary(
        dto: PostWithInteractionDto,
        maxContentLength: Int = 200
    ): PostSummaryDto {
        val truncatedContent = if (dto.content.length > maxContentLength) {
            dto.content.take(maxContentLength) + "..."
        } else {
            dto.content
        }

        return PostSummaryDto(
            id = dto.id,
            authorName = dto.authorName,
            authorAvatarUrl = dto.authorAvatarUrl,
            title = dto.title,
            contentPreview = truncatedContent,
            imageUrls = dto.imageUrls,
            tags = dto.tags,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            commentCount = dto.commentCount,
            voteScore = dto.voteScore,
            createdAt = dto.createdAt,
            userVoteState = dto.userVoteState,
        )
    }

    fun toDomainList(dtos: List<PostDto>): List<Post> = dtos.map { toDomain(it) }

    fun toDtoList(posts: List<Post>): List<PostDto> = posts.map { toDto(it) }
}
