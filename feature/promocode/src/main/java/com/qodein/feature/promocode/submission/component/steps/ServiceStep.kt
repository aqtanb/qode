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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.validation.ValidationState
import com.qodein.feature.promocode.submission.validation.isValidServiceUrl
import com.qodein.feature.promocode.submission.validation.sanitizeServiceUrl
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
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
private fun ManualServiceEntry(
    serviceName: String,
    serviceUrl: String,
    onServiceNameChange: (String) -> Unit,
    onServiceUrlChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onToggleManualEntry: () -> Unit
) {
    val urlFocusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        ServiceNameField(
            value = serviceName,
            onValueChange = onServiceNameChange,
            onMoveToNext = { urlFocusRequester.requestFocus() },
        )

        ServiceUrlField(
            value = serviceUrl,
            onValueChange = onServiceUrlChange,
            onSubmitForm = onNextStep,
            focusRequester = urlFocusRequester,
        )

        Text(
            text = stringResource(R.string.service_step_browse_services_instead),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleManualEntry() }
                .padding(top = SpacingTokens.md),
        )
    }
}

@Composable
private fun ServiceNameField(
    value: String,
    onValueChange: (String) -> Unit,
    onMoveToNext: () -> Unit
) {
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankErrorText = stringResource(R.string.service_step_error_blank)
    val maxLengthErrorText = stringResource(
        R.string.service_step_error_max_length,
        Service.NAME_MAX_LENGTH,
    )

    QodeinTextField(
        value = value,
        onValueChange = { newValue ->
            val clamped = newValue.take(Service.NAME_MAX_LENGTH)
            errorText = when {
                newValue.length >= Service.NAME_MAX_LENGTH -> maxLengthErrorText
                else -> null
            }
            onValueChange(clamped)
        },
        placeholder = stringResource(R.string.service_step_placeholder_service_name),
        leadingIcon = QodeIcons.Service,
        helperText = stringResource(R.string.service_step_helper_service_name),
        errorText = errorText,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (value.isBlank()) {
                    errorText = blankErrorText
                } else {
                    errorText = null
                    onMoveToNext()
                }
            },
        ),
    )
}

@Composable
private fun ServiceUrlField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmitForm: () -> Unit,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val blankUrlErrorText = stringResource(R.string.service_step_error_url_blank)
    val invalidUrlErrorText = stringResource(R.string.service_step_error_url_format)

    QodeinTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue
                .lowercase()
                .filter { it.isLetterOrDigit() || it in setOf('.', '-', '/') }
            val trimmed = sanitizeServiceUrl(filtered)
            errorText = null
            onValueChange(trimmed)
        },
        placeholder = stringResource(R.string.service_step_placeholder_service_url),
        leadingIcon = QodeActionIcons.Share,
        helperText = stringResource(R.string.service_step_helper_service_url),
        errorText = errorText,
        focusRequester = focusRequester,
        keyboardOptions = KeyboardOptions(
            imeAction = if (value.isEmpty()) ImeAction.Previous else ImeAction.Next,
            keyboardType = KeyboardType.Uri,
            capitalization = KeyboardCapitalization.None,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                keyboardController?.hide()
                when {
                    value.isBlank() -> {
                        errorText = blankUrlErrorText
                    }
                    !isValidServiceUrl(value) -> {
                        errorText = invalidUrlErrorText
                    }
                    else -> {
                        errorText = null
                        onSubmitForm()
                    }
                }
            },
        ),
    )
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

    HorizontalDivider(modifier = Modifier.padding(vertical = SpacingTokens.md))

    Column(
        modifier = Modifier.fillMaxWidth(),
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
        color = Color.Transparent,
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
                CircularImage(
                    imageUrl = selectedService.logoUrl,
                    fallbackIcon = QodeIcons.Service,
                    contentDescription = stringResource(R.string.service_step_service_logo_cd),
                    size = SizeTokens.Icon.sizeMedium,
                )
            } else {
                Icon(
                    imageVector = if (hasSelection) QodeIcons.Service else QodeNavigationIcons.Search,
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
                imageVector = QodeActionIcons.Next,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ServiceSelectorPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ServiceSelectedPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(selectedService = ServicePreviewData.yandex),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ServiceManualPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(isManualServiceEntry = true),
            validation = ValidationState.valid(),
            onAction = {},
        )
    }
}
