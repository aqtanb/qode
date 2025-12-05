package com.qodein.feature.promocode.submission

import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
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
    val serviceUrl: String = "",
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
    val endDate: LocalDate? = LocalDate.now().plusDays(30)
) {
    val effectiveServiceName: String get() = selectedService?.name ?: serviceName
    val effectiveServiceUrl: String get() = selectedService?.siteUrl ?: serviceUrl
}

sealed interface PromocodeSubmissionUiState {
    data object Loading : PromocodeSubmissionUiState

    data class Success(
        val wizardFlow: WizardFlowState,
        val validation: ValidationState,
        val submission: PromocodeSubmissionState,
        val showServiceSelector: Boolean = false
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

/**
 * Clean submission operation state without UI concerns.
 *
 * Represents the lifecycle of promo code submission operation.
 * UI behavior should be derived in presentation layer.
 */
sealed interface PromocodeSubmissionState {
    data object Idle : PromocodeSubmissionState
    data object Submitting : PromocodeSubmissionState
    data class Success(val promoCodeId: String) : PromocodeSubmissionState
    data class Error(val error: OperationError) : PromocodeSubmissionState
}

/**
 * Represents the wizard flow state including current step and navigation capabilities.
 *
 * This state encapsulates wizard progression logic with efficient computed properties
 * and safe navigation methods that don't break flow with nulls.
 */
data class WizardFlowState(val wizardData: SubmissionWizardData, val currentStep: PromocodeSubmissionStep) {
    val canGoNext get() = (currentStep.canProceed(wizardData) || !currentStep.isRequired) && !currentStep.isLast
    val canGoPrevious get() = !currentStep.isFirst
    val canSubmit get() = allRequiredStepsComplete() &&
        currentStep.canProceed(wizardData) &&
        currentStep.stepNumber >= getLastRequiredStepNumber()

    private fun getLastRequiredStepNumber(): Int =
        PromocodeSubmissionStep.entries
            .filter { it.isRequired }
            .maxOfOrNull { it.stepNumber } ?: 1

    private fun allRequiredStepsComplete(): Boolean =
        PromocodeSubmissionStep.entries
            .filter { it.isRequired }
            .all { step ->
                step.stepNumber <= currentStep.stepNumber && step.canProceed(wizardData)
            }

    companion object {
        fun initial() =
            WizardFlowState(
                SubmissionWizardData(),
                PromocodeSubmissionStep.SERVICE,
            )
    }

    fun updateData(newData: SubmissionWizardData) = WizardFlowState(newData, currentStep)

    fun moveToNext() = currentStep.next()?.let { WizardFlowState(wizardData, it) } ?: this

    fun moveToPrevious() = currentStep.previous()?.let { WizardFlowState(wizardData, it) } ?: this

    fun moveToStep(step: PromocodeSubmissionStep) = WizardFlowState(wizardData, step)
}
