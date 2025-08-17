package com.qodein.feature.promocode.submission

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomWizardController(
    uiState: SubmissionWizardUiState.Success,
    onAction: (SubmissionWizardAction) -> Unit,
    onFocusField: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for service selector
    val showServiceSelector = remember { mutableStateOf(false) }
    val serviceSheetState = rememberModalBottomSheetState()

    // Swipe state for controller
    val swipeOffset = remember { mutableFloatStateOf(0f) }

    // Floating gaming HUD controller with bottom padding
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = SpacingTokens.xl) // Add extra bottom margin for better spacing
            .pointerInput(uiState.currentStep) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val swipeThreshold = 30f
                        when {
                            swipeOffset.floatValue > swipeThreshold && uiState.canGoPrevious -> {
                                onAction(SubmissionWizardAction.GoToPreviousStep)
                            }
                            swipeOffset.floatValue < -swipeThreshold && uiState.canGoNext -> {
                                onAction(SubmissionWizardAction.GoToNextStep)
                            }
                        }
                        swipeOffset.floatValue = 0f
                    },
                ) { _, dragAmount ->
                    swipeOffset.floatValue += dragAmount
                }
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Gaming HUD Surface (non-interactive for gestures)
        Surface(
            modifier = Modifier.size(width = 280.dp, height = 72.dp), // Compact gaming controller size
            shape = RoundedCornerShape(36.dp), // More circular for gaming aesthetic
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // Use theme surface color
            shadowElevation = 24.dp,
        ) {
            // Neon border effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            ),
                        ),
                        shape = RoundedCornerShape(36.dp),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Compact progress circles
                    GamingProgressIndicator(
                        currentStep = uiState.currentStep,
                        modifier = Modifier.weight(1f),
                    )

                    // Interactive action chips
                    GamingActionChips(
                        currentStep = uiState.currentStep,
                        wizardData = uiState.wizardData,
                        onAction = onAction,
                        onOpenServiceSelector = { showServiceSelector.value = true },
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        }
    }

    // Service Selector Bottom Sheet
    ServiceSelectorBottomSheet(
        isVisible = showServiceSelector.value,
        services = uiState.availableServices,
        currentSelection = uiState.wizardData.serviceName,
        onServiceSelected = { service ->
            onAction(SubmissionWizardAction.UpdateServiceName(service.name))
            showServiceSelector.value = false
        },
        onDismiss = { showServiceSelector.value = false },
        onSearch = { query ->
            onAction(SubmissionWizardAction.SearchServices(query))
        },
        isLoading = uiState.isLoadingServices,
        sheetState = serviceSheetState,
    )
}

@Composable
private fun GeekProgressIndicator(
    currentStep: SubmissionWizardStep,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        // Cyberpunk-style step circles
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubmissionWizardStep.entries.forEach { step ->
                GeekStepCircle(
                    step = step,
                    isActive = step == currentStep,
                    isCompleted = step.stepNumber < currentStep.stepNumber,
                )

                // Animated connection line (except after last step)
                if (step != SubmissionWizardStep.entries.last()) {
                    GeekConnectionLine(
                        isActive = step.stepNumber < currentStep.stepNumber,
                    )
                }
            }
        }

        // Glowing step info with neon effect
        Text(
            text = "STEP ${currentStep.stepNumber}/${SubmissionWizardStep.entries.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
    }
}

@Composable
private fun GeekStepCircle(
    step: SubmissionWizardStep,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "step_circle_scale",
    )

    val circleColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "circle_color",
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "border_color",
    )

    // Outer glow effect for active step
    val glowBrush = if (isActive) {
        Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0f),
            ),
            radius = 40f,
        )
    } else {
        null
    }

    if (glowBrush != null) {
        // Glow effect background
        Box(
            modifier = modifier
                .size(40.dp)
                .scale(scale * 1.5f)
                .background(glowBrush, CircleShape),
            contentAlignment = Alignment.Center,
        ) {}
    }

    // Main circle
    Box(
        modifier = modifier
            .size(20.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .background(circleColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = step.stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun GeekConnectionLine(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "line_color",
    )

    Box(
        modifier = modifier
            .width(16.dp)
            .height(2.dp)
            .background(
                brush = if (isActive) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        ),
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(lineColor, lineColor),
                    )
                },
                shape = RoundedCornerShape(1.dp),
            ),
    )
}

