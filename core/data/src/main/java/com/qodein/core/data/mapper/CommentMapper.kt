package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.CommentDto
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object CommentMapper {

    fun toDomain(dto: CommentDto): Comment {
        require(dto.id.isNotBlank()) { "Comment ID cannot be blank" }
        require(dto.parentId.isNotBlank()) { "Comment parent ID cannot be blank" }
        require(dto.parentType.isNotBlank()) { "Comment parent type cannot be blank" }
        require(dto.authorId.isNotBlank()) { "Comment author ID cannot be blank" }
        require(dto.authorUsername.isNotBlank()) { "Comment author username cannot be blank" }
        require(dto.content.isNotBlank()) { "Comment content cannot be blank" }

        val parentType = when (dto.parentType.uppercase()) {
            "PROMO_CODE" -> CommentParentType.PROMO_CODE
            "POST" -> CommentParentType.POST
            else -> throw IllegalArgumentException("Unknown parent type: ${dto.parentType}")
        }

        return Comment(
            id = CommentId(dto.id),
            parentId = dto.parentId,
            parentType = parentType,
            authorId = UserId(dto.authorId),
            authorUsername = dto.authorUsername,
            authorAvatarUrl = dto.authorAvatarUrl,
            authorCountry = dto.authorCountry,
            content = dto.content,
            imageUrls = dto.imageUrls,
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            isUpvotedByCurrentUser = dto.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = dto.isDownvotedByCurrentUser,
        )
    }

    fun toDto(domain: Comment): CommentDto =
        CommentDto(
            id = domain.id.value,
            parentId = domain.parentId,
            parentType = domain.parentType.name,
            authorId = domain.authorId.value,
            authorUsername = domain.authorUsername,
            authorAvatarUrl = domain.authorAvatarUrl,
            authorCountry = domain.authorCountry,
            content = domain.content,
            imageUrls = domain.imageUrls,
            upvotes = domain.upvotes,
            downvotes = domain.downvotes,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            isUpvotedByCurrentUser = domain.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = domain.isDownvotedByCurrentUser,
        )

    fun toDomainList(dtos: List<CommentDto>): List<Comment> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(comments: List<Comment>): List<CommentDto> = comments.map { toDto(it) }
}
