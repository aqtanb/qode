package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.submission.ProgressiveStep
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

data class CompletedStepData(
    val step: ProgressiveStep,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val isEditable: Boolean = true,
    val fieldType: EditableFieldType = EditableFieldType.TEXT,
    val options: List<String> = emptyList()
)

enum class EditableFieldType {
    TEXT,
    DROPDOWN,
    NUMBER,
    PERCENTAGE,
    CURRENCY,
    DATE
}

@Composable
fun SmartProgressSummary(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    onEditStep: (ProgressiveStep) -> Unit,
    onUpdateField: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    val completedSteps = buildCompletedStepsList(
        currentStep = currentStep,
        wizardData = wizardData,
        primaryColor = MaterialTheme.colorScheme.primary,
        onPrimaryColor = MaterialTheme.colorScheme.onPrimary,
        secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onTertiaryContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
        primaryContainerColor = MaterialTheme.colorScheme.primaryContainer,
        onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "chevronRotation",
    )

    AnimatedVisibility(
        visible = completedSteps.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = ElevationTokens.small,
            ),
        ) {
            Column {
                // Header with toggle
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isExpanded = !isExpanded
                        },
                    color = Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Progress Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${completedSteps.size} steps completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isExpanded = !isExpanded
                            },
                        ) {
                            Icon(
                                imageVector = QodeActionIcons.Down,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(SizeTokens.Icon.sizeMedium)
                                    .rotate(rotationAngle),
                            )
                        }
                    }
                }

                // Expandable content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = SpacingTokens.lg,
                            end = SpacingTokens.lg,
                            bottom = SpacingTokens.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        completedSteps.forEachIndexed { index, stepData ->
                            CompletedStepItem(
                                stepData = stepData,
                                onEdit = { onEditStep(stepData.step) },
                                isLast = index == completedSteps.size - 1,
                            )
                        }
                    }
                }

                // Compact preview when collapsed
                AnimatedVisibility(
                    visible = !isExpanded && completedSteps.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    CompactStepsPreview(
                        completedSteps = completedSteps.take(3),
                        totalSteps = completedSteps.size,
                        modifier = Modifier.padding(
                            start = SpacingTokens.lg,
                            end = SpacingTokens.lg,
                            bottom = SpacingTokens.lg,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedStepItem(
    stepData: CompletedStepData,
    onEdit: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(stepData.value) }
    var showDropdown by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus when entering edit mode
    LaunchedEffect(isEditing) {
        if (isEditing && stepData.fieldType != EditableFieldType.DROPDOWN) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) ElevationTokens.medium else ElevationTokens.small,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Step icon
                Box(
                    modifier = Modifier
                        .size(SizeTokens.Icon.sizeXLarge)
                        .background(
                            color = if (isEditing) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                stepData.containerColor
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = isEditing,
                        transitionSpec = {
                            scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                        },
                        label = "iconAnimation",
                    ) { editing ->
                        Icon(
                            imageVector = if (editing) QodeActionIcons.Edit else stepData.icon,
                            contentDescription = null,
                            tint = if (editing) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                stepData.contentColor
                            },
                            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        )
                    }
                }

                // Step content - display or edit mode
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stepData.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isEditing) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium,
                    )

                    AnimatedContent(
                        targetState = isEditing,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "contentAnimation",
                    ) { editing ->
                        if (editing && stepData.isEditable) {
                            // Edit mode
                            when (stepData.fieldType) {
                                EditableFieldType.DROPDOWN -> {
                                    Box {
                                        OutlinedTextField(
                                            value = editValue,
                                            onValueChange = { },
                                            readOnly = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showDropdown = !showDropdown },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            ),
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = if (showDropdown) QodeActionIcons.Up else QodeActionIcons.Down,
                                                    contentDescription = null,
                                                    modifier = Modifier.clickable { showDropdown = !showDropdown },
                                                )
                                            },
                                        )

                                        DropdownMenu(
                                            expanded = showDropdown,
                                            onDismissRequest = { showDropdown = false },
                                        ) {
                                            stepData.options.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        editValue = option
                                                        showDropdown = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    OutlinedTextField(
                                        value = editValue,
                                        onValueChange = { editValue = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done,
                                            keyboardType = when (stepData.fieldType) {
                                                EditableFieldType.NUMBER,
                                                EditableFieldType.PERCENTAGE,
                                                EditableFieldType.CURRENCY -> KeyboardType.Number
                                                else -> KeyboardType.Text
                                            },
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                // TODO: Save changes
                                                isEditing = false
                                            },
                                        ),
                                    )
                                }
                            }
                        } else {
                            // Display mode
                            Column {
                                Text(
                                    text = stepData.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = if (isEditing) Int.MAX_VALUE else 1,
                                    overflow = if (isEditing) TextOverflow.Visible else TextOverflow.Ellipsis,
                                )
                                stepData.subtitle?.let { subtitle ->
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = if (isEditing) Int.MAX_VALUE else 1,
                                        overflow = if (isEditing) TextOverflow.Visible else TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                // Action buttons
                AnimatedContent(
                    targetState = isEditing,
                    transitionSpec = {
                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                    },
                    label = "actionsAnimation",
                ) { editing ->
                    if (editing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            // Cancel button
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    editValue = stepData.value
                                    isEditing = false
                                },
                            ) {
                                Icon(
                                    imageVector = QodeActionIcons.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                                )
                            }

                            // Save button
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // TODO: Trigger save action based on step type
                                    isEditing = false
                                },
                            ) {
                                Icon(
                                    imageVector = QodeActionIcons.Check,
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                                )
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            if (stepData.isEditable) {
                                // Inline edit button
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        editValue = stepData.value
                                        isEditing = true
                                    },
                                ) {
                                    Icon(
                                        imageVector = QodeActionIcons.Edit,
                                        contentDescription = "Edit inline",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                                    )
                                }
                            }

                            // Navigate to step button
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEdit()
                                },
                            ) {
                                Icon(
                                    imageVector = QodeActionIcons.Forward,
                                    contentDescription = "Go to ${stepData.title}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                                )
                            }
                        }
                    }
                }
            }

            // Edit mode helper text
            AnimatedVisibility(
                visible = isEditing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Text(
                    text = "Edit and press Done to save, or tap Cancel to discard changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = SpacingTokens.xs),
                )
            }
        }
    }
}

