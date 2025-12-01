package com.qodein.feature.promocode.submission

import com.qodein.core.ui.state.UiAuthState
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service
import java.time.LocalDate

enum class PromocodeType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class SubmissionWizardData(
    // Service Details
    val selectedService: Service? = null,
    val serviceName: String = "",
    val isManualServiceEntry: Boolean = false,

    // Promo Code Details
    val code: String = "",
    val promocodeType: PromocodeType? = null,
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val minimumOrderAmount: String = "",

    // Options
    val isFirstUserOnly: Boolean = false,
    val isOneTimeUseOnly: Boolean = false,
    val description: String = "",

    // Date Settings
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null
) {
    val effectiveServiceName: String get() = selectedService?.name ?: serviceName
}

sealed interface PromocodeSubmissionUiState {
    data object Loading : PromocodeSubmissionUiState

    data class Success(
        val wizardFlow: WizardFlowState,
        val validation: ValidationState,
        val submission: PromocodeSubmissionState,
        val showServiceSelector: Boolean = false,
        val authentication: UiAuthState = UiAuthState.Loading
    ) : PromocodeSubmissionUiState {

        // Navigation capabilities
        val canGoNext: Boolean get() = wizardFlow.canGoNext && submission !is PromocodeSubmissionState.Submitting
        val canGoPrevious: Boolean get() = wizardFlow.canGoPrevious && submission !is PromocodeSubmissionState.Submitting
        val canSubmit: Boolean get() = wizardFlow.canSubmit && validation.isValid && submission !is PromocodeSubmissionState.Submitting

        companion object {
            fun initial(): Success =
                Success(
                    wizardFlow = WizardFlowState.initial(),
                    validation = ValidationState.valid(),
                    submission = PromocodeSubmissionState.Idle,
                )
        }
    }

    data class Error(val error: OperationError) : PromocodeSubmissionUiState
}
