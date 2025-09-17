package com.qodein.feature.promocode.submission

/**
 * Represents the wizard flow state including current step and navigation capabilities.
 *
 * This state encapsulates wizard progression logic with efficient computed properties
 * and safe navigation methods that don't break flow with nulls.
 */
data class WizardFlowState(val wizardData: SubmissionWizardData, val currentStep: ProgressiveStep) {
    val canGoNext get() = currentStep.canProceed(wizardData) && !currentStep.isLast
    val canGoPrevious get() = !currentStep.isFirst
    val canSubmit get() = currentStep.isLast && currentStep.canProceed(wizardData)

    companion object {
        fun initial() =
            WizardFlowState(
                SubmissionWizardData(),
                ProgressiveStep.SERVICE,
            )
    }

    fun updateData(newData: SubmissionWizardData) = WizardFlowState(newData, currentStep)

    fun moveToNext() = currentStep.next()?.let { WizardFlowState(wizardData, it) } ?: this

    fun moveToPrevious() = currentStep.previous()?.let { WizardFlowState(wizardData, it) } ?: this

    fun moveToStep(step: ProgressiveStep) = WizardFlowState(wizardData, step)
}
