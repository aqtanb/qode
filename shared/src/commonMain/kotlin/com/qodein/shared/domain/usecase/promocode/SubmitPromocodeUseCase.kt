package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.StoragePath
import com.qodein.shared.model.User
import com.qodein.shared.platform.PlatformUri
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
    val imageUris: List<String>,
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
class SubmitPromocodeUseCase(
    private val repository: PromocodeRepository,
    private val resolveService: GetOrCreateServiceUseCase,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        request: SubmitPromocodeRequest,
        imageUris: List<PlatformUri> = emptyList(),
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Unit, OperationError> {
        val service = when (val result = resolveService(request.service)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        val imageUrls = mutableListOf<String>()
        imageUris.forEachIndexed { index, uri ->
            onProgress(index + 1, imageUris.size)

            when (val result = storageRepository.uploadImage(uri, StoragePath.PROMOCODE_IMAGES)) {
                is Result.Success -> imageUrls.add(result.data)
                is Result.Error -> return Result.Error(result.error)
            }
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
                imageUrls = imageUrls,
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
