package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DiscountValueStep
import com.qodein.feature.promocode.submission.component.steps.MinimumOrderAmountStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDatesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDescriptionStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeTypeStep
import com.qodein.feature.promocode.submission.component.steps.ServiceStep
import com.qodein.feature.promocode.submission.validation.SubmissionField
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep

@Composable
fun PromocodeSubmissionCardContent(
    currentStep: PromocodeSubmissionStep,
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
            PromocodeSubmissionStep.SERVICE -> ServiceStep(
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

            PromocodeSubmissionStep.DISCOUNT_TYPE -> PromocodeTypeStep(
                selectedType = wizardData.promocodeType,
                onTypeSelected = { onAction(PromocodeSubmissionAction.UpdatePromocodeType(it)) },
            )

            PromocodeSubmissionStep.PROMOCODE -> PromocodeStep(
                promocode = wizardData.code,
                onPromocodeChange = { onAction(PromocodeSubmissionAction.UpdatePromocode(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
                promocodeError = validation.fieldErrors[SubmissionField.PROMO_CODE],
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

            PromocodeSubmissionStep.DESCRIPTION -> PromocodeDescriptionStep(
                description = wizardData.description,
                onDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateDescription(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
            )

            PromocodeSubmissionStep.START_DATE -> PromocodeDatesStep(
                selectedDate = wizardData.startDate,
                onDateSelected = { onAction(PromocodeSubmissionAction.UpdateStartDate(it)) },
                placeholder = stringResource(R.string.promocode_dates_start_placeholder),
            )

            PromocodeSubmissionStep.END_DATE -> PromocodeDatesStep(
                selectedDate = wizardData.endDate,
                onDateSelected = { date ->
                    onAction(PromocodeSubmissionAction.UpdateEndDate(date))
                },
                placeholder = stringResource(R.string.promocode_dates_end_placeholder),
            )
        }
    }
}
