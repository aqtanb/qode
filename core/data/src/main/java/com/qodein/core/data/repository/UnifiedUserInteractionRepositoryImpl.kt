package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreUnifiedUserInteractionDataSource
import com.qodein.core.data.mapper.UserInteractionMapper
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.InteractionStats
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for unified user interactions.
 * Handles conversion between DTOs and domain models, error handling, and business logic.
 */
@Singleton
class UnifiedUserInteractionRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUnifiedUserInteractionDataSource,
    private val mapper: UserInteractionMapper
) : UnifiedUserInteractionRepository {

    // ================================================================================================
    // SINGLE INTERACTION OPERATIONS
    // ================================================================================================

    override suspend fun getUserInteraction(
        itemId: String,
        userId: UserId
    ): UserInteraction? {
        val dto = dataSource.getUserInteraction(itemId, userId.value)
        return dto?.let { mapper.fromDto(it) }
    }

    override suspend fun upsertUserInteraction(interaction: UserInteraction): UserInteraction {
        val dto = mapper.toDto(interaction)
        val savedDto = dataSource.upsertUserInteraction(dto)
        return mapper.fromDto(savedDto) ?: interaction
    }

    override suspend fun deleteUserInteraction(
        itemId: String,
        userId: UserId
    ) {
        dataSource.deleteUserInteraction(itemId, userId.value)
    }

    override fun observeUserInteraction(
        itemId: String,
        userId: UserId
    ): Flow<UserInteraction?> =
        dataSource.observeUserInteraction(itemId, userId.value)
            .map { dto -> dto?.let { mapper.fromDto(it) } }

    // ================================================================================================
    // BATCH OPERATIONS
    // ================================================================================================

    override suspend fun getUserBookmarks(userId: UserId): List<UserInteraction> {
        val dtos = dataSource.getUserBookmarks(userId.value)
        return mapper.fromDtos(dtos)
    }

    override suspend fun getAllUserInteractions(userId: UserId): List<UserInteraction> {
        val dtos = dataSource.getAllUserInteractions(userId.value)
        return mapper.fromDtos(dtos)
    }

    override suspend fun getUserInteractionsForItems(
        itemIds: List<String>,
        userId: UserId
    ): Map<String, UserInteraction> {
        val dtoMap = dataSource.getUserInteractionsForItems(itemIds, userId.value)
        return dtoMap.mapNotNull { (itemId, dto) ->
            mapper.fromDto(dto)?.let { interaction ->
                itemId to interaction
            }
        }.toMap()
    }

    // ================================================================================================
    // CONTENT-CENTRIC OPERATIONS
    // ================================================================================================

    override suspend fun getInteractionsForContent(itemId: String): List<UserInteraction> {
        val dtos = dataSource.getInteractionsForContent(itemId)
        return mapper.fromDtos(dtos)
    }

    override suspend fun getInteractionStats(itemId: String): InteractionStats {
        // TODO: Implement when InteractionStats models are created
        throw NotImplementedError("InteractionStats functionality not yet implemented")
    }

    // ================================================================================================
    // CONVENIENCE METHODS
    // ================================================================================================

    override suspend fun toggleVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId,
        newVoteState: VoteState
    ): UserInteraction {
        // Get existing interaction or create new one
        val existingInteraction = getUserInteraction(itemId, userId)

        val updatedInteraction = if (existingInteraction != null) {
            // Toggle existing interaction
            when (newVoteState) {
                VoteState.UPVOTE -> existingInteraction.toggleUpvote()
                VoteState.DOWNVOTE -> existingInteraction.toggleDownvote()
                VoteState.NONE -> existingInteraction.copy(voteState = VoteState.NONE)
            }
        } else {
            // Create new interaction with vote
            UserInteraction.create(
                itemId = itemId,
                itemType = itemType,
                userId = userId,
                voteState = newVoteState,
                isBookmarked = false,
            )
        }

        return upsertUserInteraction(updatedInteraction)
    }

    override suspend fun toggleBookmark(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): UserInteraction {
        // Get existing interaction or create new one
        val existingInteraction = getUserInteraction(itemId, userId)

        val updatedInteraction = if (existingInteraction != null) {
            // Toggle existing bookmark
            existingInteraction.toggleBookmark()
        } else {
            // Create new interaction with bookmark
            UserInteraction.create(
                itemId = itemId,
                itemType = itemType,
                userId = userId,
                voteState = VoteState.NONE,
                isBookmarked = true,
            )
        }

        return upsertUserInteraction(updatedInteraction)
    }

    // ================================================================================================
    // HELPER METHODS
    // ================================================================================================

    // TODO: Implement when InteractionStats models are created
    // /**
    //  * Map InteractionStatsDto to domain InteractionStats
    //  */
    // private fun mapStatsFromDto(dto: InteractionStatsDto): InteractionStats =
    //     InteractionStats(
    //         itemId = dto.itemId,
    //         upvoteCount = dto.upvoteCount,
    //         downvoteCount = dto.downvoteCount,
    //         bookmarkCount = dto.bookmarkCount,
    //         totalInteractions = dto.totalInteractions,
    //     )
}
