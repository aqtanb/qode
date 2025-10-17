package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.QodeActionIcons
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
 * @param keyboardOptions Software keyboard options
 * @param keyboardActions Software keyboard actions
 * @param focusRequester Optional focus requester
 * @param singleLine Whether the field is single line (default true)
 * @param minLines Minimum number of lines for multiline text field
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    // Auto-scroll to bring text field into view when focused
    LaunchedEffect(isFocused) {
        if (isFocused) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    val fieldScale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "fieldScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth(),
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
                    isError = errorText != null,
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
                    trailingIcon = if (value.isNotEmpty()) {
                        {
                            QodeinIconButton(
                                onClick = {
                                    onValueChange("")
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                icon = QodeActionIcons.Clear,
                                contentDescription = "Clear text",
                                size = ButtonSize.Small,
                                modifier = Modifier.padding(end = SpacingTokens.md),
                                outlined = true,
                            )
                        }
                    } else {
                        null
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
                            Text(
                                text = errorText,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.lg),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
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
    }
}

/**
 * Qode basic text field component for inline editing without borders or decorations
 *
 * Perfect for post titles, descriptions, and content where the text should blend naturally
 * into the layout without visual boundaries.
 *
 * @param value The current text value
 * @param onValueChange Called when the text changes
 * @param modifier Modifier to be applied to the text field
 * @param placeholder Optional placeholder text shown when empty
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is in read-only mode
 * @param textStyle Text style to apply (default: bodyMedium)
 * @param keyboardOptions Software keyboard options
 * @param keyboardActions Software keyboard actions
 * @param singleLine Whether the field is single line
 * @param minLines Minimum number of lines for multiline text field
 * @param maxLines Maximum number of lines for multiline text field
 */
@Composable
fun QodeinBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    focusRequester: FocusRequester = FocusRequester.Default
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value, isFocused) {
        if (isFocused) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier
            .padding(vertical = SpacingTokens.xs)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusRequester(focusRequester),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = textStyle.copy(
                        color = textStyle.color.copy(alpha = 0.5f),
                    ),
                )
            }
            innerTextField()
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

@PreviewLightDark
@Composable
private fun QodeTextFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var value by remember { mutableStateOf("aaa") }
            QodeinTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Enter text...",
                helperText = "Helper text",
                leadingIcon = QodeUIIcons.Tag,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            var errorValue by remember { mutableStateOf("") }
            QodeinTextField(
                value = errorValue,
                onValueChange = { errorValue = it },
                placeholder = "Enter text...",
                errorText = "This field has a really looooooooooooooooooooong error message\nType here...",
                leadingIcon = QodeUIIcons.Tag,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeBasicTextFieldPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var singleLineValue by remember { mutableStateOf("AAA") }
            QodeinBasicTextField(
                value = singleLineValue,
                onValueChange = { singleLineValue = it },
                placeholder = "Single line placeholder",
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            var multiLineValue by remember { mutableStateOf("") }
            QodeinBasicTextField(
                value = multiLineValue,
                onValueChange = { multiLineValue = it },
                placeholder = "Multi-line placeholder\nType here...",
                singleLine = false,
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            var titleValue by remember { mutableStateOf("") }
            QodeinBasicTextField(
                value = titleValue,
                onValueChange = { titleValue = it },
                placeholder = "Title",
                singleLine = true,
            )
        }
    }
}
