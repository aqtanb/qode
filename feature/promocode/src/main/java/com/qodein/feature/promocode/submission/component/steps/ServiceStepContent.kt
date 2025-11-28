package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.SubmissionStepCard
import com.qodein.feature.promocode.submission.component.SubmissionTextField
import com.qodein.shared.model.Service

@Composable
fun ServiceStepContent(
    selectedService: Service?,
    serviceName: String,
    isManualEntry: Boolean,
    onShowServiceSelector: () -> Unit,
    onServiceNameChange: (String) -> Unit,
    onToggleManualEntry: () -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        when (isManualEntry) {
            true -> {
                LaunchedEffect(Unit) {
                    // serviceSelectionUiState) {
                    focusRequester.requestFocus()
                }

                SubmissionTextField(
                    value = serviceName,
                    onValueChange = onServiceNameChange,
                    label = "Service Name",
                    placeholder = "Type the service name",
                    leadingIcon = QodeIcons.Store,
                    helperText = "Exact service name",
                    focusRequester = focusRequester,
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNextStep() },
                    ),
                )

                Text(
                    text = "Browse services instead",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleManualEntry() }
                        .padding(top = SpacingTokens.md),
                )
            }
            else -> {
                // Default mode - show service selector with manual as secondary
                ServiceSelector(
                    selectedService = selectedService,
                    placeholder = "Search for the service",
                    onServiceSelectorClick = onShowServiceSelector,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Can't find the service?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Type manually",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = SpacingTokens.xs)
                            .clickable { onToggleManualEntry() },
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceSelector(
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

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (hasSelection) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "backgroundColor",
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (hasSelection) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
        tonalElevation = if (hasSelection) ElevationTokens.none else ElevationTokens.none,
        shadowElevation = ElevationTokens.none,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SizeTokens.Selector.padding),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Service icon/logo
            if (hasSelection && selectedService.logoUrl != null) {
                CircularImage(
                    imageUrl = selectedService.logoUrl,
                    fallbackIcon = QodeIcons.Store,
                    contentDescription = "Service logo",
                    size = SizeTokens.Icon.sizeLarge,
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            } else {
                Box(
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (hasSelection) QodeIcons.Store else QodeNavigationIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        tint = if (hasSelection) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            // Service name or placeholder
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedService?.name ?: placeholder,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasSelection) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (hasSelection) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Chevron icon
            Icon(
                imageVector = QodeActionIcons.Next,
                contentDescription = null,
                tint = if (hasSelection) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ServiceSelectorPreview() {
    QodeTheme {
        SubmissionStepCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ServiceManualPreview() {
    QodeTheme {
        SubmissionStepCard(
            currentStep = PromocodeSubmissionStep.SERVICE,
            wizardData = SubmissionWizardData(isManualServiceEntry = true),
            onAction = {},
        )
    }
}
