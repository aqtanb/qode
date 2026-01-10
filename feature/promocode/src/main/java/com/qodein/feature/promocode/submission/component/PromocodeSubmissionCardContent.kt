package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DiscountValueStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDatesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDescriptionStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeTypeStep
import com.qodein.feature.promocode.submission.component.steps.ServiceStep
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep

@Composable
fun PromocodeSubmissionCardContent(
    currentStep: PromocodeWizardStep,
    wizardData: SubmissionWizardData,
    validation: ValidationState,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        when (currentStep) {
            PromocodeWizardStep.SERVICE -> ServiceStep(
                selectedService = wizardData.selectedService,
                serviceNameInput = wizardData.serviceName,
                serviceUrlInput = wizardData.serviceUrl,
                isManualEntry = wizardData.isManualServiceEntry,
                onShowServiceSelector = { onAction(PromocodeSubmissionAction.ShowServiceSelector) },
                onServiceNameChange = { onAction(PromocodeSubmissionAction.UpdateServiceName(it)) },
                onServiceUrlChange = { onAction(PromocodeSubmissionAction.UpdateServiceUrl(it)) },
                onToggleManualEntry = { onAction(PromocodeSubmissionAction.ToggleManualEntry) },
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeWizardStep.DISCOUNT_TYPE -> PromocodeTypeStep(
                selectedType = wizardData.promocodeType,
                onTypeSelected = { onAction(PromocodeSubmissionAction.UpdatePromocodeType(it)) },
            )

            PromocodeWizardStep.PROMOCODE -> PromocodeStep(
                promocode = wizardData.code,
                onPromocodeChange = { onAction(PromocodeSubmissionAction.UpdatePromocode(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeWizardStep.DISCOUNT_VALUE -> DiscountValueStep(
                promoCodeType = wizardData.promocodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                freeItemDescription = wizardData.freeItemDescription,
                minimumOrderAmount = wizardData.minimumOrderAmount,
                wizardData = wizardData,
                onDiscountPercentageChange = { onAction(PromocodeSubmissionAction.UpdateDiscountPercentage(it)) },
                onDiscountAmountChange = { onAction(PromocodeSubmissionAction.UpdateDiscountAmount(it)) },
                onFreeItemDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateFreeItemDescription(it)) },
                onMinimumOrderAmountChange = { onAction(PromocodeSubmissionAction.UpdateMinimumOrderAmount(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeWizardStep.DATES -> PromocodeDatesStep(
                startDate = wizardData.startDate,
                endDate = wizardData.endDate,
                onStartDateSelected = { onAction(PromocodeSubmissionAction.UpdateStartDate(it)) },
                onEndDateSelected = { onAction(PromocodeSubmissionAction.UpdateEndDate(it)) },
            )

            PromocodeWizardStep.DESCRIPTION -> PromocodeDescriptionStep(
                description = wizardData.description,
                onDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
            )
        }
    }
}
