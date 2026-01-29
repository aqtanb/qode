package com.qodein.core.data.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.qodein.core.notifications.Notifier
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeRequest
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.UserId
import com.qodein.shared.platform.PlatformUri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant
import com.qodein.shared.common.Result as DomainResult

private const val KEY_CODE = "code"
private const val KEY_SERVICE_TYPE = "serviceType"
private const val KEY_SERVICE_ID = "serviceId"
private const val KEY_SERVICE_NAME = "serviceName"
private const val KEY_SERVICE_URL = "serviceUrl"
private const val KEY_USER_ID = "userId"
private const val KEY_DISCOUNT_TYPE = "discountType"
private const val KEY_DISCOUNT_VALUE = "discountValue"
private const val KEY_DISCOUNT_DESCRIPTION = "discountDescription"
private const val KEY_MINIMUM_ORDER_AMOUNT = "minimumOrderAmount"
private const val KEY_START_DATE = "startDate"
private const val KEY_END_DATE = "endDate"
private const val KEY_DESCRIPTION = "description"
private const val KEY_IMAGE_URIS = "imageUris"
private const val KEY_IS_FIRST_USER_ONLY = "isFirstUserOnly"
private const val KEY_IS_ONE_TIME_USE_ONLY = "isOneTimeUseOnly"
private const val KEY_IS_VERIFIED = "isVerified"

private const val SERVICE_TYPE_BY_ID = "BY_ID"
private const val SERVICE_TYPE_BY_NAME = "BY_NAME"
private const val DISCOUNT_TYPE_PERCENTAGE = "PERCENTAGE"
private const val DISCOUNT_TYPE_FIXED_AMOUNT = "FIXED_AMOUNT"
private const val DISCOUNT_TYPE_FREE_ITEM = "FREE_ITEM"

class UploadPromocodeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams),
    KoinComponent {

    private val submitPromocodeUseCase: SubmitPromocodeUseCase by inject()
    private val getUserByIdUseCase: GetUserByIdUseCase by inject()
    private val notifier: Notifier by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val uploadId = id.toString()

        val code = inputData.getString(KEY_CODE) ?: return ListenableWorker.Result.failure()
        val serviceType = inputData.getString(KEY_SERVICE_TYPE) ?: return ListenableWorker.Result.failure()
        val userId = inputData.getString(KEY_USER_ID) ?: return ListenableWorker.Result.failure()
        val discountType = inputData.getString(KEY_DISCOUNT_TYPE) ?: return ListenableWorker.Result.failure()
        val discountValue = inputData.getDouble(KEY_DISCOUNT_VALUE, 0.0)
        val discountDescription = inputData.getString(KEY_DISCOUNT_DESCRIPTION)
        val minimumOrderAmount = inputData.getDouble(KEY_MINIMUM_ORDER_AMOUNT, 0.0)
        val startDateEpochMillis = inputData.getLong(KEY_START_DATE, 0L)
        val endDateEpochMillis = inputData.getLong(KEY_END_DATE, 0L)
        val description = inputData.getString(KEY_DESCRIPTION)
        val imageUriStrings = inputData.getStringArray(KEY_IMAGE_URIS)?.toList() ?: emptyList()
        val isFirstUserOnly = inputData.getBoolean(KEY_IS_FIRST_USER_ONLY, false)
        val isOneTimeUseOnly = inputData.getBoolean(KEY_IS_ONE_TIME_USE_ONLY, false)
        val isVerified = inputData.getBoolean(KEY_IS_VERIFIED, false)

        val service = when (serviceType) {
            SERVICE_TYPE_BY_ID -> {
                val serviceId = inputData.getString(KEY_SERVICE_ID) ?: return ListenableWorker.Result.failure()
                ServiceRef.ById(ServiceId(serviceId))
            }
            SERVICE_TYPE_BY_NAME -> {
                val serviceName = inputData.getString(KEY_SERVICE_NAME) ?: return ListenableWorker.Result.failure()
                val serviceUrl = inputData.getString(KEY_SERVICE_URL) ?: return ListenableWorker.Result.failure()
                ServiceRef.ByName(serviceName, serviceUrl)
            }
            else -> return ListenableWorker.Result.failure()
        }

        val discount = when (discountType) {
            DISCOUNT_TYPE_PERCENTAGE -> Discount.Percentage(discountValue)
            DISCOUNT_TYPE_FIXED_AMOUNT -> Discount.FixedAmount(discountValue)
            DISCOUNT_TYPE_FREE_ITEM -> Discount.FreeItem(discountDescription ?: "")
            else -> return ListenableWorker.Result.failure()
        }

        val userResult = getUserByIdUseCase(userId)
        val user = when (userResult) {
            is DomainResult.Error -> {
                notifier.showUploadError(uploadId)
                return ListenableWorker.Result.failure()
            }
            is DomainResult.Success -> userResult.data
        }

        val imageUris = imageUriStrings.map { PlatformUri(it.toUri()) }

        val request = SubmitPromocodeRequest(
            code = code,
            service = service,
            currentUser = user,
            discount = discount,
            minimumOrderAmount = minimumOrderAmount,
            startDate = Instant.fromEpochMilliseconds(startDateEpochMillis),
            endDate = Instant.fromEpochMilliseconds(endDateEpochMillis),
            description = description,
            imageUris = emptyList(),
            isFirstUserOnly = isFirstUserOnly,
            isOneTimeUseOnly = isOneTimeUseOnly,
            isVerified = isVerified,
        )

        when (
            val result = submitPromocodeUseCase(
                request = request,
                imageUris = imageUris,
                onProgress = { current, total ->
                    notifier.showUploadProgress(uploadId, current, total)
                },
            )
        ) {
            is DomainResult.Error -> {
                notifier.showUploadError(uploadId)
                return ListenableWorker.Result.failure()
            }
            is DomainResult.Success -> {
                notifier.showUploadSuccess(uploadId)
                return ListenableWorker.Result.success()
            }
        }
    }

    companion object {
        fun createWorkRequest(
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
        ): OneTimeWorkRequest {
            val (serviceType, serviceId, serviceName, serviceUrl) = when (service) {
                is ServiceRef.ById -> listOf(SERVICE_TYPE_BY_ID, service.id.value, null, null)
                is ServiceRef.ByName -> listOf(SERVICE_TYPE_BY_NAME, null, service.name, service.siteUrl)
            }

            val (discountType, discountValue, discountDescription) = when (discount) {
                is Discount.Percentage -> Triple(DISCOUNT_TYPE_PERCENTAGE, discount.value, null)
                is Discount.FixedAmount -> Triple(DISCOUNT_TYPE_FIXED_AMOUNT, discount.value, null)
                is Discount.FreeItem -> Triple(DISCOUNT_TYPE_FREE_ITEM, 0.0, discount.description)
            }

            val inputData = workDataOf(
                KEY_CODE to code,
                KEY_SERVICE_TYPE to serviceType,
                KEY_SERVICE_ID to serviceId,
                KEY_SERVICE_NAME to serviceName,
                KEY_SERVICE_URL to serviceUrl,
                KEY_USER_ID to userId.value,
                KEY_DISCOUNT_TYPE to discountType,
                KEY_DISCOUNT_VALUE to discountValue,
                KEY_DISCOUNT_DESCRIPTION to discountDescription,
                KEY_MINIMUM_ORDER_AMOUNT to minimumOrderAmount,
                KEY_START_DATE to startDate.toEpochMilliseconds(),
                KEY_END_DATE to endDate.toEpochMilliseconds(),
                KEY_DESCRIPTION to description,
                KEY_IMAGE_URIS to imageUris.toTypedArray(),
                KEY_IS_FIRST_USER_ONLY to isFirstUserOnly,
                KEY_IS_ONE_TIME_USE_ONLY to isOneTimeUseOnly,
                KEY_IS_VERIFIED to isVerified,
            )

            return OneTimeWorkRequestBuilder<UploadPromocodeWorker>()
                .setInputData(inputData)
                .build()
        }
    }
}
