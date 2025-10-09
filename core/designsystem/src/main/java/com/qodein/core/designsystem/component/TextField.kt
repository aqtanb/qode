package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import kotlinx.coroutines.launch

/**
 * Qode text field component with submission-style design
 *
 * @param value The current text value
 * @param onValueChange Called when the text changes
 * @param modifier Modifier to be applied to the text field
 * @param placeholder Placeholder text
 * @param helperText Optional helper text below the field
 * @param errorText Optional error text (takes priority over helperText)
 * @param enabled Whether the text field is enabled
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param onTrailingIconClick Called when trailing icon is clicked
 * @param keyboardOptions Software keyboard options
 * @param keyboardActions Software keyboard actions
 * @param focusRequester Optional focus requester
 * @param singleLine Whether the field is single line (default true)
 * @param minLines Minimum number of lines for multiline text field
 * @param maxChars Maximum character count (shows char counter badge if provided)
 */
@Composable
fun QodeinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxChars: Int? = null
) {
    var isFocused by remember { mutableStateOf(false) }
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

    // Animated colors
    val borderColor by animateColorAsState(
        targetValue = when {
            errorText != null -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "borderColor",
    )

    val fieldScale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "fieldScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Main input field
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(fieldScale)
                        .let { if (singleLine) it.height(SizeTokens.Selector.height) else it }
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                        .onFocusChanged { isFocused = it.isFocused },
                    placeholder = placeholder?.let {
                        {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(SizeTokens.Selector.shape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorContainerColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                    ),
                    singleLine = singleLine,
                    minLines = minLines,
                )
            }

            // Supporting content - error has priority over helper
            AnimatedVisibility(
                visible = errorText != null || helperText != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.padding(top = SpacingTokens.sm),
                ) {
                    when {
                        // Error has highest priority
                        errorText != null -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
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
                                    text = errorText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
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
                    }
                }
            }
        }

        // Char counter badge - positioned absolutely at top-right, outside the border
        if (maxChars != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-SpacingTokens.sm), x = (-SpacingTokens.lg))
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(SpacingTokens.lg),
                    )
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs),
            ) {
                Text(
                    text = "${value.length}/$maxChars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Preview(name = "Qode TextField", showBackground = true)
@Composable
private fun QodeTextFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var value by remember { mutableStateOf("") }
            QodeinTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Enter text...",
                helperText = "Helper text",
                leadingIcon = QodeUIIcons.Tag,
                maxChars = 10,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            var errorValue by remember { mutableStateOf("") }
            QodeinTextField(
                value = errorValue,
                onValueChange = { errorValue = it },
                placeholder = "Enter text...",
                errorText = "This field has an error",
                leadingIcon = QodeUIIcons.Tag,
            )
        }
    }
}
