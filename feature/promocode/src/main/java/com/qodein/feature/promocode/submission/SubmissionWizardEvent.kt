package com.qodein.feature.promocode.submission

sealed interface SubmissionWizardEvent {
    data object PromoCodeSubmitted : SubmissionWizardEvent
    data object NavigateBack : SubmissionWizardEvent
}
