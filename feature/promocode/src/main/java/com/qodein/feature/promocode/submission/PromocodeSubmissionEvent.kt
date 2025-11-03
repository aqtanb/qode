package com.qodein.feature.promocode.submission

import com.qodein.shared.common.error.OperationError

sealed interface PromocodeSubmissionEvent {
    data object PromoCodeSubmitted : PromocodeSubmissionEvent
    data object NavigateBack : PromocodeSubmissionEvent
    data class ShowError(val error: OperationError) : PromocodeSubmissionEvent
}
