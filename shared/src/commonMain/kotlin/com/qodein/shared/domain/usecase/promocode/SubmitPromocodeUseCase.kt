package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
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
 * Use case for submitting promo codes.
 * Handles:
 * - Getting or creating service
 * - Duplicate checking
 * - Domain object creation and validation
 * - Submission to repository
 */
class SubmitPromocodeUseCase(private val repo: PromocodeRepository, private val resolveService: GetOrCreateServiceUseCase) {
    suspend operator fun invoke(req: SubmitPromocodeRequest): Result<Unit, OperationError> {
        val service = when (val result = resolveService(req.service)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        // Create and validate promocode
        val promoCode = when (
            val result = PromoCode.create(
                code = req.code,
                service = service,
                author = req.currentUser,
                discount = req.discount,
                minimumOrderAmount = req.minimumOrderAmount,
                startDate = req.startDate,
                endDate = req.endDate,
                isFirstUserOnly = req.isFirstUserOnly,
                isOneTimeUseOnly = req.isOneTimeUseOnly,
                isVerified = req.isVerified,
                description = req.description,
            )
        ) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        // Check for duplicate promocode - fail if already exists
        when (val result = repo.getPromocodeById(promoCode.id)) {
            is Result.Success -> return Result.Error(PromocodeError.SubmissionFailure.DuplicateCode)
            is Result.Error -> {
                // Only proceed if specifically not found - other errors should fail the operation
                if (result.error !is FirestoreError.NotFound) {
                    return Result.Error(result.error)
                }
            }
        }

        // Submit to repository
        return repo.createPromocode(promoCode)
    }
}
