package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * TextField variants for Qode design system
 */
enum class QodeTextFieldVariant {
    Standard,
    Search,
    Password,
    Multiline
}

/**
 * TextField validation state
 */
sealed class QodeTextFieldState {
    object Default : QodeTextFieldState()
    object Success : QodeTextFieldState()
    data class Error(val message: String) : QodeTextFieldState()
}

/**
 * Production-ready text field component for Qode design system
 *
 * @param value The current text value
 * @param onValueChange Called when the text changes
 * @param modifier Modifier to be applied to the text field
 * @param variant The variant of the text field
 * @param label The label text
 * @param placeholder The placeholder text
 * @param state The validation state
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 * @param required Whether the field is required (shows asterisk)
 * @param leadingIcon Optional leading icon
 * @param helperText Optional helper text below the field
 * @param characterCounter Whether to show character counter
 * @param maxCharacters Maximum number of characters allowed
 * @param keyboardOptions Software keyboard options
 * @param keyboardActions Software keyboard actions
 * @param shape The shape of the text field
 * @param singleLine Whether the field is single line
 * @param maxLines Maximum number of visible lines
 */
@Composable
fun QodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: QodeTextFieldVariant = QodeTextFieldVariant.Standard,
    label: String? = null,
    placeholder: String? = null,
    state: QodeTextFieldState = QodeTextFieldState.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    required: Boolean = false,
    leadingIcon: ImageVector? = null,
    helperText: String? = null,
    characterCounter: Boolean = false,
    maxCharacters: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = RoundedCornerShape(QodeCorners.sm),
    singleLine: Boolean = variant != QodeTextFieldVariant.Multiline,
    maxLines: Int = if (variant == QodeTextFieldVariant.Multiline) 5 else 1
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Determine colors based on state
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            state is QodeTextFieldState.Error -> MaterialTheme.colorScheme.error
            state is QodeTextFieldState.Success -> Color(0xFF4CAF50) // Success green
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "border_color",
    )

    // Configure based on variant
    val effectiveLeadingIcon = when (variant) {
        QodeTextFieldVariant.Search -> Icons.Default.Search
        else -> leadingIcon
    }

    val effectiveKeyboardOptions = when (variant) {
        QodeTextFieldVariant.Search -> keyboardOptions.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        )
        QodeTextFieldVariant.Password -> keyboardOptions.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )
        QodeTextFieldVariant.Multiline -> keyboardOptions.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
        )
        else -> keyboardOptions
    }

    val visualTransformation = when (variant) {
        QodeTextFieldVariant.Password -> {
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        }
        else -> VisualTransformation.None
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxCharacters == null || newValue.length <= maxCharacters) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            label = label?.let { labelText ->
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (required) {
                            Text(
                                text = " *",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
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
            leadingIcon = effectiveLeadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(QodeSize.iconMedium),
                    )
                }
            },
            trailingIcon = {
                when {
                    variant == QodeTextFieldVariant.Password -> {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.Email // Using Email as a placeholder for visibility
                                },
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                modifier = Modifier.size(QodeSize.iconMedium),
                            )
                        }
                    }
                    state is QodeTextFieldState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(QodeSize.iconMedium),
                        )
                    }
                    state is QodeTextFieldState.Success -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(QodeSize.iconMedium),
                        )
                    }
                    value.isNotEmpty() && enabled && !readOnly && variant != QodeTextFieldVariant.Search -> {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear text",
                                modifier = Modifier.size(QodeSize.iconSmall),
                            )
                        }
                    }
                }
            },
            isError = state is QodeTextFieldState.Error,
            visualTransformation = visualTransformation,
            keyboardOptions = effectiveKeyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                errorBorderColor = borderColor,
                disabledBorderColor = borderColor,
                focusedLabelColor = when (state) {
                    is QodeTextFieldState.Error -> MaterialTheme.colorScheme.error
                    is QodeTextFieldState.Success -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.primary
                },
                unfocusedLabelColor = when (state) {
                    is QodeTextFieldState.Error -> MaterialTheme.colorScheme.error
                    is QodeTextFieldState.Success -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
        )

        // Helper text and character counter row
        if (helperText != null || (characterCounter && maxCharacters != null) || state is QodeTextFieldState.Error) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Helper text or error message
                AnimatedVisibility(
                    visible = helperText != null || state is QodeTextFieldState.Error,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = when (state) {
                            is QodeTextFieldState.Error -> state.message
                            else -> helperText ?: ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (state) {
                            is QodeTextFieldState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Character counter
                if (characterCounter && maxCharacters != null) {
                    Text(
                        text = "${value.length}/$maxCharacters",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            value.length >= maxCharacters -> MaterialTheme.colorScheme.error
                            value.length >= maxCharacters * 0.9 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(start = SpacingTokens.sm),
                    )
                }
            }
        }
    }
}

// Previews
@Preview(name = "TextField Variants", showBackground = true)
@Composable
private fun QodeTextFieldVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var standardValue by remember { mutableStateOf("") }
            QodeTextField(
                value = standardValue,
                onValueChange = { standardValue = it },
                label = "Standard Field",
                placeholder = "Enter text...",
                variant = QodeTextFieldVariant.Standard,
            )

            var searchValue by remember { mutableStateOf("") }
            QodeTextField(
                value = searchValue,
                onValueChange = { searchValue = it },
                label = "Search",
                placeholder = "Search promo codes...",
                variant = QodeTextFieldVariant.Search,
            )

            var passwordValue by remember { mutableStateOf("") }
            QodeTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = "Password",
                placeholder = "Enter password...",
                variant = QodeTextFieldVariant.Password,
            )

            var multilineValue by remember { mutableStateOf("") }
            QodeTextField(
                value = multilineValue,
                onValueChange = { multilineValue = it },
                label = "Description",
                placeholder = "Enter description...",
                variant = QodeTextFieldVariant.Multiline,
                helperText = "Describe the promo code details",
            )
        }
    }
}

@Preview(name = "TextField States", showBackground = true)
@Composable
private fun QodeTextFieldStatesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeTextField(
                value = "Valid input",
                onValueChange = {},
                label = "Success State",
                state = QodeTextFieldState.Success,
                helperText = "Perfect!",
            )

            QodeTextField(
                value = "Invalid input",
                onValueChange = {},
                label = "Error State",
                state = QodeTextFieldState.Error("Please enter a valid promo code"),
                required = true,
            )

            QodeTextField(
                value = "Disabled field",
                onValueChange = {},
                label = "Disabled",
                enabled = false,
            )

            QodeTextField(
                value = "Read only content",
                onValueChange = {},
                label = "Read Only",
                readOnly = true,
                leadingIcon = Icons.Default.Info,
            )
        }
    }
}

@Preview(name = "TextField Features", showBackground = true)
@Composable
private fun QodeTextFieldFeaturesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            var counterValue by remember { mutableStateOf("") }
            QodeTextField(
                value = counterValue,
                onValueChange = { counterValue = it },
                label = "Promo Code",
                placeholder = "Enter code...",
                helperText = "Enter the promo code exactly as shown",
                characterCounter = true,
                maxCharacters = 20,
            )

            var requiredValue by remember { mutableStateOf("") }
            QodeTextField(
                value = requiredValue,
                onValueChange = { requiredValue = it },
                label = "Store Name",
                required = true,
                helperText = "This field is required",
            )
        }
    }
}
