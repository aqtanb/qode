package com.qodein.core.data.mapper

import com.qodein.core.data.dto.PostDto
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toKotlinInstant

object PostMapper {

    val now = Clock.System.now()
    fun toDomain(dto: PostDto): Post =
        Post.fromDto(
            id = PostId(dto.id),
            authorId = UserId(dto.authorId),
            authorName = dto.authorName,
            authorAvatarUrl = dto.authorAvatarUrl,
            title = dto.title,
            content = dto.content,
            imageUrls = dto.imageUrls,
            tags = dto.tags,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            voteScore = dto.voteScore,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: now,
            updatedAt = dto.updatedAt?.toInstant()?.toKotlinInstant() ?: now,
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
            tags = post.tags,
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            voteScore = post.voteScore,
            createdAt = null,
            updatedAt = null,
        )
}
