package com.qodein.feature.promocode.submission

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCalendarIcons
import com.qodein.core.designsystem.icon.QodeEssentialIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.validation.isValidBusinessLogic
import com.qodein.feature.promocode.submission.validation.isValidDiscountValue
import com.qodein.feature.promocode.submission.validation.isValidMinimumOrderAmount
import com.qodein.feature.promocode.submission.validation.isValidPromoCodeFormat
import com.qodein.feature.promocode.submission.validation.isValidServiceName

enum class PromocodeSubmissionStep(val stepNumber: Int, val isRequired: Boolean = true) {
    SERVICE(1) {
        override fun canProceed(data: SubmissionWizardData): Boolean = isValidServiceName(data.effectiveServiceName)
    },
    DISCOUNT_TYPE(2) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCodeType != null
    },
    PROMO_CODE(3) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promoCode.isNotBlank() && isValidPromoCodeFormat(data.promoCode)
    },
    DISCOUNT_VALUE(4) {
        override fun canProceed(data: SubmissionWizardData): Boolean = isValidDiscountValue(data)
    },
    MINIMUM_ORDER(5) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            isValidMinimumOrderAmount(data.minimumOrderAmount) && isValidBusinessLogic(data)
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

    fun next(): PromocodeSubmissionStep? =
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

    fun previous(): PromocodeSubmissionStep? =
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
fun PromocodeSubmissionStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            PromocodeSubmissionStep.SERVICE -> QodeEssentialIcons.Store
            PromocodeSubmissionStep.DISCOUNT_TYPE -> QodeEssentialIcons.Sale
            PromocodeSubmissionStep.PROMO_CODE -> QodeEssentialIcons.PromoCode
            PromocodeSubmissionStep.DISCOUNT_VALUE -> QodeEssentialIcons.Dollar
            PromocodeSubmissionStep.MINIMUM_ORDER -> QodeEssentialIcons.Dollar
            PromocodeSubmissionStep.ELIGIBILITY -> QodeNavigationIcons.Settings
            PromocodeSubmissionStep.DESCRIPTION -> QodeCalendarIcons.Info
            PromocodeSubmissionStep.START_DATE, PromocodeSubmissionStep.END_DATE -> QodeCalendarIcons.Datepicker
        }
    }

// String resource extensions
val PromocodeSubmissionStep.titleRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> R.string.step_service_title
        PromocodeSubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_title
        PromocodeSubmissionStep.PROMO_CODE -> R.string.step_promo_code_title
        PromocodeSubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_title
        PromocodeSubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_title
        PromocodeSubmissionStep.ELIGIBILITY -> R.string.step_eligibility_title
        PromocodeSubmissionStep.DESCRIPTION -> R.string.step_description_title
        PromocodeSubmissionStep.START_DATE -> R.string.step_start_date_title
        PromocodeSubmissionStep.END_DATE -> R.string.step_end_date_title
    }

val PromocodeSubmissionStep.shortNameRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> R.string.step_service_short
        PromocodeSubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_short
        PromocodeSubmissionStep.PROMO_CODE -> R.string.step_promo_code_short
        PromocodeSubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_short
        PromocodeSubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_short
        PromocodeSubmissionStep.ELIGIBILITY -> R.string.step_eligibility_short
        PromocodeSubmissionStep.DESCRIPTION -> R.string.step_description_short
        PromocodeSubmissionStep.START_DATE -> R.string.step_start_date_short
        PromocodeSubmissionStep.END_DATE -> R.string.step_end_date_short
    }

val PromocodeSubmissionStep.hintRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> R.string.step_service_hint
        PromocodeSubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_hint
        PromocodeSubmissionStep.PROMO_CODE -> R.string.step_promo_code_hint
        PromocodeSubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_hint
        PromocodeSubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_hint
        PromocodeSubmissionStep.ELIGIBILITY -> R.string.step_eligibility_hint
        PromocodeSubmissionStep.DESCRIPTION -> R.string.step_description_hint
        PromocodeSubmissionStep.START_DATE -> R.string.step_start_date_hint
        PromocodeSubmissionStep.END_DATE -> R.string.step_end_date_hint
    }
