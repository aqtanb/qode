package com.qodein.feature.promocode.submission

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
