package com.qodein.feature.promocode.submission

/**
 * Extension functions for ergonomic state updates with the new composed architecture.
 *
 * These extensions make it easy to update individual sub-states without
 * boilerplate while maintaining immutability.
 */

// Wizard flow updates
fun PromocodeSubmissionUiState.Success.updateWizardFlow(update: (WizardFlowState) -> WizardFlowState): PromocodeSubmissionUiState.Success =
    copy(wizardFlow = update(wizardFlow))

fun PromocodeSubmissionUiState.Success.updateWizardData(
    update: (SubmissionWizardData) -> SubmissionWizardData
): PromocodeSubmissionUiState.Success {
    val newData = update(wizardFlow.wizardData)
    return copy(wizardFlow = wizardFlow.updateData(newData))
}

fun PromocodeSubmissionUiState.Success.updateStep(newStep: PromocodeSubmissionStep): PromocodeSubmissionUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToStep(newStep),
    )

fun PromocodeSubmissionUiState.Success.moveToNextStep(): PromocodeSubmissionUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToNext(),
    )

fun PromocodeSubmissionUiState.Success.moveToPreviousStep(): PromocodeSubmissionUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToPrevious(),
    )

// Authentication updates
fun PromocodeSubmissionUiState.Success.updateAuthentication(
    newAuthState: PromocodeSubmissionAuthenticationState
): PromocodeSubmissionUiState.Success = copy(authentication = newAuthState)

// Validation updates
fun PromocodeSubmissionUiState.Success.updateValidation(update: (ValidationState) -> ValidationState): PromocodeSubmissionUiState.Success =
    copy(validation = update(validation))

fun PromocodeSubmissionUiState.Success.setValidationErrors(errors: Map<SubmissionField, Int>): PromocodeSubmissionUiState.Success =
    copy(
        validation = ValidationState.invalid(errors),
    )

fun PromocodeSubmissionUiState.Success.clearValidationErrors(): PromocodeSubmissionUiState.Success =
    copy(
        validation = ValidationState.valid(),
    )

// Submission updates
fun PromocodeSubmissionUiState.Success.updateSubmission(
    newPromocodeSubmissionState: PromocodeSubmissionState
): PromocodeSubmissionUiState.Success = copy(submission = newPromocodeSubmissionState)

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

fun PromocodeSubmissionUiState.Success.resetSubmission(): PromocodeSubmissionUiState.Success =
    copy(
        submission = PromocodeSubmissionState.Idle,
    )

// Service selection updates (temporary - until extraction)
fun PromocodeSubmissionUiState.Success.showServiceSelector(): PromocodeSubmissionUiState.Success = copy(showServiceSelector = true)

fun PromocodeSubmissionUiState.Success.hideServiceSelector(): PromocodeSubmissionUiState.Success = copy(showServiceSelector = false)
