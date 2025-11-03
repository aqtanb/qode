package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import kotlinx.coroutines.launch

enum class SubmissionFieldType {
    TEXT,
    NUMBER,
    PERCENTAGE,
    CURRENCY,
    PROMO_CODE
}

data class SubmissionFieldOption(val value: String, val label: String, val description: String? = null, val icon: ImageVector? = null)

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
    isRequired: Boolean = false,
    enabled: Boolean = true,
    options: List<SubmissionFieldOption> = emptyList(),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester? = null,
    supportingContent: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bring text field into view when focused
    LaunchedEffect(isFocused) {
        if (isFocused) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    // Simple error text handling
    val effectiveErrorText = errorText

    // Simple animation states
    val borderColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "borderColor",
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.error
            isFocused || value.isNotEmpty() -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "labelColor",
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            effectiveErrorText != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
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
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        // Main input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(fieldScale),
        ) {
            when (fieldType) {
                else -> {
                    RegularTextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = placeholder,
                        fieldType = fieldType,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
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
                        bringIntoViewRequester = bringIntoViewRequester,
                    )
                }
            }
        }

        // Supporting content - only show one at a time
        AnimatedVisibility(
            visible = effectiveErrorText != null || helperText != null || supportingContent != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.padding(top = SpacingTokens.sm),
            ) {
                when {
                    // Error has highest priority
                    effectiveErrorText != null -> {
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
                            Spacer(modifier = Modifier.width(SpacingTokens.sm))
                            Text(
                                text = effectiveErrorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    // Helper text has medium priority
                    helperText != null -> {
                        Text(
                            text = helperText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    // Custom content has lowest priority
                    supportingContent != null -> {
                        supportingContent()
                    }
                }
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
    onFocusChanged: (Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester
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
            .bringIntoViewRequester(bringIntoViewRequester)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { onFocusChanged(it.isFocused) },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            errorBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            errorContainerColor = MaterialTheme.colorScheme.surface,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
        ),
        singleLine = true,
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
                helperText = "Choose percentage or fixed amount discount",
                isRequired = true,
            )
        }
    }
}
