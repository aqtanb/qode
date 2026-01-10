package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.User
import kotlin.time.Instant

data class SubmitPromocodeRequest(
    val code: String,
    val service: ServiceRef,
    val currentUser: User,
    val discount: Discount,
    val minimumOrderAmount: Double,
    val startDate: Instant,
    val endDate: Instant,
    val description: String?,
    val isFirstUserOnly: Boolean,
    val isOneTimeUseOnly: Boolean,
    val isVerified: Boolean
)

/**
 * Use case for submitting promocodes.
 * Handles:
 * - Getting or creating service
 * - Duplicate checking
 * - Domain object creation and validation
 * - Submission to repository
 */
class SubmitPromocodeUseCase(private val repository: PromocodeRepository, private val resolveService: GetOrCreateServiceUseCase) {
    suspend operator fun invoke(request: SubmitPromocodeRequest): Result<Unit, OperationError> {
        val service = when (val result = resolveService(request.service)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        val promocode = when (
            val result = Promocode.create(
                code = request.code,
                service = service,
                author = request.currentUser,
                discount = request.discount,
                minimumOrderAmount = request.minimumOrderAmount,
                startDate = request.startDate,
                endDate = request.endDate,
                isVerified = request.isVerified,
                description = request.description,
            )
        ) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        when (val result = repository.getPromocodeById(promocode.id)) {
            is Result.Success -> return Result.Error(PromocodeError.SubmissionFailure.DuplicateCode)
            is Result.Error -> {
                if (result.error !is FirestoreError.NotFound) {
                    return Result.Error(result.error)
                }
            }
        }

        return repository.createPromocode(promocode)
    }
}
