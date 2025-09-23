package com.qodein.feature.promocode.submission

/**
 * Extension functions for ergonomic state updates with the new composed architecture.
 *
 * These extensions make it easy to update individual sub-states without
 * boilerplate while maintaining immutability.
 */

// Wizard flow updates
fun SubmissionWizardUiState.Success.updateWizardFlow(update: (WizardFlowState) -> WizardFlowState): SubmissionWizardUiState.Success =
    copy(wizardFlow = update(wizardFlow))

fun SubmissionWizardUiState.Success.updateWizardData(
    update: (SubmissionWizardData) -> SubmissionWizardData
): SubmissionWizardUiState.Success {
    val newData = update(wizardFlow.wizardData)
    return copy(wizardFlow = wizardFlow.updateData(newData))
}

fun SubmissionWizardUiState.Success.updateStep(newStep: SubmissionStep): SubmissionWizardUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToStep(newStep),
    )

fun SubmissionWizardUiState.Success.moveToNextStep(): SubmissionWizardUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToNext(),
    )

fun SubmissionWizardUiState.Success.moveToPreviousStep(): SubmissionWizardUiState.Success =
    copy(
        wizardFlow = wizardFlow.moveToPrevious(),
    )

// Authentication updates
fun SubmissionWizardUiState.Success.updateAuthentication(newAuthState: AuthenticationState): SubmissionWizardUiState.Success =
    copy(authentication = newAuthState)

// Validation updates
fun SubmissionWizardUiState.Success.updateValidation(update: (ValidationState) -> ValidationState): SubmissionWizardUiState.Success =
    copy(validation = update(validation))

fun SubmissionWizardUiState.Success.setValidationErrors(errors: Map<SubmissionField, Int>): SubmissionWizardUiState.Success =
    copy(
        validation = ValidationState.invalid(errors),
    )

fun SubmissionWizardUiState.Success.clearValidationErrors(): SubmissionWizardUiState.Success =
    copy(
        validation = ValidationState.valid(),
    )

// Submission updates
fun SubmissionWizardUiState.Success.updateSubmission(newSubmissionState: SubmissionState): SubmissionWizardUiState.Success =
    copy(submission = newSubmissionState)

fun SubmissionWizardUiState.Success.startSubmission(): SubmissionWizardUiState.Success =
    copy(
        submission = SubmissionState.Submitting,
    )

fun SubmissionWizardUiState.Success.submitSuccess(promoCodeId: String): SubmissionWizardUiState.Success =
    copy(
        submission = SubmissionState.Success(promoCodeId),
    )

fun SubmissionWizardUiState.Success.submitError(throwable: Throwable): SubmissionWizardUiState.Success =
    copy(
        submission = SubmissionState.Error(throwable),
    )

fun SubmissionWizardUiState.Success.resetSubmission(): SubmissionWizardUiState.Success =
    copy(
        submission = SubmissionState.Idle,
    )

// Service selection updates (temporary - until extraction)
fun SubmissionWizardUiState.Success.showServiceSelector(): SubmissionWizardUiState.Success = copy(showServiceSelector = true)

fun SubmissionWizardUiState.Success.hideServiceSelector(): SubmissionWizardUiState.Success = copy(showServiceSelector = false)
