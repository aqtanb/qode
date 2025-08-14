package com.qodein.core.domain.usecase.promocode

import com.qodein.core.domain.repository.PromoCodeRepository
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddCommentUseCase @Inject constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): Flow<Result<PromoCode>> {
        val trimmedComment = comment.trim()
        require(trimmedComment.isNotBlank()) { "Comment cannot be blank" }
        require(trimmedComment.length <= 500) { "Comment too long (max 500 characters)" }

        return promoCodeRepository.addComment(promoCodeId, userId, trimmedComment)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }
}