@Composable
private fun InteractiveFieldNavigation(
    currentStep: SubmissionWizardStep,
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    onFocusField: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fields = when (currentStep) {
        SubmissionWizardStep.SERVICE_AND_TYPE -> listOf(
            InteractiveFieldInfo("Service", wizardData.serviceName.isNotBlank(), FieldAction.OpenServiceSelector),
            InteractiveFieldInfo("Type", wizardData.promoCodeType != null, FieldAction.ToggleType),
        )
        SubmissionWizardStep.TYPE_DETAILS -> listOf(
            InteractiveFieldInfo("Promo Code", wizardData.promoCode.isNotBlank(), FieldAction.FocusField),
            InteractiveFieldInfo(
                "Percentage",
                wizardData.discountPercentage.isNotBlank() || wizardData.discountAmount.isNotBlank(),
                FieldAction.FocusField,
            ),
            InteractiveFieldInfo("Min Order", wizardData.minimumOrderAmount.isNotBlank(), FieldAction.FocusField),
        )
        SubmissionWizardStep.DATE_SETTINGS -> listOf(
            InteractiveFieldInfo("Start Date", true, FieldAction.FocusField),
            InteractiveFieldInfo(
                "End Date",
                wizardData.endDate != null && wizardData.endDate!!.isAfter(wizardData.startDate),
                FieldAction.FocusField,
            ),
        )
        SubmissionWizardStep.OPTIONAL_DETAILS -> listOf(
            InteractiveFieldInfo("Title", wizardData.title.isNotBlank(), FieldAction.FocusField),
            InteractiveFieldInfo("Description", wizardData.description.isNotBlank(), FieldAction.FocusField),
            InteractiveFieldInfo("Screenshot", wizardData.screenshotUrl != null, FieldAction.FocusField),
        )
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        items(fields) { field ->
            InteractiveFieldChip(
                field = field,
                wizardData = wizardData,
                onAction = onAction,
                onFocusField = onFocusField,
            )
        }
    }
}

@Composable
private fun InteractiveFieldChip(
    field: InteractiveFieldInfo,
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    onFocusField: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (field.isFilled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "chip_background",
    )

    val textColor by animateColorAsState(
        targetValue = if (field.isFilled) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "chip_text",
    )

    Card(
        modifier = modifier
            .clickable {
                when (field.action) {
                    FieldAction.FocusField -> onFocusField(field.name)
                    FieldAction.ToggleType -> {
                        // Cycle between percentage and fixed amount
                        val newType = when (wizardData.promoCodeType) {
                            PromoCodeType.PERCENTAGE -> PromoCodeType.FIXED_AMOUNT
                            PromoCodeType.FIXED_AMOUNT -> PromoCodeType.PERCENTAGE
                            null -> PromoCodeType.PERCENTAGE
                        }
                        onAction(SubmissionWizardAction.UpdatePromoCodeType(newType))
                    }
                    FieldAction.OpenServiceSelector -> {
                        // Trigger service search to show available services
                        val searchQuery = if (wizardData.serviceName.isNotBlank()) {
                            wizardData.serviceName
                        } else {
                            "" // Empty search to show popular services
                        }
                        onAction(SubmissionWizardAction.SearchServices(searchQuery))
                        onFocusField("Service")
                    }
                }
            }
            .then(
                if (field.isFilled) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(ShapeTokens.Corner.large),
                    )
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (field.isFilled) {
                backgroundColor
            } else {
                backgroundColor.copy(alpha = 0.7f)
            },
        ),
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (field.isFilled) 4.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = SpacingTokens.md,
                vertical = SpacingTokens.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            if (field.isFilled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = textColor,
                )
            }

            Text(
                text = field.name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (field.isFilled) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GamingProgressIndicator(
    currentStep: SubmissionWizardStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubmissionWizardStep.entries.forEach { step ->
            GamingStepDot(
                step = step,
                isActive = step == currentStep,
                isCompleted = step.stepNumber < currentStep.stepNumber,
            )
        }
    }
}

@Composable
private fun GamingStepDot(
    step: SubmissionWizardStep,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "dot_color",
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "dot_scale",
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .scale(scale)
            .background(
                brush = if (isActive || isCompleted) {
                    Brush.radialGradient(
                        colors = listOf(
                            dotColor,
                            dotColor.copy(alpha = 0.3f),
                        ),
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(dotColor, dotColor),
                    )
                },
                shape = CircleShape,
            ),
    )
}

