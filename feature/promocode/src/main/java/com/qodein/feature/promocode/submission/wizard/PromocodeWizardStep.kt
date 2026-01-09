package com.qodein.feature.promocode.submission.wizard

import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.validation.isValidBusinessLogic
import com.qodein.feature.promocode.submission.validation.isValidDiscountValue
import com.qodein.feature.promocode.submission.validation.isValidMinimumOrderAmount
import com.qodein.feature.promocode.submission.validation.isValidPromoCodeFormat
import com.qodein.core.ui.R as CoreUiR

enum class PromocodeWizardStep(val stepNumber: Int, val isRequired: Boolean = true) {
    SERVICE(1) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            data.selectedService != null ||
                (data.serviceName.isNotBlank() && data.serviceUrl.isNotBlank())
    },
    PROMOCODE(2) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.code.isNotBlank() && isValidPromoCodeFormat(data.code)
    },
    DISCOUNT_TYPE(3) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.promocodeType != null
    },
    DISCOUNT_VALUE(4) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            isValidDiscountValue(data) && isValidMinimumOrderAmount(data.minimumOrderAmount) && isValidBusinessLogic(data)
    },
    DATES(5, true) {
        override fun canProceed(data: SubmissionWizardData): Boolean = data.endDate != null && data.endDate.isAfter(data.startDate)
    },
    DESCRIPTION(6, false) {
        override fun canProceed(data: SubmissionWizardData): Boolean = true
    };

    abstract fun canProceed(data: SubmissionWizardData): Boolean

    val isFirst: Boolean get() = this == SERVICE
    val isLast: Boolean get() = this == DESCRIPTION

    fun next(): PromocodeWizardStep? =
        when (this) {
            SERVICE -> PROMOCODE
            PROMOCODE -> DISCOUNT_TYPE
            DISCOUNT_TYPE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> DATES
            DATES -> DESCRIPTION
            DESCRIPTION -> null
        }

    fun previous(): PromocodeWizardStep? =
        when (this) {
            SERVICE -> null
            PROMOCODE -> SERVICE
            DISCOUNT_TYPE -> PROMOCODE
            DISCOUNT_VALUE -> DISCOUNT_TYPE
            DATES -> DISCOUNT_VALUE
            DESCRIPTION -> DATES
        }
}

fun PromocodeWizardStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            PromocodeWizardStep.SERVICE -> QodeIcons.Service
            PromocodeWizardStep.DISCOUNT_TYPE -> PromocodeIcons.DiscountType
            PromocodeWizardStep.PROMOCODE -> QodeIcons.Promocode
            PromocodeWizardStep.DISCOUNT_VALUE -> PromocodeIcons.DiscountValue
            PromocodeWizardStep.DATES -> PromocodeIcons.StartDate
            PromocodeWizardStep.DESCRIPTION -> PromocodeIcons.Description
        }
    }

val PromocodeWizardStep.titleRes: Int
    get() = when (this) {
        PromocodeWizardStep.SERVICE -> CoreUiR.string.step_service_title
        PromocodeWizardStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_title
        PromocodeWizardStep.PROMOCODE -> CoreUiR.string.step_promo_code_title
        PromocodeWizardStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_title
        PromocodeWizardStep.DATES -> CoreUiR.string.step_start_date_title
        PromocodeWizardStep.DESCRIPTION -> CoreUiR.string.step_description_title
    }

val PromocodeWizardStep.indicatorRes: Int
    get() = when (this) {
        PromocodeWizardStep.SERVICE -> CoreUiR.string.step_service_short
        PromocodeWizardStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_short
        PromocodeWizardStep.PROMOCODE -> CoreUiR.string.step_promo_code_short
        PromocodeWizardStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_short
        PromocodeWizardStep.DATES -> CoreUiR.string.step_start_date_short
        PromocodeWizardStep.DESCRIPTION -> CoreUiR.string.step_description_short
    }
