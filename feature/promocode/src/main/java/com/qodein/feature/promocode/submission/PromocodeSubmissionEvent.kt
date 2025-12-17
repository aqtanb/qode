package com.qodein.feature.promocode.submission

import com.qodein.core.ui.text.UiText

sealed interface PromocodeSubmissionEvent {
    data object PromoCodeSubmitted : PromocodeSubmissionEvent
    data object NavigateBack : PromocodeSubmissionEvent
    data class ShowError(val message: UiText) : PromocodeSubmissionEvent
}
