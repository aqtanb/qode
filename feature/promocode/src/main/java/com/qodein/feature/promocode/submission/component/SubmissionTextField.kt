package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import kotlinx.coroutines.delay

enum class SubmissionFieldType {
    TEXT,
    NUMBER,
    PERCENTAGE,
    CURRENCY,
    DROPDOWN,
    DATE,
    PROMO_CODE
}

enum class ValidationState {
    IDLE,
    VALIDATING,
    VALID,
    WARNING,
    ERROR
}

enum class ValidationTiming {
    ON_CHANGE, // Validate immediately as user types
    ON_FOCUS_LOST, // Validate when field loses focus
    DEBOUNCED, // Validate after user stops typing for a moment
    MANUAL // Only validate when explicitly triggered
}

data class SubmissionFieldOption(val value: String, val label: String, val description: String? = null, val icon: ImageVector? = null)

data class ValidationRule(val validator: (String) -> ValidationResult, val timing: ValidationTiming = ValidationTiming.DEBOUNCED)

data class ValidationResult(
    val isValid: Boolean,
    val state: ValidationState = if (isValid) ValidationState.VALID else ValidationState.ERROR,
    val message: String? = null,
    val suggestions: List<String> = emptyList(),
    val strength: Float = if (isValid) 1.0f else 0.0f // 0.0 to 1.0 for strength indicators
)

data class FieldStrength(
    val score: Float, // 0.0 to 1.0
    val label: String, // "Weak", "Fair", "Good", "Strong"
    val color: androidx.compose.ui.graphics.Color,
    val suggestions: List<String> = emptyList()
)

