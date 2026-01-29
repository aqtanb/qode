package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeSubmissionScheduler
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.UserId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class EnqueuePromocodeSubmissionUseCase(
    private val scheduler: PromocodeSubmissionScheduler,
    private val getUserByIdUseCase: GetUserByIdUseCase
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        code: String,
        service: ServiceRef,
        userId: UserId,
        discount: Discount,
        minimumOrderAmount: Double,
        startDate: Instant,
        endDate: Instant,
        description: String?,
        imageUris: List<String>,
        isFirstUserOnly: Boolean,
        isOneTimeUseOnly: Boolean,
        isVerified: Boolean
    ): Result<Unit, OperationError> =
        when (val userResult = getUserByIdUseCase(userId.value)) {
            is Result.Error -> Result.Error(userResult.error)
            is Result.Success -> {
                scheduler.schedulePromocodeSubmission(
                    code = code,
                    service = service,
                    userId = userId,
                    discount = discount,
                    minimumOrderAmount = minimumOrderAmount,
                    startDate = startDate,
                    endDate = endDate,
                    description = description,
                    imageUris = imageUris,
                    isFirstUserOnly = isFirstUserOnly,
                    isOneTimeUseOnly = isOneTimeUseOnly,
                    isVerified = isVerified,
                )
                Result.Success(Unit)
            }
        }
}
