package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.UserVoteDto
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserVote
import com.qodein.shared.model.VoteType
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object UserVoteMapper {

    fun toDomain(dto: UserVoteDto): UserVote {
        require(dto.id.isNotBlank()) { "UserVote ID cannot be blank" }
        require(dto.userId.isNotBlank()) { "UserVote user ID cannot be blank" }
        require(dto.itemId.isNotBlank()) { "UserVote item ID cannot be blank" }
        require(dto.itemType.isNotBlank()) { "UserVote item type cannot be blank" }

        val itemType = when (dto.itemType.uppercase()) {
            "PROMO_CODE" -> VoteType.PROMO_CODE
            "POST" -> VoteType.POST
            "COMMENT" -> VoteType.COMMENT
            else -> throw IllegalArgumentException("Unknown vote type: ${dto.itemType}")
        }

        return UserVote(
            id = dto.id,
            userId = UserId(dto.userId),
            itemId = dto.itemId,
            itemType = itemType,
            isUpvote = dto.isUpvote,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            updatedAt = dto.updatedAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: UserVote): UserVoteDto =
        UserVoteDto(
            id = domain.id,
            userId = domain.userId.value,
            itemId = domain.itemId,
            itemType = domain.itemType.name,
            isUpvote = domain.isUpvote,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            updatedAt = Timestamp(domain.updatedAt.toJavaInstant()),
        )

    fun toDomainList(dtos: List<UserVoteDto>): List<UserVote> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(votes: List<UserVote>): List<UserVoteDto> = votes.map { toDto(it) }
}
