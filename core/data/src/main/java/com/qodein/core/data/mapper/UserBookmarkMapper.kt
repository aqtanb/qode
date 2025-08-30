package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.UserBookmarkDto
import com.qodein.shared.model.BookmarkType
import com.qodein.shared.model.UserBookmark
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object UserBookmarkMapper {

    fun toDomain(dto: UserBookmarkDto): UserBookmark {
        require(dto.id.isNotBlank()) { "UserBookmark ID cannot be blank" }
        require(dto.userId.isNotBlank()) { "UserBookmark user ID cannot be blank" }
        require(dto.itemId.isNotBlank()) { "UserBookmark item ID cannot be blank" }
        require(dto.itemType.isNotBlank()) { "UserBookmark item type cannot be blank" }
        require(dto.itemTitle.isNotBlank()) { "UserBookmark item title cannot be blank" }

        val itemType = when (dto.itemType.uppercase()) {
            "PROMO_CODE" -> BookmarkType.PROMO_CODE
            "POST" -> BookmarkType.POST
            else -> throw IllegalArgumentException("Unknown bookmark type: ${dto.itemType}")
        }

        return UserBookmark(
            id = dto.id,
            userId = UserId(dto.userId),
            itemId = dto.itemId,
            itemType = itemType,
            itemTitle = dto.itemTitle,
            itemCategory = dto.itemCategory,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: UserBookmark): UserBookmarkDto =
        UserBookmarkDto(
            id = domain.id,
            userId = domain.userId.value,
            itemId = domain.itemId,
            itemType = domain.itemType.name,
            itemTitle = domain.itemTitle,
            itemCategory = domain.itemCategory,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
        )

    fun toDomainList(dtos: List<UserBookmarkDto>): List<UserBookmark> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(bookmarks: List<UserBookmark>): List<UserBookmarkDto> = bookmarks.map { toDto(it) }
}
