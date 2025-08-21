package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class AddCommentUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): Flow<Result<PromoCode>> {
        val trimmedComment = comment.trim()
        require(trimmedComment.isNotBlank()) { "Comment cannot be blank" }
        require(trimmedComment.length <= 500) { "Comment too long (max 500 characters)" }

        return promoCodeRepository.addComment(promoCodeId, userId, trimmedComment)
            .asResult()
    }
}
