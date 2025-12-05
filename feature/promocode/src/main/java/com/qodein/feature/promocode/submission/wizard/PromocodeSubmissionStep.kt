package com.qodein.feature.promocode.submission.wizard

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.validation.isValidBusinessLogic
import com.qodein.feature.promocode.submission.validation.isValidDiscountValue
import com.qodein.feature.promocode.submission.validation.isValidMinimumOrderAmount
import com.qodein.feature.promocode.submission.validation.isValidPromoCodeFormat
import com.qodein.feature.promocode.submission.validation.isValidServiceName
import com.qodein.feature.promocode.submission.validation.isValidServiceUrl
import com.qodein.core.ui.R as CoreUiR

enum class PromocodeSubmissionStep(val stepNumber: Int, val isRequired: Boolean = true) {
    SERVICE(1) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            data.selectedService != null ||
                (isValidServiceName(data.serviceName) && isValidServiceUrl(data.serviceUrl))
    },
    DISCOUNT_TYPE(2) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promocodeType != null
    },
    PROMOCODE(3) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.code.isNotBlank() && isValidPromoCodeFormat(data.code)
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
    RULES(8, false) {
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
            DISCOUNT_TYPE -> PROMOCODE
            PROMOCODE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> MINIMUM_ORDER
            MINIMUM_ORDER -> START_DATE
            START_DATE -> END_DATE
            END_DATE -> RULES
            RULES -> DESCRIPTION
            DESCRIPTION -> null
        }

    fun previous(): PromocodeSubmissionStep? =
        when (this) {
            SERVICE -> null
            DISCOUNT_TYPE -> SERVICE
            PROMOCODE -> DISCOUNT_TYPE
            DISCOUNT_VALUE -> PROMOCODE
            MINIMUM_ORDER -> DISCOUNT_VALUE
            START_DATE -> MINIMUM_ORDER
            END_DATE -> START_DATE
            RULES -> END_DATE
            DESCRIPTION -> RULES
        }
}

// Centralized step icon logic
fun PromocodeSubmissionStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            PromocodeSubmissionStep.SERVICE -> QodeIcons.Store
            PromocodeSubmissionStep.DISCOUNT_TYPE -> PromocodeIcons.DiscountType
            PromocodeSubmissionStep.PROMOCODE -> QodeIcons.Promocode
            PromocodeSubmissionStep.DISCOUNT_VALUE -> PromocodeIcons.DiscountValue
            PromocodeSubmissionStep.MINIMUM_ORDER -> PromocodeIcons.MinimumOrder
            PromocodeSubmissionStep.RULES -> PromocodeIcons.Rules
            PromocodeSubmissionStep.DESCRIPTION -> PromocodeIcons.Description
            PromocodeSubmissionStep.START_DATE -> PromocodeIcons.StartDate
            PromocodeSubmissionStep.END_DATE -> PromocodeIcons.EndDate
        }
    }

// String resource extensions
val PromocodeSubmissionStep.titleRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> CoreUiR.string.step_service_title
        PromocodeSubmissionStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_title
        PromocodeSubmissionStep.PROMOCODE -> CoreUiR.string.step_promo_code_title
        PromocodeSubmissionStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_title
        PromocodeSubmissionStep.MINIMUM_ORDER -> CoreUiR.string.step_minimum_order_title
        PromocodeSubmissionStep.RULES -> CoreUiR.string.step_eligibility_title
        PromocodeSubmissionStep.DESCRIPTION -> CoreUiR.string.step_description_title
        PromocodeSubmissionStep.START_DATE -> CoreUiR.string.step_start_date_title
        PromocodeSubmissionStep.END_DATE -> CoreUiR.string.step_end_date_title
    }

val PromocodeSubmissionStep.shortNameRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> CoreUiR.string.step_service_short
        PromocodeSubmissionStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_short
        PromocodeSubmissionStep.PROMOCODE -> CoreUiR.string.step_promo_code_short
        PromocodeSubmissionStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_short
        PromocodeSubmissionStep.MINIMUM_ORDER -> CoreUiR.string.step_minimum_order_short
        PromocodeSubmissionStep.RULES -> CoreUiR.string.step_eligibility_short
        PromocodeSubmissionStep.DESCRIPTION -> CoreUiR.string.step_description_short
        PromocodeSubmissionStep.START_DATE -> CoreUiR.string.step_start_date_short
        PromocodeSubmissionStep.END_DATE -> CoreUiR.string.step_end_date_short
    }

val PromocodeSubmissionStep.hintRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> R.string.step_service_hint
        PromocodeSubmissionStep.DISCOUNT_TYPE -> R.string.step_discount_type_hint
        PromocodeSubmissionStep.PROMOCODE -> R.string.step_promo_code_hint
        PromocodeSubmissionStep.DISCOUNT_VALUE -> R.string.step_discount_value_hint
        PromocodeSubmissionStep.MINIMUM_ORDER -> R.string.step_minimum_order_hint
        PromocodeSubmissionStep.RULES -> R.string.step_eligibility_hint
        PromocodeSubmissionStep.DESCRIPTION -> R.string.step_description_hint
        PromocodeSubmissionStep.START_DATE -> R.string.step_start_date_hint
        PromocodeSubmissionStep.END_DATE -> R.string.step_end_date_hint
    }
