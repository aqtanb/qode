package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.PostDto
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

/**
 * Mapper between Post domain models and Firestore DTOs.
 * Handles conversion between different data representations including Tag objects.
 */
object PostMapper {

    fun toDomain(dto: PostDto): Post {
        // Validate required fields
        require(dto.id.isNotBlank()) { "Post ID cannot be blank" }
        require(dto.authorId.isNotBlank()) { "Author ID cannot be blank" }
        require(dto.authorUsername.isNotBlank()) { "Author username cannot be blank" }
        require(dto.content.isNotBlank()) { "Post content cannot be blank" }

        return Post(
            id = PostId(dto.id),
            authorId = UserId(dto.authorId),
            authorUsername = dto.authorUsername,
            authorAvatarUrl = dto.authorAvatarUrl,
            authorCountry = dto.authorCountry,
            title = dto.title,
            content = dto.content,
            imageUrls = dto.imageUrls,
            tags = mapTagsFromDto(dto.tags),
            upvotes = dto.upvotes,
            downvotes = dto.downvotes,
            shares = dto.shares,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            isUpvotedByCurrentUser = dto.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = dto.isDownvotedByCurrentUser,
            isBookmarkedByCurrentUser = dto.isBookmarkedByCurrentUser,
        )
    }

    fun toDto(domain: Post): PostDto =
        PostDto(
            id = domain.id.value,
            authorId = domain.authorId.value,
            authorUsername = domain.authorUsername,
            authorAvatarUrl = domain.authorAvatarUrl,
            authorCountry = domain.authorCountry,
            title = domain.title,
            content = domain.content,
            imageUrls = domain.imageUrls,
            tags = mapTagsToDto(domain.tags),
            upvotes = domain.upvotes,
            downvotes = domain.downvotes,
            shares = domain.shares,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            isUpvotedByCurrentUser = domain.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = domain.isDownvotedByCurrentUser,
            isBookmarkedByCurrentUser = domain.isBookmarkedByCurrentUser,
        )

    /**
     * Convert Tag objects from DTO format (List<Map<String, Any>>) to domain format (List<Tag>)
     */
    private fun mapTagsFromDto(tagMaps: List<Map<String, Any>>): List<Tag> =
        tagMaps.mapNotNull { tagMap ->
            try {
                val id = tagMap["id"] as? String ?: return@mapNotNull null
                val name = tagMap["name"] as? String ?: return@mapNotNull null
                Tag(id = id, name = name)
            } catch (e: Exception) {
                // Skip invalid tag, don't fail the entire operation
                null
            }
        }

    /**
     * Convert Tag objects from domain format (List<Tag>) to DTO format (List<Map<String, Any>>)
     */
    private fun mapTagsToDto(tags: List<Tag>): List<Map<String, Any>> =
        tags.map { tag ->
            mapOf(
                "id" to tag.id,
                "name" to tag.name,
            )
        }

    fun toDomainList(dtos: List<PostDto>): List<Post> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                // Log error but don't fail entire operation
                null
            }
        }

    fun toDtoList(posts: List<Post>): List<PostDto> = posts.map { toDto(it) }
}
