package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DiscountValueStep
import com.qodein.feature.promocode.submission.component.steps.MinimumOrderAmountStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDatesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDescriptionStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeRulesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeTypeStep
import com.qodein.feature.promocode.submission.component.steps.ServiceStep

internal enum class FieldValidationState {
    IDLE,
    VALID,
    ERROR
}

@Composable
fun PromocodeSubmissionCardContent(
    currentStep: PromocodeSubmissionStep,
    wizardData: SubmissionWizardData,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        when (currentStep) {
            PromocodeSubmissionStep.SERVICE -> ServiceStep(
                selectedService = wizardData.selectedService,
                serviceName = wizardData.serviceName,
                isManualEntry = wizardData.isManualServiceEntry,
                onShowServiceSelector = { onAction(PromocodeSubmissionAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(PromocodeSubmissionAction.UpdateServiceName(it)) },
                onToggleManualEntry = { onAction(PromocodeSubmissionAction.ToggleManualEntry) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_TYPE -> PromocodeTypeStep(
                selectedType = wizardData.promocodeType,
                onTypeSelected = { onAction(PromocodeSubmissionAction.UpdatePromocodeType(it)) },
            )

            PromocodeSubmissionStep.PROMOCODE -> PromocodeStep(
                promoCode = wizardData.promoCode,
                onPromoCodeChange = { onAction(PromocodeSubmissionAction.UpdatePromoCode(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.DISCOUNT_VALUE -> DiscountValueStep(
                promoCodeType = wizardData.promocodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                onDiscountPercentageChange = { onAction(PromocodeSubmissionAction.UpdateDiscountPercentage(it)) },
                onDiscountAmountChange = { onAction(PromocodeSubmissionAction.UpdateDiscountAmount(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.MINIMUM_ORDER -> MinimumOrderAmountStep(
                minimumOrderAmount = wizardData.minimumOrderAmount,
                onMinimumOrderAmountChange = { onAction(PromocodeSubmissionAction.UpdateMinimumOrderAmount(it)) },
                focusRequester = focusRequester,
                wizardData = wizardData,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeSubmissionStep.ELIGIBILITY -> PromocodeRulesStep(
                isFirstUserOnly = wizardData.isFirstUserOnly,
                isOneTimeUseOnly = wizardData.isOneTimeUseOnly,
                onFirstUserOnlyChange = { onAction(PromocodeSubmissionAction.UpdateFirstUserOnly(it)) },
                onOneTimeUseOnlyChange = { onAction(PromocodeSubmissionAction.UpdateOneTimeUseOnly(it)) },
                focusRequester = focusRequester,
            )

            PromocodeSubmissionStep.DESCRIPTION -> PromocodeDescriptionStep(
                description = wizardData.description,
                onDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
            )

            PromocodeSubmissionStep.START_DATE -> PromocodeDatesStep(
                label = "Start Date",
                selectedDate = wizardData.startDate,
                onDateSelected = { onAction(PromocodeSubmissionAction.UpdateStartDate(it)) },
                placeholder = "Select start date",
                isRequired = true,
            )

            PromocodeSubmissionStep.END_DATE -> PromocodeDatesStep(
                label = "End Date",
                selectedDate = wizardData.endDate,
                onDateSelected = { date ->
                    onAction(PromocodeSubmissionAction.UpdateEndDate(date))
                },
                placeholder = "Select end date",
                isRequired = true,
            )
        }
    }
}
