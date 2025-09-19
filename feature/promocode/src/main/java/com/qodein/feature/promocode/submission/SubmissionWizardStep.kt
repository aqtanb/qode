package com.qodein.feature.promocode.submission

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.feature.promocode.R

enum class ProgressiveStep(val stepNumber: Int, val hint: String) {
    SERVICE(1, "Select the service for your promo code. Can't find it? Type it manually.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.serviceName.isNotBlank()
    },
    DISCOUNT_TYPE(2, "Percentage takes % off the total. " + "\nFixed amount takes exact $ off.") {

        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCodeType != null
    },
    PROMO_CODE(3, "Enter your promo code (e.g., SAVE20).") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCode.isNotBlank()
    },
    DISCOUNT_VALUE(4, "Set your discount value and minimum order amount.") {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            when (data.promoCodeType) {
                PromoCodeType.PERCENTAGE -> data.discountPercentage.isNotBlank() && data.minimumOrderAmount.isNotBlank()
                PromoCodeType.FIXED_AMOUNT -> data.discountAmount.isNotBlank() && data.minimumOrderAmount.isNotBlank()
                null -> false
            }
    },
    START_DATE(5, "Set when your promo code becomes active. Default is today.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Always valid (defaults to today)
    },
    END_DATE(6, "Choose when your promo code expires. This is required.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.endDate != null && data.endDate.isAfter(data.startDate)
    },
    OPTIONS(7, "Configure additional options for your promo code.") {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Optional step, always valid
    };

    abstract fun canProceed(data: SubmissionWizardData): Boolean

    val isFirst: Boolean get() = this == SERVICE
    val isLast: Boolean get() = this == OPTIONS

    fun next(): ProgressiveStep? =
        when (this) {
            SERVICE -> DISCOUNT_TYPE
            DISCOUNT_TYPE -> PROMO_CODE
            PROMO_CODE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> START_DATE
            START_DATE -> END_DATE
            END_DATE -> OPTIONS
            OPTIONS -> null
        }

    fun previous(): ProgressiveStep? =
        when (this) {
            SERVICE -> null
            DISCOUNT_TYPE -> SERVICE
            PROMO_CODE -> DISCOUNT_TYPE
            DISCOUNT_VALUE -> PROMO_CODE
            START_DATE -> DISCOUNT_VALUE
            END_DATE -> START_DATE
            OPTIONS -> END_DATE
        }
}

// Centralized step icon logic
fun ProgressiveStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            ProgressiveStep.SERVICE -> QodeCommerceIcons.Store
            ProgressiveStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
            ProgressiveStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
            ProgressiveStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
            ProgressiveStep.OPTIONS -> QodeNavigationIcons.Settings
            ProgressiveStep.START_DATE, ProgressiveStep.END_DATE -> QodeUIIcons.Datepicker
        }
    }

// String resource extensions
val ProgressiveStep.titleRes: Int
    get() = when (this) {
        ProgressiveStep.SERVICE -> R.string.step_service_title
        ProgressiveStep.DISCOUNT_TYPE -> R.string.step_discount_type_title
        ProgressiveStep.PROMO_CODE -> R.string.step_promo_code_title
        ProgressiveStep.DISCOUNT_VALUE -> R.string.step_discount_value_title
        ProgressiveStep.OPTIONS -> R.string.step_options_title
        ProgressiveStep.START_DATE -> R.string.step_start_date_title
        ProgressiveStep.END_DATE -> R.string.step_end_date_title
    }

val ProgressiveStep.shortNameRes: Int
    get() = when (this) {
        ProgressiveStep.SERVICE -> R.string.step_service_short
        ProgressiveStep.DISCOUNT_TYPE -> R.string.step_discount_type_short
        ProgressiveStep.PROMO_CODE -> R.string.step_promo_code_short
        ProgressiveStep.DISCOUNT_VALUE -> R.string.step_discount_value_short
        ProgressiveStep.OPTIONS -> R.string.step_options_short
        ProgressiveStep.START_DATE -> R.string.step_start_date_short
        ProgressiveStep.END_DATE -> R.string.step_end_date_short
    }
