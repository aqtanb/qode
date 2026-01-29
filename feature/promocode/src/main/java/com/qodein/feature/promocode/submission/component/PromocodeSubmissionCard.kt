package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeSubmissionAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.steps.DiscountValueStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDatesStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeDescriptionStep
import com.qodein.feature.promocode.submission.component.steps.PromocodeStep
import com.qodein.feature.promocode.submission.component.steps.ServiceStep
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep

@Composable
fun PromocodeSubmissionCard(
    currentStep: PromocodeWizardStep,
    wizardData: SubmissionWizardData,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width / 3 } + fadeOut()
            },
            label = "stepContent",
        ) { step ->
            PromocodeSubmissionCardContent(
                currentStep = step,
                wizardData = wizardData,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xl),
            )
        }
    }
}

@Composable
private fun PromocodeSubmissionCardContent(
    currentStep: PromocodeWizardStep,
    wizardData: SubmissionWizardData,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    Column(modifier = modifier.fillMaxWidth()) {
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

            PromocodeWizardStep.PROMOCODE -> PromocodeStep(
                promocode = wizardData.promocode,
                promocodeType = wizardData.promocodeType,
                onPromocodeChange = { onAction(PromocodeSubmissionAction.UpdatePromocode(it)) },
                onPromocodeTypeChange = { onAction(PromocodeSubmissionAction.UpdatePromocodeType(it)) },
                focusRequester = focusRequester,
                onNextStep = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            )

            PromocodeWizardStep.DISCOUNT_VALUE -> DiscountValueStep(
                promoCodeType = wizardData.promocodeType,
                discountPercentage = wizardData.discountPercentage,
                discountAmount = wizardData.discountAmount,
                freeItemDescription = wizardData.freeItemDescription,
                minimumOrderAmount = wizardData.minimumOrderAmount,
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
                imageUris = wizardData.imageUris,
                onDescriptionChange = { onAction(PromocodeSubmissionAction.UpdateDescription(it)) },
                onRemoveImage = { onAction(PromocodeSubmissionAction.RemoveImage(it)) },
                onNextStep = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PromocodeSubmissionCardPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.SERVICE,
            wizardData = SubmissionWizardData(),
            onAction = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Composable
internal fun PromocodeSubmissionBottomToolbar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disable: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SizeTokens.AppBar.heightSmall)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = NavigationIcons.Gallery,
            contentDescription = stringResource(R.string.cd_add_image_promocode),
            modifier = Modifier
                .size(SizeTokens.Icon.sizeLarge)
                .clickable { onClick() },
            tint = if (disable) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = OpacityTokens.DISABLED)
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
    }
}