@Composable
fun SubmissionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    fieldType: SubmissionFieldType = SubmissionFieldType.TEXT,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    validationState: ValidationState = ValidationState.IDLE,
    validationRules: List<ValidationRule> = emptyList(),
    showStrengthIndicator: Boolean = false,
    showValidationSuggestions: Boolean = true,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    options: List<SubmissionFieldOption> = emptyList(),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    onDateClick: (() -> Unit)? = null,
    onDropdownClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    onValidationResult: ((ValidationResult) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Enhanced validation state
    var currentValidationResult by remember { mutableStateOf<ValidationResult?>(null) }
    var currentFieldStrength by remember { mutableStateOf<FieldStrength?>(null) }
    var isValidating by remember { mutableStateOf(false) }

    // Capture theme colors for non-Composable contexts
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Real-time validation with debouncing
    LaunchedEffect(value, validationRules) {
        if (validationRules.isNotEmpty() && value.isNotEmpty()) {
            val debouncedRules = validationRules.filter { it.timing == ValidationTiming.DEBOUNCED }
            val immediateRules = validationRules.filter { it.timing == ValidationTiming.ON_CHANGE }

            // Execute immediate validation rules
            if (immediateRules.isNotEmpty()) {
                val results = immediateRules.map { it.validator(value) }
                val combinedResult = combineValidationResults(results)
                currentValidationResult = combinedResult
                onValidationResult?.invoke(combinedResult)
            }

            // Execute debounced validation rules
            if (debouncedRules.isNotEmpty()) {
                isValidating = true
                delay(300) // Debounce delay

                val results = debouncedRules.map { it.validator(value) }
                val combinedResult = combineValidationResults(results)
                currentValidationResult = combinedResult
                onValidationResult?.invoke(combinedResult)
                isValidating = false
            }

            // Calculate field strength for supported field types
            if (showStrengthIndicator) {
                currentFieldStrength = calculateFieldStrength(
                    value = value,
                    fieldType = fieldType,
                    primaryColor = primaryColor,
                    errorColor = errorColor,
                    tertiaryColor = tertiaryColor,
                    onSurfaceVariantColor = onSurfaceVariantColor,
                )
            }
        } else {
            currentValidationResult = null
            currentFieldStrength = null
            isValidating = false
        }
    }

    // Focus-lost validation
    LaunchedEffect(isFocused) {
        if (!isFocused && value.isNotEmpty()) {
            val focusLostRules = validationRules.filter { it.timing == ValidationTiming.ON_FOCUS_LOST }
            if (focusLostRules.isNotEmpty()) {
                val results = focusLostRules.map { it.validator(value) }
                val combinedResult = combineValidationResults(results)
                currentValidationResult = combinedResult
                onValidationResult?.invoke(combinedResult)
            }
        }
    }

    // Determine effective validation state
    val effectiveValidationState = when {
        isValidating -> ValidationState.VALIDATING
        currentValidationResult != null -> currentValidationResult!!.state
        validationState != ValidationState.IDLE -> validationState
        else -> ValidationState.IDLE
    }

    // Determine effective error text
    val effectiveErrorText = currentValidationResult?.message?.takeIf {
        currentValidationResult!!.state == ValidationState.ERROR
    } ?: errorText

    // Enhanced animation states
    val borderColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.error
            effectiveValidationState == ValidationState.VALID -> MaterialTheme.colorScheme.primary
            effectiveValidationState == ValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "borderColor",
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.error
            effectiveValidationState == ValidationState.VALID -> MaterialTheme.colorScheme.primary
            effectiveValidationState == ValidationState.WARNING -> MaterialTheme.colorScheme.tertiary
            isFocused || value.isNotEmpty() -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "labelColor",
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            effectiveValidationState == ValidationState.VALID -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            effectiveValidationState == ValidationState.WARNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            isFocused -> MaterialTheme.colorScheme.surfaceVariant
            value.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "containerColor",
    )

    val fieldScale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "fieldScale",
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (isDropdownExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "chevronRotation",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Label with required indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = SpacingTokens.xs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = labelColor,
                fontWeight = FontWeight.Medium,
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Main input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(fieldScale),
        ) {
            when (fieldType) {
                SubmissionFieldType.DROPDOWN -> {
                    DropdownField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = placeholder,
                        options = options,
                        isExpanded = isDropdownExpanded,
                        onExpandedChange = { shouldExpand ->
                            isDropdownExpanded = shouldExpand
                            if (shouldExpand) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        borderColor = borderColor,
                        containerColor = containerColor,
                        chevronRotation = chevronRotation,
                        enabled = enabled,
                        onFocusChanged = { isFocused = it },
                        onExternalClick = onDropdownClick,
                    )
                }

                SubmissionFieldType.DATE -> {
                    DateField(
                        value = value,
                        placeholder = placeholder,
                        onClick = onDateClick ?: {},
                        borderColor = borderColor,
                        containerColor = containerColor,
                        enabled = enabled,
                        onFocusChanged = { isFocused = it },
                    )
                }

                else -> {
                    RegularTextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = placeholder,
                        fieldType = fieldType,
                        leadingIcon = leadingIcon,
                        trailingIcon = getTrailingIcon(fieldType, trailingIcon, effectiveValidationState),
                        onTrailingIconClick = onTrailingIconClick,
                        borderColor = borderColor,
                        containerColor = containerColor,
                        enabled = enabled,
                        keyboardOptions = getKeyboardOptions(fieldType, keyboardOptions),
                        keyboardActions = keyboardActions,
                        visualTransformation = getVisualTransformation(fieldType, visualTransformation),
                        focusRequester = focusRequester,
                        interactionSource = interactionSource,
                        onFocusChanged = { isFocused = it },
                    )
                }
            }
        }

        // Enhanced supporting content with validation feedback
        AnimatedVisibility(
            visible = effectiveErrorText != null ||
                helperText != null ||
                supportingContent != null ||
                currentFieldStrength != null ||
                (currentValidationResult?.suggestions?.isNotEmpty() == true && showValidationSuggestions),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.padding(top = SpacingTokens.xs),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                // Field strength indicator
                currentFieldStrength?.let { strength ->
                    AnimatedVisibility(
                        visible = showStrengthIndicator && value.isNotEmpty(),
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut(),
                    ) {
                        FieldStrengthIndicator(strength = strength)
                    }
                }

                // Error text
                effectiveErrorText?.let { error ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = QodeUIIcons.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                            )
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Warning text
                if (effectiveErrorText == null && currentValidationResult?.state == ValidationState.WARNING) {
                    currentValidationResult?.message?.let { warning ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = QodeUIIcons.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                                )
                                Spacer(modifier = Modifier.width(SpacingTokens.xs))
                                Text(
                                    text = warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // Validation suggestions
                if (showValidationSuggestions &&
                    currentValidationResult?.suggestions?.isNotEmpty() == true &&
                    effectiveErrorText == null
                ) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut(),
                    ) {
                        ValidationSuggestions(
                            suggestions = currentValidationResult!!.suggestions,
                            onSuggestionClick = { suggestion ->
                                onValueChange(suggestion)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                    }
                }

                // Helper text (only show if no error or warning)
                if (effectiveErrorText == null &&
                    currentValidationResult?.state != ValidationState.WARNING &&
                    helperText != null
                ) {
                    Text(
                        text = helperText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Custom supporting content
                supportingContent?.invoke()
            }
        }
    }
}

@Composable
private fun RegularTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    fieldType: SubmissionFieldType,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    onTrailingIconClick: (() -> Unit)?,
    borderColor: Color,
    containerColor: Color,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation,
    focusRequester: FocusRequester?,
    interactionSource: MutableInteractionSource,
    onFocusChanged: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val formattedValue = formatInput(newValue, fieldType)
            onValueChange(formattedValue)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(SizeTokens.Selector.height)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { onFocusChanged(it.isFocused) },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                IconButton(
                    onClick = { onTrailingIconClick?.invoke() },
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    )
                }
            }
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(SizeTokens.Selector.shape),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = if (value.isNotEmpty()) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            },
            focusedContainerColor = if (value.isNotEmpty()) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            unfocusedContainerColor = if (value.isNotEmpty()) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            errorBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            errorContainerColor = MaterialTheme.colorScheme.surface,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
        ),
    )
}