@Composable
private fun GamingActionChips(
    currentStep: SubmissionWizardStep,
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    onOpenServiceSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = when (currentStep) {
        SubmissionWizardStep.SERVICE_AND_TYPE -> listOf(
            GamingChip("SRV", wizardData.serviceName.isNotBlank(), ChipType.Interactive) { onOpenServiceSelector() },
            GamingChip("TYP", wizardData.promoCodeType != null, ChipType.Interactive) {
                val newType = when (wizardData.promoCodeType) {
                    PromoCodeType.PERCENTAGE -> PromoCodeType.FIXED_AMOUNT
                    PromoCodeType.FIXED_AMOUNT -> PromoCodeType.PERCENTAGE
                    null -> PromoCodeType.PERCENTAGE
                }
                onAction(SubmissionWizardAction.UpdatePromoCodeType(newType))
            },
            GamingChip("NXT", wizardData.promoCodeType != null, ChipType.Continue) {
                onAction(SubmissionWizardAction.GoToNextStep)
            },
        )
        SubmissionWizardStep.TYPE_DETAILS -> listOf(
            GamingChip("COD", wizardData.promoCode.isNotBlank(), ChipType.Display) { },
            GamingChip("PCT", wizardData.discountPercentage.isNotBlank() || wizardData.discountAmount.isNotBlank(), ChipType.Display) { },
            GamingChip(
                "NXT",
                wizardData.promoCode.isNotBlank() && (wizardData.discountPercentage.isNotBlank() || wizardData.discountAmount.isNotBlank()),
                ChipType.Continue,
            ) {
                onAction(SubmissionWizardAction.GoToNextStep)
            },
        )
        SubmissionWizardStep.DATE_SETTINGS -> listOf(
            GamingChip("STA", true, ChipType.Display) { },
            GamingChip("END", wizardData.endDate != null && wizardData.endDate!!.isAfter(wizardData.startDate), ChipType.Display) { },
            GamingChip("NXT", wizardData.endDate != null && wizardData.endDate!!.isAfter(wizardData.startDate), ChipType.Continue) {
                onAction(SubmissionWizardAction.GoToNextStep)
            },
        )
        SubmissionWizardStep.OPTIONAL_DETAILS -> listOf(
            GamingChip("TTL", wizardData.title.isNotBlank(), ChipType.Display) { },
            GamingChip("IMG", wizardData.screenshotUrl != null, ChipType.Display) { },
            GamingChip("SUB", true, ChipType.Submit) {
                onAction(SubmissionWizardAction.SubmitPromoCode)
            },
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { chip ->
            GamingChipButton(
                text = chip.text,
                isActive = chip.isActive,
                chipType = chip.chipType,
                onClick = chip.onClick,
            )
        }
    }
}

@Composable
private fun GamingChipButton(
    text: String,
    isActive: Boolean,
    chipType: ChipType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = when (chipType) {
        ChipType.Interactive -> ChipColors(
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
        ChipType.Display -> ChipColors(
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
        ChipType.Continue -> ChipColors(
            activeColor = MaterialTheme.colorScheme.tertiary,
            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
        ChipType.Submit -> ChipColors(
            activeColor = MaterialTheme.colorScheme.secondary,
            inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) colors.activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "chip_bg",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) colors.activeColor else colors.inactiveColor,
        animationSpec = tween(300),
        label = "chip_border",
    )

    val textColor by animateColorAsState(
        targetValue = if (isActive) colors.activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "chip_text",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 32.dp, height = 24.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
            )
        }
    }
}

// Data classes for gaming chips
private enum class ChipType {
    Interactive, // Cyan - clickable actions (SRV, TYP)
    Display, // Cyan - status display only
    Continue, // Green - next step navigation
    Submit // Yellow - final submission
}

private data class ChipColors(val activeColor: Color, val inactiveColor: Color)

private data class GamingChip(val text: String, val isActive: Boolean, val chipType: ChipType, val onClick: () -> Unit)

// Data classes for interactive field information
private enum class FieldAction {
    FocusField,
    ToggleType,
    OpenServiceSelector
}

private data class InteractiveFieldInfo(val name: String, val isFilled: Boolean, val action: FieldAction)

// MARK: - Previews

@Preview(name = "Bottom Controller - Step 1", showBackground = true)
@Composable
private fun BottomWizardControllerStep1Preview() {
    QodeTheme {
        BottomWizardController(
            uiState = SubmissionWizardUiState.Success(
                currentStep = SubmissionWizardStep.SERVICE_AND_TYPE,
                wizardData = SubmissionWizardData(
                    serviceName = "Netflix",
                    promoCodeType = null,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Bottom Controller - Step 2", showBackground = true)
@Composable
private fun BottomWizardControllerStep2Preview() {
    QodeTheme {
        BottomWizardController(
            uiState = SubmissionWizardUiState.Success(
                currentStep = SubmissionWizardStep.TYPE_DETAILS,
                wizardData = SubmissionWizardData(
                    serviceName = "Netflix",
                    promoCodeType = PromoCodeType.PERCENTAGE,
                    promoCode = "SAVE20",
                    discountPercentage = "20",
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Bottom Controller - Last Step", showBackground = true)
@Composable
private fun BottomWizardControllerLastStepPreview() {
    QodeTheme {
        BottomWizardController(
            uiState = SubmissionWizardUiState.Success(
                currentStep = SubmissionWizardStep.OPTIONAL_DETAILS,
                wizardData = SubmissionWizardData(
                    serviceName = "Netflix",
                    promoCodeType = PromoCodeType.PERCENTAGE,
                    title = "20% off Netflix Premium",
                    description = "Limited time offer",
                ),
            ),
            onAction = {},
        )
    }
}
