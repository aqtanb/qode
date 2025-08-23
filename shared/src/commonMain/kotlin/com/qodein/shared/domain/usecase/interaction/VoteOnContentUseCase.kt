package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.UserInteractionRepository
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserVote
import com.qodein.shared.model.VoteType
import kotlinx.coroutines.flow.Flow

class VoteOnContentUseCase constructor(private val userInteractionRepository: UserInteractionRepository) {
    operator fun invoke(
        userId: UserId,
        itemId: String,
        itemType: VoteType,
        isUpvote: Boolean?
    ): Flow<Result<UserVote?>> {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }

        return if (isUpvote != null) {
            // Create or update vote
            userInteractionRepository.createOrUpdateVote(
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                isUpvote = isUpvote,
            ).asResult()
        } else {
            // Remove vote
            userInteractionRepository.removeVote(userId, itemId)
                .asResult()
                .let { flow ->
                    kotlinx.coroutines.flow.flow {
                        flow.collect { result ->
                            when (result) {
                                is Result.Loading -> emit(Result.Loading)
                                is Result.Success -> emit(Result.Success(null))
                                is Result.Error -> emit(Result.Error(result.exception))
                            }
                        }
                    }
                }
        }
    }
}