@Composable
private fun DropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    options: List<SubmissionFieldOption>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    borderColor: Color,
    containerColor: Color,
    chevronRotation: Float,
    enabled: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onExternalClick: (() -> Unit)? = null
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    if (onExternalClick != null) {
                        onExternalClick()
                        onFocusChanged(true)
                    } else {
                        onExpandedChange(!isExpanded)
                        onFocusChanged(true)
                    }
                },
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = QodeActionIcons.Down,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(SizeTokens.Icon.sizeMedium)
                        .rotate(chevronRotation),
                )
            },
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(ShapeTokens.Corner.large),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor.copy(alpha = 0.7f),
                focusedContainerColor = if (value.isNotEmpty()) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                unfocusedContainerColor = if (value.isNotEmpty()) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                onExpandedChange(false)
                onFocusChanged(false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(ShapeTokens.Corner.large),
                ),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(option.value)
                        onExpandedChange(false)
                        onFocusChanged(false)
                    },
                    text = {
                        Column {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            option.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    leadingIcon = option.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DateField(
    value: String,
    placeholder: String?,
    onClick: () -> Unit,
    borderColor: Color,
    containerColor: Color,
    enabled: Boolean,
    onFocusChanged: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                onClick()
                onFocusChanged(true)
            },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = QodeUIIcons.Datepicker,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )
        },
        readOnly = true,
        enabled = enabled,
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor.copy(alpha = 0.7f),
            focusedContainerColor = if (value.isNotEmpty()) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            unfocusedContainerColor = if (value.isNotEmpty()) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
        ),
    )
}

private fun getKeyboardOptions(
    fieldType: SubmissionFieldType,
    default: KeyboardOptions
): KeyboardOptions =
    when (fieldType) {
        SubmissionFieldType.NUMBER, SubmissionFieldType.PERCENTAGE, SubmissionFieldType.CURRENCY ->
            default.copy(keyboardType = KeyboardType.Number)
        else -> default
    }

