package com.qodein.feature.promocode.submission

import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.text.UiText
import com.qodein.shared.model.ServiceId

sealed interface PromocodeSubmissionEvent {
    data object PromoCodeSubmitted : PromocodeSubmissionEvent
    data object NavigateBack : PromocodeSubmissionEvent
    data class NavigateToAuth(val action: AuthPromptAction) : PromocodeSubmissionEvent
    data class ShowError(val message: UiText) : PromocodeSubmissionEvent
    data class ShowServiceSelection(val currentSelectedService: ServiceId?) : PromocodeSubmissionEvent
}
