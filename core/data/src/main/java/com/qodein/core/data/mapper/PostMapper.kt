package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.PostDto
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

object PostMapper {

    fun toDomain(dto: PostDto): Post =
        Post.fromDto(
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
            createdAt = Timestamp(post.createdAt.epochSeconds, 0),
            updatedAt = Timestamp(post.updatedAt.epochSeconds, 0),
        )
}
