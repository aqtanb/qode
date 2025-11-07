package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.UserInteractionDto
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

/**
 * Mapper between UserInteraction domain model and UserInteractionDto data model.
 * Handles conversion between Kotlin types and Firestore-compatible types.
 */
class UserInteractionMapper constructor() {

    /**
     * Convert domain model to DTO for Firestore storage
     */
    fun toDto(userInteraction: UserInteraction): UserInteractionDto =
        UserInteractionDto(
            documentId = userInteraction.id,
            itemId = userInteraction.itemId,
            itemType = userInteraction.itemType.name,
            userId = userInteraction.userId.value,
            voteState = userInteraction.voteState.name,
            isBookmarked = userInteraction.isBookmarked,
            createdAt = userInteraction.createdAt.toTimestamp(),
            updatedAt = userInteraction.updatedAt.toTimestamp(),
        )

    /**
     * Convert DTO from Firestore to domain model
     */
    fun fromDto(dto: UserInteractionDto): UserInteraction? =
        try {
            UserInteraction(
                id = dto.documentId,
                itemId = dto.itemId,
                itemType = ContentType.valueOf(dto.itemType),
                userId = UserId(dto.userId),
                voteState = VoteState.valueOf(dto.voteState),
                isBookmarked = dto.isBookmarked,
                createdAt = dto.createdAt.toInstant().toKotlinInstant(),
                updatedAt = dto.updatedAt.toInstant().toKotlinInstant(),
            )
        } catch (e: Exception) {
            // Log error but return null for malformed data
            null
        }

    /**
     * Convert list of DTOs to domain models, filtering out malformed data
     */
    fun fromDtos(dtos: List<UserInteractionDto>): List<UserInteraction> = dtos.mapNotNull { fromDto(it) }

    /**
     * Convert list of domain models to DTOs
     */
    fun toDtos(userInteractions: List<UserInteraction>): List<UserInteractionDto> = userInteractions.map { toDto(it) }

    /**
     * Extension function to convert Instant to Firebase Timestamp
     */
    private fun Instant.toTimestamp(): Timestamp = Timestamp(epochSeconds, nanosecondsOfSecond.toInt())

    /**
     * Extension function to convert Firebase Timestamp to Instant
     */
    private fun Timestamp.toInstant(): Instant = Instant.fromEpochSeconds(seconds, nanoseconds.toLong())
}
