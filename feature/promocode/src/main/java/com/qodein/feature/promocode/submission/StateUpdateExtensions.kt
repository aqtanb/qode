package com.qodein.feature.promocode.submission

import com.qodein.core.ui.state.UiAuthState

/**
 * Extension functions for ergonomic state updates with the new composed architecture.
 *
 * These extensions make it easy to update individual sub-states without
 * boilerplate while maintaining immutability.
 */

fun PromocodeSubmissionUiState.Success.updateWizardData(
    update: (SubmissionWizardData) -> SubmissionWizardData
): PromocodeSubmissionUiState.Success {
    val newData = update(wizardFlow.wizardData)
    return copy(wizardFlow = wizardFlow.updateData(newData))
}

fun PromocodeSubmissionUiState.Success.moveToNextStep(): PromocodeSubmissionUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToNext(),
    )

fun PromocodeSubmissionUiState.Success.moveToPreviousStep(): PromocodeSubmissionUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToPrevious(),
    )

// Authentication updates
fun PromocodeSubmissionUiState.Success.updateAuthentication(newAuthState: UiAuthState): PromocodeSubmissionUiState.Success =
    copy(authentication = newAuthState)

fun PromocodeSubmissionUiState.Success.clearValidationErrors(): PromocodeSubmissionUiState.Success =
    copy(
        validation = ValidationState.valid(),
    )

fun PromocodeSubmissionUiState.Success.startSubmission(): PromocodeSubmissionUiState.Success =
    copy(
        submission = PromocodeSubmissionState.Submitting,
    )

fun PromocodeSubmissionUiState.Success.submitSuccess(promoCodeId: String): PromocodeSubmissionUiState.Success =
    copy(
        submission = PromocodeSubmissionState.Success(promoCodeId),
    )

fun PromocodeSubmissionUiState.Success.submitError(throwable: Throwable): PromocodeSubmissionUiState.Success =
    copy(
        submission = PromocodeSubmissionState.Error(throwable),
    )

fun PromocodeSubmissionUiState.Success.showServiceSelector(): PromocodeSubmissionUiState.Success = copy(showServiceSelector = true)

fun PromocodeSubmissionUiState.Success.hideServiceSelector(): PromocodeSubmissionUiState.Success = copy(showServiceSelector = false)
