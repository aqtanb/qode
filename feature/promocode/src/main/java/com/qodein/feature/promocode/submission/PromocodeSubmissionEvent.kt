package com.qodein.feature.promocode.submission

sealed interface PromocodeSubmissionEvent {
    data object PromoCodeSubmitted : PromocodeSubmissionEvent
    data object NavigateBack : PromocodeSubmissionEvent
}
