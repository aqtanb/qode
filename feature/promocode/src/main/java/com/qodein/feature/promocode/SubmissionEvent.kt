package com.qodein.feature.promocode

sealed interface SubmissionEvent {

    data object PromoCodeSubmitted : SubmissionEvent

    data object NavigateBack : SubmissionEvent
}
