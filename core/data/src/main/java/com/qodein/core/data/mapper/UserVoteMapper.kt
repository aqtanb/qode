package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.UserVoteDto
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import com.qodein.shared.model.VoteType
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object VoteMapper {

    fun toDomain(dto: UserVoteDto): Vote {
        require(dto.id.isNotBlank()) { "Vote ID cannot be blank" }
        require(dto.userId.isNotBlank()) { "Vote user ID cannot be blank" }
        require(dto.itemId.isNotBlank()) { "Vote item ID cannot be blank" }
        require(dto.itemType.isNotBlank()) { "Vote item type cannot be blank" }

        val itemType = when (dto.itemType.uppercase()) {
            "PROMO_CODE" -> VoteType.PROMO_CODE
            "POST" -> VoteType.POST
            "COMMENT" -> VoteType.COMMENT
            "PROMO" -> VoteType.PROMO
            else -> throw IllegalArgumentException("Unknown vote type: ${dto.itemType}")
        }

        val voteState = when (dto.voteState?.uppercase()) {
            "UPVOTE" -> VoteState.UPVOTE
            "DOWNVOTE" -> VoteState.DOWNVOTE
            "NONE", null -> VoteState.NONE
            else -> throw IllegalArgumentException("Unknown vote state: ${dto.voteState}")
        }

        return Vote(
            id = dto.id,
            userId = UserId(dto.userId),
            itemId = dto.itemId,
            itemType = itemType,
            voteState = voteState,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
            updatedAt = dto.updatedAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: Vote): UserVoteDto =
        UserVoteDto(
            id = domain.id,
            userId = domain.userId.value,
            itemId = domain.itemId,
            itemType = domain.itemType.name,
            voteState = domain.voteState.name,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
            updatedAt = Timestamp(domain.updatedAt.toJavaInstant()),
        )

    fun toDomainList(dtos: List<UserVoteDto>): List<Vote> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(votes: List<Vote>): List<UserVoteDto> = votes.map { toDto(it) }
}
