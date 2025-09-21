package com.qodein.feature.promocode.submission

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.feature.promocode.R

enum class SubmissionStep(val stepNumber: Int, val isRequired: Boolean = true) {
    SERVICE(1) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.effectiveServiceName.isNotBlank()
    },
    DISCOUNT_TYPE(2) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCodeType != null
    },
    PROMO_CODE(3) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCode.isNotBlank()
    },
    DISCOUNT_VALUE(4) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            when (data.promoCodeType) {
                PromoCodeType.PERCENTAGE -> data.discountPercentage.isNotBlank()
                PromoCodeType.FIXED_AMOUNT -> data.discountAmount.isNotBlank()
                null -> false
            }
    },
    MINIMUM_ORDER(5) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.minimumOrderAmount.isNotBlank()
    },
    START_DATE(6) {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Always valid (defaults to today)
    },
    END_DATE(7, true) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.endDate != null && data.endDate.isAfter(data.startDate)
    },
    ELIGIBILITY(8, false) {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Optional step, always valid
    },
    DESCRIPTION(9, false) {
        override fun canProceed(data: SubmissionWizardData): Boolean = true // Optional step, always valid
    };

    abstract fun canProceed(data: SubmissionWizardData): Boolean

    val isFirst: Boolean get() = this == SERVICE
    val isLast: Boolean get() = this == DESCRIPTION
    val isLastRequired: Boolean get() = this == END_DATE

    fun next(): SubmissionStep? =
        when (this) {
            SERVICE -> DISCOUNT_TYPE
            DISCOUNT_TYPE -> PROMO_CODE
            PROMO_CODE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> MINIMUM_ORDER
            MINIMUM_ORDER -> START_DATE
            START_DATE -> END_DATE
            END_DATE -> ELIGIBILITY
            ELIGIBILITY -> DESCRIPTION
            DESCRIPTION -> null
        }

    fun previous(): SubmissionStep? =
        when (this) {
            SERVICE -> null
            DISCOUNT_TYPE -> SERVICE
            PROMO_CODE -> DISCOUNT_TYPE
            DISCOUNT_VALUE -> PROMO_CODE
            MINIMUM_ORDER -> DISCOUNT_VALUE
            START_DATE -> MINIMUM_ORDER
            END_DATE -> START_DATE
            ELIGIBILITY -> END_DATE
            DESCRIPTION -> ELIGIBILITY
        }
}

// Centralized step icon logic
fun SubmissionStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            SubmissionStep.SERVICE -> QodeCommerceIcons.Store
            SubmissionStep.DISCOUNT_TYPE -> QodeCommerceIcons.Sale
            SubmissionStep.PROMO_CODE -> QodeCommerceIcons.PromoCode
            SubmissionStep.DISCOUNT_VALUE -> QodeCommerceIcons.Dollar
            SubmissionStep.MINIMUM_ORDER -> QodeCommerceIcons.Dollar
            SubmissionStep.ELIGIBILITY -> QodeNavigationIcons.Settings
            SubmissionStep.DESCRIPTION -> QodeUIIcons.Info
            SubmissionStep.START_DATE, SubmissionStep.END_DATE -> QodeUIIcons.Datepicker
        }
    }

// String resource extensions
val SubmissionStep.titleRes: Int
    get() = when (this) {
        SubmissionStep.SERVICE -> R.string.step_service_title
        SubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_title
        SubmissionStep.PROMO_CODE -> R.string.step_promo_code_title
        SubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_title
        SubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_title
        SubmissionStep.ELIGIBILITY -> R.string.step_eligibility_title
        SubmissionStep.DESCRIPTION -> R.string.step_description_title
        SubmissionStep.START_DATE -> R.string.step_start_date_title
        SubmissionStep.END_DATE -> R.string.step_end_date_title
    }

val SubmissionStep.shortNameRes: Int
    get() = when (this) {
        SubmissionStep.SERVICE -> R.string.step_service_short
        SubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_short
        SubmissionStep.PROMO_CODE -> R.string.step_promo_code_short
        SubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_short
        SubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_short
        SubmissionStep.ELIGIBILITY -> R.string.step_eligibility_short
        SubmissionStep.DESCRIPTION -> R.string.step_description_short
        SubmissionStep.START_DATE -> R.string.step_start_date_short
        SubmissionStep.END_DATE -> R.string.step_end_date_short
    }

val SubmissionStep.hintRes: Int
    get() = when (this) {
        SubmissionStep.SERVICE -> R.string.step_service_hint
        SubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_hint
        SubmissionStep.PROMO_CODE -> R.string.step_promo_code_hint
        SubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_hint
        SubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_hint
        SubmissionStep.ELIGIBILITY -> R.string.step_eligibility_hint
        SubmissionStep.DESCRIPTION -> R.string.step_description_hint
        SubmissionStep.START_DATE -> R.string.step_start_date_hint
        SubmissionStep.END_DATE -> R.string.step_end_date_hint
    }
