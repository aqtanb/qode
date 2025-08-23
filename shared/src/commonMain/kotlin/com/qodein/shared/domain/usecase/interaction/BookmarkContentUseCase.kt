package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.UserInteractionRepository
import com.qodein.shared.model.BookmarkType
import com.qodein.shared.model.UserBookmark
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class BookmarkContentUseCase constructor(private val userInteractionRepository: UserInteractionRepository) {
    operator fun invoke(
        userId: UserId,
        itemId: String,
        itemType: BookmarkType,
        itemTitle: String,
        itemCategory: String? = null,
        isBookmarked: Boolean
    ): Flow<Result<UserBookmark?>> {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }
        require(itemTitle.isNotBlank()) { "Item title cannot be blank" }

        return if (isBookmarked) {
            userInteractionRepository.createBookmark(
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                itemTitle = itemTitle.trim(),
                itemCategory = itemCategory?.trim(),
            ).asResult()
        } else {
            userInteractionRepository.removeBookmark(userId, itemId)
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
