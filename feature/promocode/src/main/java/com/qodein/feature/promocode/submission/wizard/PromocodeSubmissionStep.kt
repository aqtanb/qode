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
import com.qodein.feature.promocode.submission.validation.isValidServiceName
import com.qodein.feature.promocode.submission.validation.isValidServiceUrl
import com.qodein.core.ui.R as CoreUiR

enum class PromocodeSubmissionStep(val stepNumber: Int, val isRequired: Boolean = true) {
    SERVICE(1) {
        override fun canProceed(data: SubmissionWizardData): Boolean =
            data.selectedService != null ||
                (isValidServiceName(data.serviceName) && isValidServiceUrl(data.serviceUrl))
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

    fun next(): PromocodeSubmissionStep? =
        when (this) {
            SERVICE -> PROMOCODE
            PROMOCODE -> DISCOUNT_TYPE
            DISCOUNT_TYPE -> DISCOUNT_VALUE
            DISCOUNT_VALUE -> DATES
            DATES -> DESCRIPTION
            DESCRIPTION -> null
        }

    fun previous(): PromocodeSubmissionStep? =
        when (this) {
            SERVICE -> null
            PROMOCODE -> SERVICE
            DISCOUNT_TYPE -> PROMOCODE
            DISCOUNT_VALUE -> DISCOUNT_TYPE
            DATES -> DISCOUNT_VALUE
            DESCRIPTION -> DATES
        }
}

fun PromocodeSubmissionStep.stepIcon(isCompleted: Boolean = false): ImageVector =
    when {
        isCompleted -> QodeActionIcons.Check
        else -> when (this) {
            PromocodeSubmissionStep.SERVICE -> QodeIcons.Service
            PromocodeSubmissionStep.DISCOUNT_TYPE -> PromocodeIcons.DiscountType
            PromocodeSubmissionStep.PROMOCODE -> QodeIcons.Promocode
            PromocodeSubmissionStep.DISCOUNT_VALUE -> PromocodeIcons.DiscountValue
            PromocodeSubmissionStep.DATES -> PromocodeIcons.StartDate
            PromocodeSubmissionStep.DESCRIPTION -> PromocodeIcons.Description
        }
    }

val PromocodeSubmissionStep.titleRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> CoreUiR.string.step_service_title
        PromocodeSubmissionStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_title
        PromocodeSubmissionStep.PROMOCODE -> CoreUiR.string.step_promo_code_title
        PromocodeSubmissionStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_title
        PromocodeSubmissionStep.DATES -> CoreUiR.string.step_start_date_title
        PromocodeSubmissionStep.DESCRIPTION -> CoreUiR.string.step_description_title
    }

val PromocodeSubmissionStep.indicatorRes: Int
    get() = when (this) {
        PromocodeSubmissionStep.SERVICE -> CoreUiR.string.step_service_short
        PromocodeSubmissionStep.DISCOUNT_TYPE -> CoreUiR.string.step_discount_type_short
        PromocodeSubmissionStep.PROMOCODE -> CoreUiR.string.step_promo_code_short
        PromocodeSubmissionStep.DISCOUNT_VALUE -> CoreUiR.string.step_discount_value_short
        PromocodeSubmissionStep.DATES -> CoreUiR.string.step_start_date_short
        PromocodeSubmissionStep.DESCRIPTION -> CoreUiR.string.step_description_short
    }
