package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinAsyncImage
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.Service

@Composable
fun ServiceStep(
    selectedService: Service?,
    serviceNameInput: String,
    serviceUrlInput: String,
    isManualEntry: Boolean,
    onShowServiceSelector: () -> Unit,
    onServiceNameChange: (String) -> Unit,
    onServiceUrlChange: (String) -> Unit,
    onToggleManualEntry: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        when (isManualEntry) {
            true -> {
                ManualServiceEntry(
                    serviceName = serviceNameInput,
                    serviceUrl = serviceUrlInput,
                    onServiceNameChange = onServiceNameChange,
                    onServiceUrlChange = onServiceUrlChange,
                    onNextStep = onNextStep,
                    onToggleManualEntry = onToggleManualEntry,
                )
            }
            else -> {
                ServiceSelection(
                    selectedService = selectedService,
                    onShowServiceSelector = onShowServiceSelector,
                    onToggleManualEntry = onToggleManualEntry,
                )
            }
        }
    }
}

@Composable
private fun ServiceSelection(
    selectedService: Service?,
    onShowServiceSelector: () -> Unit,
    onToggleManualEntry: () -> Unit
) {
    SelectServiceButton(
        selectedService = selectedService,
        placeholder = stringResource(R.string.service_step_search_placeholder),
        onServiceSelectorClick = onShowServiceSelector,
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = SpacingTokens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.service_step_cant_find_service),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.service_step_type_manually),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = SpacingTokens.xs)
                .clickable { onToggleManualEntry() },
        )
    }
}

@Composable
private fun ManualServiceEntry(
    serviceName: String,
    serviceUrl: String,
    onServiceNameChange: (String) -> Unit,
    onServiceUrlChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onToggleManualEntry: () -> Unit
) {
    val urlFocusRequester = remember { FocusRequester() }

    Column {
        Text(
            text = stringResource(R.string.promocode_service_choose_name),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        ServiceNameField(
            value = serviceName,
            onServiceNameChange = onServiceNameChange,
            onMoveToNext = { urlFocusRequester.requestFocus() },
            modifier = Modifier.padding(bottom = SpacingTokens.xl),
        )

        Text(
            text = stringResource(R.string.promocode_service_choose_url),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        ServiceUrlField(
            value = serviceUrl,
            onServiceUrlChange = onServiceUrlChange,
            onSubmitForm = onNextStep,
            focusRequester = urlFocusRequester,
            modifier = Modifier.padding(bottom = SpacingTokens.xl),
        )

        Text(
            text = stringResource(R.string.service_step_browse_services_instead),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleManualEntry() },
        )
    }
}

@Composable
private fun ServiceNameField(
    value: String,
    onServiceNameChange: (String) -> Unit,
    onMoveToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinTextField(
        value = value,
        onValueChange = { onServiceNameChange(it) },
        placeholder = stringResource(R.string.service_step_placeholder_service_name),
        leadingIcon = QodeIcons.Service,
        helperText = stringResource(R.string.service_step_helper_service_name),
        maxLength = Service.NAME_MAX_LENGTH,
        canBeBlank = false,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onMoveToNext() },
        ),
        modifier = modifier,
    )
}

@Composable
private fun ServiceUrlField(
    value: String,
    onServiceUrlChange: (String) -> Unit,
    onSubmitForm: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    QodeinTextField(
        value = value,
        onValueChange = { onServiceUrlChange(it) },
        placeholder = stringResource(R.string.service_step_placeholder_service_url),
        leadingIcon = ActionIcons.Share,
        helperText = stringResource(R.string.service_step_helper_service_url),
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Uri,
            capitalization = KeyboardCapitalization.None,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onSubmitForm() },
        ),
        showPasteIcon = true,
        maxLength = Service.SITE_URL_MAX_LENGTH,
        modifier = modifier,
    )
}

@Composable
fun SelectServiceButton(
    selectedService: Service?,
    placeholder: String,
    onServiceSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hasSelection = selectedService != null

    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = AnimationTokens.Spec.emphasized(),
        label = "scale",
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (hasSelection) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "borderColor",
    )

    Column {
        Text(
            text = stringResource(R.string.promocode_service_choose_selector_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(SizeTokens.Selector.height)
                .scale(animatedScale)
                .border(
                    width = ShapeTokens.Border.thin,
                    color = animatedBorderColor,
                    shape = RoundedCornerShape(SizeTokens.Selector.shape),
                )
                .clip(RoundedCornerShape(SizeTokens.Selector.shape))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onServiceSelectorClick,
                ),
            shape = RoundedCornerShape(SizeTokens.Selector.shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SizeTokens.Selector.padding),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasSelection && selectedService.logoUrl != null) {
                    QodeinAsyncImage(
                        imageUrl = selectedService.logoUrl ?: "",
                        contentDescription = stringResource(R.string.service_step_service_logo_cd),
                        size = SizeTokens.Icon.sizeMedium,
                        fallbackText = selectedService.name,
                    )
                } else {
                    Icon(
                        imageVector = if (hasSelection) QodeIcons.Service else NavigationIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = selectedService?.name ?: placeholder,
                    fontWeight = if (hasSelection) FontWeight.SemiBold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    imageVector = NavigationIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ServiceSelectorPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.SERVICE,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ServiceSelectedPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.SERVICE,
            wizardData = SubmissionWizardData(selectedService = ServicePreviewData.yandex),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ServiceManualPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.SERVICE,
            wizardData = SubmissionWizardData(isManualServiceEntry = true),
            onAction = {},
        )
    }
}