@Composable
private fun CompactStepsPreview(
    completedSteps: List<CompletedStepData>,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        completedSteps.forEach { stepData ->
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeLarge)
                    .background(
                        color = stepData.containerColor,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = stepData.icon,
                    contentDescription = null,
                    tint = stepData.contentColor,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        }

        if (totalSteps > 3) {
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeLarge)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+${totalSteps - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Tap to expand",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildCompletedStepsList(
    currentStep: ProgressiveStep,
    wizardData: SubmissionWizardData,
    primaryColor: Color,
    onPrimaryColor: Color,
    secondaryContainerColor: Color,
    onSecondaryContainerColor: Color,
    tertiaryContainerColor: Color,
    onTertiaryContainerColor: Color,
    primaryContainerColor: Color,
    onPrimaryContainerColor: Color
): List<CompletedStepData> {
    val steps = mutableListOf<CompletedStepData>()

    // Service
    if (currentStep.stepNumber > ProgressiveStep.SERVICE.stepNumber && wizardData.serviceName.isNotBlank()) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.SERVICE,
                title = "Service",
                value = wizardData.serviceName,
                icon = QodeCommerceIcons.Store,
                containerColor = primaryColor,
                contentColor = onPrimaryColor,
                fieldType = EditableFieldType.TEXT,
            ),
        )
    }

    // Discount Type
    if (currentStep.stepNumber > ProgressiveStep.DISCOUNT_TYPE.stepNumber && wizardData.promoCodeType != null) {
        val discountType = when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> "Percentage Discount"
            PromoCodeType.FIXED_AMOUNT -> "Fixed Amount Discount"
            null -> ""
        }
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.DISCOUNT_TYPE,
                title = "Discount Type",
                value = discountType,
                icon = QodeCommerceIcons.Sale,
                containerColor = secondaryContainerColor,
                contentColor = onSecondaryContainerColor,
                fieldType = EditableFieldType.DROPDOWN,
                options = listOf("Percentage Discount", "Fixed Amount Discount"),
            ),
        )
    }

    // Promo Code
    if (currentStep.stepNumber > ProgressiveStep.PROMO_CODE.stepNumber && wizardData.promoCode.isNotBlank()) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.PROMO_CODE,
                title = "Promo Code",
                value = wizardData.promoCode,
                icon = QodeCommerceIcons.PromoCode,
                containerColor = tertiaryContainerColor,
                contentColor = onTertiaryContainerColor,
                fieldType = EditableFieldType.TEXT,
            ),
        )
    }

    // Discount Value
    if (currentStep.stepNumber > ProgressiveStep.DISCOUNT_VALUE.stepNumber) {
        val discountValue = when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> "${wizardData.discountPercentage}%"
            PromoCodeType.FIXED_AMOUNT -> "${wizardData.discountAmount}₸"
            null -> ""
        }
        if (discountValue.isNotBlank()) {
            steps.add(
                CompletedStepData(
                    step = ProgressiveStep.DISCOUNT_VALUE,
                    title = "Discount Value",
                    value = discountValue,
                    subtitle = "Min order: ${wizardData.minimumOrderAmount}₸",
                    icon = QodeCommerceIcons.Dollar,
                    containerColor = secondaryContainerColor,
                    contentColor = onSecondaryContainerColor,
                    fieldType = when (wizardData.promoCodeType) {
                        PromoCodeType.PERCENTAGE -> EditableFieldType.PERCENTAGE
                        PromoCodeType.FIXED_AMOUNT -> EditableFieldType.CURRENCY
                        null -> EditableFieldType.NUMBER
                    },
                ),
            )
        }
    }

    // Options
    if (currentStep.stepNumber > ProgressiveStep.OPTIONAL.stepNumber) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.OPTIONAL,
                title = "Customer Eligibility",
                value = if (wizardData.isFirstUserOnly) "First-time customers only" else "All customers",
                subtitle = if (wizardData.description.isNotBlank()) wizardData.description else null,
                icon = QodeNavigationIcons.Settings,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
                fieldType = EditableFieldType.DROPDOWN,
                options = listOf("All customers", "First-time customers only"),
            ),
        )
    }

    // Start Date
    if (currentStep.stepNumber > ProgressiveStep.START_DATE.stepNumber) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.START_DATE,
                title = "Start Date",
                value = wizardData.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                icon = QodeUIIcons.Datepicker,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
                fieldType = EditableFieldType.DATE,
            ),
        )
    }

    // End Date
    if (currentStep.stepNumber > ProgressiveStep.END_DATE.stepNumber && wizardData.endDate != null) {
        steps.add(
            CompletedStepData(
                step = ProgressiveStep.END_DATE,
                title = "End Date",
                value = wizardData.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                icon = QodeUIIcons.Datepicker,
                containerColor = primaryContainerColor,
                contentColor = onPrimaryContainerColor,
                fieldType = EditableFieldType.DATE,
            ),
        )
    }

    return steps
}

@Preview(name = "Smart Progress Summary - Expanded", showBackground = true)
@Composable
private fun SmartProgressSummaryExpandedPreview() {
    QodeTheme {
        SmartProgressSummary(
            currentStep = ProgressiveStep.OPTIONAL,
            wizardData = SubmissionWizardData(
                selectedService = ServicePreviewData.netflix,
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "SAVE20",
                discountPercentage = "20",
                minimumOrderAmount = "500",
            ),
            onEditStep = {},
            onUpdateField = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
