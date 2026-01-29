package com.qodein.core.data.promocode

import androidx.work.WorkManager
import com.qodein.core.data.worker.UploadPromocodeWorker
import com.qodein.shared.domain.repository.PromocodeSubmissionScheduler
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.UserId
import kotlin.time.Instant

class WorkManagerPromocodeSubmissionScheduler(private val workManager: WorkManager) : PromocodeSubmissionScheduler {

    override fun schedulePromocodeSubmission(
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
    ) {
        val workRequest = UploadPromocodeWorker.createWorkRequest(
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
        workManager.enqueue(workRequest)
    }
}
