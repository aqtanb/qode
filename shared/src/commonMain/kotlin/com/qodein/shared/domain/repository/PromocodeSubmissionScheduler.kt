package com.qodein.shared.domain.repository

import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.UserId
import kotlin.time.Instant

interface PromocodeSubmissionScheduler {
    fun schedulePromocodeSubmission(
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
    )
}