private fun getVisualTransformation(
    fieldType: SubmissionFieldType,
    default: VisualTransformation
): VisualTransformation =
    when (fieldType) {
        // Add currency formatting or percentage formatting if needed
        else -> default
    }

private fun getTrailingIcon(
    fieldType: SubmissionFieldType,
    trailingIcon: ImageVector?,
    validationState: ValidationState
): ImageVector? =
    when (validationState) {
        ValidationState.VALIDATING -> QodeUIIcons.Loading
        ValidationState.VALID -> QodeActionIcons.Check
        ValidationState.WARNING -> QodeUIIcons.Info
        ValidationState.ERROR -> QodeUIIcons.Error
        else -> trailingIcon
    }

private fun formatInput(
    input: String,
    fieldType: SubmissionFieldType
): String =
    when (fieldType) {
        SubmissionFieldType.PERCENTAGE -> {
            val digits = input.filter { it.isDigit() }
            if (digits.length <= 3) digits else digits.take(3)
        }
        SubmissionFieldType.CURRENCY -> {
            input.filter { it.isDigit() || it == '.' }
        }
        SubmissionFieldType.NUMBER -> {
            input.filter { it.isDigit() }
        }
        else -> input
    }

// Enhanced validation utility functions
private fun combineValidationResults(results: List<ValidationResult>): ValidationResult {
    if (results.isEmpty()) {
        return ValidationResult(isValid = true, state = ValidationState.IDLE)
    }

    // Find the highest priority state (ERROR > WARNING > VALID > IDLE)
    val prioritizedResult = results.sortedWith { a, b ->
        val priorityA = when (a.state) {
            ValidationState.ERROR -> 0
            ValidationState.WARNING -> 1
            ValidationState.VALID -> 2
            else -> 3
        }
        val priorityB = when (b.state) {
            ValidationState.ERROR -> 0
            ValidationState.WARNING -> 1
            ValidationState.VALID -> 2
            else -> 3
        }
        priorityA.compareTo(priorityB)
    }.first()

    // Combine suggestions from all results
    val allSuggestions = results.flatMap { it.suggestions }.distinct()

    // Calculate combined strength (average of all strengths)
    val avgStrength = results.map { it.strength }.average().toFloat()

    return ValidationResult(
        isValid = prioritizedResult.isValid,
        state = prioritizedResult.state,
        message = prioritizedResult.message,
        suggestions = allSuggestions,
        strength = avgStrength,
    )
}

private fun calculateFieldStrength(
    value: String,
    fieldType: SubmissionFieldType,
    primaryColor: Color,
    errorColor: Color,
    tertiaryColor: Color,
    onSurfaceVariantColor: Color
): FieldStrength =
    when (fieldType) {
        SubmissionFieldType.TEXT -> calculateTextStrength(value, primaryColor, errorColor, tertiaryColor)
        SubmissionFieldType.PROMO_CODE -> calculatePromoCodeStrength(value, primaryColor, errorColor, tertiaryColor)
        else -> FieldStrength(
            score = if (value.isNotEmpty()) 1.0f else 0.0f,
            label = if (value.isNotEmpty()) "Complete" else "Empty",
            color = if (value.isNotEmpty()) primaryColor else onSurfaceVariantColor,
        )
    }

private fun calculateTextStrength(
    value: String,
    primaryColor: Color,
    errorColor: Color,
    tertiaryColor: Color
): FieldStrength {
    val score = when {
        value.isEmpty() -> 0.0f
        value.length < 3 -> 0.3f
        value.length < 6 -> 0.6f
        value.length < 10 -> 0.8f
        else -> 1.0f
    }

    val label = when {
        score == 0.0f -> "Empty"
        score <= 0.3f -> "Too Short"
        score <= 0.6f -> "Fair"
        score <= 0.8f -> "Good"
        else -> "Excellent"
    }

    val color = when {
        score <= 0.3f -> errorColor
        score <= 0.6f -> tertiaryColor
        else -> primaryColor
    }

    return FieldStrength(score = score, label = label, color = color)
}

