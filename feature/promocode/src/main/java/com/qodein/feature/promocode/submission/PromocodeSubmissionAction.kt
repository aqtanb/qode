package com.qodein.feature.promocode.submission

import com.qodein.core.ui.text.UiText
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.ServiceId
import java.time.LocalDate

sealed interface PromocodeSubmissionAction {
    data object NextProgressiveStep : PromocodeSubmissionAction
    data object PreviousProgressiveStep : PromocodeSubmissionAction
    data class NavigateToStep(val step: PromocodeWizardStep) : PromocodeSubmissionAction

    data object ShowServiceSelector : PromocodeSubmissionAction
    data object ToggleManualEntry : PromocodeSubmissionAction
    data object ConfirmServiceLogo : PromocodeSubmissionAction
    data object DismissServiceConfirmation : PromocodeSubmissionAction

    data class UpdateServiceName(val serviceName: String) : PromocodeSubmissionAction
    data class UpdateServiceUrl(val serviceUrl: String) : PromocodeSubmissionAction
    data class UpdatePromocodeType(val type: PromocodeType) : PromocodeSubmissionAction
    data class UpdatePromocode(val promocode: String) : PromocodeSubmissionAction
    data class UpdateDiscountPercentage(val percentage: String) : PromocodeSubmissionAction
    data class UpdateDiscountAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateFreeItemDescription(val description: String) : PromocodeSubmissionAction
    data class UpdateMinimumOrderAmount(val amount: String) : PromocodeSubmissionAction
    data class UpdateDescription(val description: String) : PromocodeSubmissionAction
    data class UpdateStartDate(val date: LocalDate) : PromocodeSubmissionAction
    data class UpdateEndDate(val date: LocalDate) : PromocodeSubmissionAction

    data object PickImages : PromocodeSubmissionAction
    data class UpdateImageUris(val uris: List<String>) : PromocodeSubmissionAction
    data class RemoveImage(val index: Int) : PromocodeSubmissionAction
    data object SubmitPromoCode : PromocodeSubmissionAction
}

sealed interface PromocodeSubmissionEvent {
    data object PromocodeSubmitted : PromocodeSubmissionEvent
    data object NavigateBack : PromocodeSubmissionEvent
    data class ShowError(val message: UiText) : PromocodeSubmissionEvent
    data class ShowServiceSelection(val currentSelectedService: ServiceId?) : PromocodeSubmissionEvent
    data object PickImagesRequested : PromocodeSubmissionEvent
    data object ImageLimitReached : PromocodeSubmissionEvent
    data class ImagesPartiallyAdded(val count: Int) : PromocodeSubmissionEvent
}
