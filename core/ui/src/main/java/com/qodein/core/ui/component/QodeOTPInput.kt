package com.qodein.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * OTP input state
 */
sealed class QodeOTPState {
    object Default : QodeOTPState()
    object Success : QodeOTPState()
    data class Error(val message: String) : QodeOTPState()
}

/**
 * OTP input component with individual digit boxes
 */
@Composable
fun QodeOTPInput(
    otp: String,
    onOTPChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = 6,
    state: QodeOTPState = QodeOTPState.Default,
    enabled: Boolean = true,
    onOTPComplete: ((String) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequesters = remember { List(digitCount) { FocusRequester() } }

    // Handle OTP completion
    LaunchedEffect(otp) {
        if (otp.length == digitCount) {
            onOTPComplete?.invoke(otp)
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(digitCount) { index ->
                OTPDigitBox(
                    digit = otp.getOrNull(index)?.toString() ?: "",
                    isFocused = index == otp.length,
                    state = state,
                    enabled = enabled,
                    focusRequester = focusRequesters[index],
                    onDigitChange = { digit ->
                        val newOTP = buildString {
                            repeat(digitCount) { i ->
                                when {
                                    i < otp.length && i != index -> append(otp[i])
                                    i == index -> append(digit)
                                }
                            }
                        }
                        onOTPChange(newOTP.take(digitCount))

                        // Move focus to next box
                        if (digit.isNotEmpty() && index < digitCount - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                    onBackspace = {
                        if (index > 0) {
                            val newOTP = otp.take(index - 1)
                            onOTPChange(newOTP)
                            focusRequesters[index - 1].requestFocus()
                        }
                    },
                )
            }
        }

        // Error message
        if (state is QodeOTPState.Error) {
            Spacer(modifier = Modifier.height(QodeSpacing.sm))
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }

    // Auto-focus first box
    LaunchedEffect(Unit) {
        if (enabled) {
            focusRequesters[0].requestFocus()
        }
    }
}

/**
 * Individual OTP digit input box
 */
@Composable
private fun OTPDigitBox(
    digit: String,
    isFocused: Boolean,
    state: QodeOTPState,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onDigitChange: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(digit) {
        mutableStateOf(TextFieldValue(digit, TextRange(digit.length)))
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            state is QodeOTPState.Error -> MaterialTheme.colorScheme.error
            state is QodeOTPState.Success -> Color(0xFF4CAF50)
            isFocused -> MaterialTheme.colorScheme.primary
            digit.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "border_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            state is QodeOTPState.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            state is QodeOTPState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            digit.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "background_color",
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(QodeCorners.md))
            .background(backgroundColor)
            .border(
                width = if (isFocused) QodeBorder.medium else QodeBorder.thin,
                color = borderColor,
                shape = RoundedCornerShape(QodeCorners.md),
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val newDigit = newValue.text.filter { it.isDigit() }.take(1)

                when {
                    // New digit entered
                    newDigit.isNotEmpty() && newDigit != digit -> {
                        textFieldValue = TextFieldValue(newDigit, TextRange(1))
                        onDigitChange(newDigit)
                    }
                    // Backspace pressed
                    newValue.text.isEmpty() && digit.isNotEmpty() -> {
                        textFieldValue = TextFieldValue("", TextRange(0))
                        onDigitChange("")
                    }
                    // Backspace on empty field - move to previous
                    newValue.text.isEmpty() && digit.isEmpty() -> {
                        onBackspace()
                    }
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(56.dp),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { /* Focus management is handled in onDigitChange */ },
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    state is QodeOTPState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
            ),
        ) { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp),
            ) {
                innerTextField()
            }
        }
    }
}

// Previews
@Preview(name = "OTP Input States", showBackground = true)
@Composable
private fun QodeOTPInputPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.xl),
        ) {
            // Empty state
            Text("Empty", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "",
                onOTPChange = {},
            )

            // Partially filled
            Text("Partially Filled", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "123",
                onOTPChange = {},
            )

            // Completed
            Text("Completed", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "123456",
                onOTPChange = {},
                state = QodeOTPState.Success,
            )

            // Error state
            Text("Error", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "123456",
                onOTPChange = {},
                state = QodeOTPState.Error("Invalid verification code"),
            )

            // Disabled
            Text("Disabled", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "123",
                onOTPChange = {},
                enabled = false,
            )
        }
    }
}

@Preview(name = "Different Digit Counts", showBackground = true)
@Composable
private fun QodeOTPInputDigitCountPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            Text("4 Digits", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "12",
                onOTPChange = {},
                digitCount = 4,
            )

            Text("5 Digits", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "123",
                onOTPChange = {},
                digitCount = 5,
            )

            Text("6 Digits (Default)", style = MaterialTheme.typography.titleMedium)
            QodeOTPInput(
                otp = "1234",
                onOTPChange = {},
                digitCount = 6,
            )
        }
    }
}