private fun calculatePromoCodeStrength(
    value: String,
    primaryColor: Color,
    errorColor: Color,
    tertiaryColor: Color
): FieldStrength {
    var score = 0.0f
    val suggestions = mutableListOf<String>()

    // Length check
    when {
        value.length >= 6 -> score += 0.4f
        value.length >= 3 -> {
            score += 0.2f
            suggestions.add("Consider making it longer (6+ characters)")
        }
        else -> suggestions.add("Too short - use at least 3 characters")
    }

    // Character variety
    val hasNumbers = value.any { it.isDigit() }
    val hasLetters = value.any { it.isLetter() }

    when {
        hasNumbers && hasLetters -> score += 0.3f
        hasNumbers || hasLetters -> {
            score += 0.1f
            suggestions.add("Mix letters and numbers for better uniqueness")
        }
        else -> suggestions.add("Use letters and numbers")
    }

    // Readability (avoid confusing characters)
    val hasConfusingChars = value.any { it in "0O1Il" }
    if (!hasConfusingChars) {
        score += 0.2f
    } else {
        suggestions.add("Avoid confusing characters like 0, O, 1, I, l")
    }

    // Memorability (not too random)
    val isAllRandom = value.all { !it.isLetter() || it.isUpperCase() }
    if (!isAllRandom) {
        score += 0.1f
    } else {
        suggestions.add("Consider making it more memorable")
    }

    val label = when {
        score <= 0.3f -> "Weak"
        score <= 0.6f -> "Fair"
        score <= 0.8f -> "Good"
        else -> "Strong"
    }

    val color = when {
        score <= 0.3f -> errorColor
        score <= 0.6f -> tertiaryColor
        else -> primaryColor
    }

    return FieldStrength(
        score = score,
        label = label,
        color = color,
        suggestions = suggestions,
    )
}

@Composable
private fun FieldStrengthIndicator(
    strength: FieldStrength,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = strength.score,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "strengthScore",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Strength: ${strength.label}",
                style = MaterialTheme.typography.labelSmall,
                color = strength.color,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = "${(animatedScore * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = strength.color,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Strength progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(2.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedScore)
                    .height(4.dp)
                    .background(
                        strength.color,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

@Composable
private fun ValidationSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Text(
                text = "Suggestions:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )

            suggestions.take(3).forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(ShapeTokens.Corner.small),
                        )
                        .padding(
                            horizontal = SpacingTokens.sm,
                            vertical = SpacingTokens.xs,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = QodeUIIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    )

                    Spacer(modifier = Modifier.width(SpacingTokens.xs))

                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )

                    Icon(
                        imageVector = QodeActionIcons.Check,
                        contentDescription = "Apply suggestion",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    )
                }
            }
        }
    }
}

@Preview(name = "Submission Text Field - Text", showBackground = true)
@Composable
private fun SubmissionTextFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            SubmissionTextField(
                value = "",
                onValueChange = {},
                label = "Service Name",
                placeholder = "Enter service name",
                helperText = "Choose the service for your promo code",
                isRequired = true,
            )
        }
    }
}

@Preview(name = "Submission Text Field - Dropdown", showBackground = true)
@Composable
private fun SubmissionDropdownFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            SubmissionTextField(
                value = "",
                onValueChange = {},
                label = "Discount Type",
                placeholder = "Select discount type",
                fieldType = SubmissionFieldType.DROPDOWN,
                options = listOf(
                    SubmissionFieldOption("percentage", "Percentage", "Discount as percentage"),
                    SubmissionFieldOption("fixed", "Fixed Amount", "Fixed amount discount"),
                ),
                isRequired = true,
            )
        }
    }
}
