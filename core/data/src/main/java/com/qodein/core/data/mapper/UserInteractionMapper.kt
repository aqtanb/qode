package com.qodein.core.data.mapper

import com.qodein.core.data.dto.UserInteractionDto
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

object UserInteractionMapper {

    fun toDto(userInteraction: UserInteraction): UserInteractionDto =
        UserInteractionDto(
            documentId = userInteraction.id,
            itemId = userInteraction.itemId,
            itemType = userInteraction.itemType.name,
            userId = userInteraction.userId.value,
            voteState = userInteraction.voteState.name,
            isBookmarked = userInteraction.isBookmarked,
        )

    fun fromDto(dto: UserInteractionDto): UserInteraction? =
        try {
            UserInteraction(
                itemId = dto.itemId,
                itemType = ContentType.valueOf(dto.itemType),
                userId = UserId(dto.userId),
                voteState = VoteState.valueOf(dto.voteState),
                isBookmarked = dto.isBookmarked,
            )
        } catch (e: Exception) {
            null
        }
}
