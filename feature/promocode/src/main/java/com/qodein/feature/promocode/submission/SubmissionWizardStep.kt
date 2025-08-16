package com.qodein.feature.promocode.submission

enum class SubmissionWizardStep(val stepNumber: Int) {
    SERVICE_AND_TYPE(1),
    TYPE_DETAILS(2),
    DATE_SETTINGS(3),
    OPTIONAL_DETAILS(4);

    val isFirst: Boolean get() = this == SERVICE_AND_TYPE
    val isLast: Boolean get() = this == OPTIONAL_DETAILS

    fun next(): SubmissionWizardStep? =
        when (this) {
            SERVICE_AND_TYPE -> TYPE_DETAILS
            TYPE_DETAILS -> DATE_SETTINGS
            DATE_SETTINGS -> OPTIONAL_DETAILS
            OPTIONAL_DETAILS -> null
        }

    fun previous(): SubmissionWizardStep? =
        when (this) {
            SERVICE_AND_TYPE -> null
            TYPE_DETAILS -> SERVICE_AND_TYPE
            DATE_SETTINGS -> TYPE_DETAILS
            OPTIONAL_DETAILS -> DATE_SETTINGS
        }

    companion object {
        fun fromStepNumber(stepNumber: Int): SubmissionWizardStep? = values().find { it.stepNumber == stepNumber }
    }
}
