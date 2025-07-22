package com.qodein.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeSocialIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Button variants for Qode design system
 */
enum class QodeButtonVariant {
    Primary,
    Secondary,
    Text,
    Outlined
}

/**
 * Button sizes for Qode design system
 */
enum class QodeButtonSize {
    Small,
    Medium,
    Large
}

/**
 * Production-ready button component for Qode design system
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param variant The visual style variant of the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param shape The shape of the button
 * @param contentDescription Accessibility description for the button
 * @param text The text to display in the button
 */
@Composable
fun QodeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QodeButtonVariant = QodeButtonVariant.Primary,
    size: QodeButtonSize = QodeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(QodeCorners.md),
    contentDescription: String? = null,
    text: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.96f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "button_scale",
    )

    // Height based on size
    val height = when (size) {
        QodeButtonSize.Small -> QodeSize.buttonHeightSmall
        QodeButtonSize.Medium -> QodeSize.buttonHeightMedium
        QodeButtonSize.Large -> QodeSize.buttonHeightLarge
    }

    // Padding based on size
    val horizontalPadding = when (size) {
        QodeButtonSize.Small -> QodeSpacing.md
        QodeButtonSize.Medium -> QodeSpacing.lg
        QodeButtonSize.Large -> QodeSpacing.xl
    }

    // Text style based on size
    val textStyle = when (size) {
        QodeButtonSize.Small -> MaterialTheme.typography.labelMedium
        QodeButtonSize.Medium -> MaterialTheme.typography.labelLarge
        QodeButtonSize.Large -> MaterialTheme.typography.titleMedium
    }

    // Icon size based on button size
    val iconSize = when (size) {
        QodeButtonSize.Small -> QodeSize.iconSmall
        QodeButtonSize.Medium -> QodeSize.iconMedium
        QodeButtonSize.Large -> QodeSize.iconMedium
    }

    val buttonModifier = modifier
        .height(height)
        .widthIn(min = QodeSize.minButtonWidth, max = QodeSize.maxButtonWidth)
        .scale(scale)
        .semantics {
            role = Role.Button
            contentDescription?.let { this.contentDescription = it }
        }

    when (variant) {
        QodeButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                interactionSource = interactionSource,
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    textStyle = textStyle,
                    iconSize = iconSize,
                )
            }
        }

        QodeButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                interactionSource = interactionSource,
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    textStyle = textStyle,
                    iconSize = iconSize,
                )
            }
        }

        QodeButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                interactionSource = interactionSource,
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    textStyle = textStyle,
                    iconSize = iconSize,
                )
            }
        }

        QodeButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                border = BorderStroke(
                    width = QodeBorder.thin,
                    color = if (enabled && !loading) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    },
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                interactionSource = interactionSource,
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    loading = loading,
                    textStyle = textStyle,
                    iconSize = iconSize,
                )
            }
        }
    }
}

/**
 * Icon-only button variant
 */
@Composable
fun QodeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    variant: QodeButtonVariant = QodeButtonVariant.Primary,
    size: QodeButtonSize = QodeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.9f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "icon_button_scale",
    )

    val buttonSize = when (size) {
        QodeButtonSize.Small -> QodeSize.iconButtonSmall
        QodeButtonSize.Medium -> QodeSize.iconButtonMedium
        QodeButtonSize.Large -> QodeSize.iconButtonLarge
    }

    val iconSize = when (size) {
        QodeButtonSize.Small -> QodeSize.iconSmall
        QodeButtonSize.Medium -> QodeSize.iconMedium
        QodeButtonSize.Large -> QodeSize.iconLarge
    }

    val containerColor = when (variant) {
        QodeButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        QodeButtonVariant.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        QodeButtonVariant.Text -> Color.Transparent
        QodeButtonVariant.Outlined -> Color.Transparent
    }

    val contentColor = when (variant) {
        QodeButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        QodeButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        QodeButtonVariant.Text -> MaterialTheme.colorScheme.primary
        QodeButtonVariant.Outlined -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale),
    ) {
        if (variant == QodeButtonVariant.Outlined) {
            OutlinedIconButton(
                onClick = onClick,
                enabled = enabled && !loading,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                border = BorderStroke(
                    width = QodeBorder.thin,
                    color = if (enabled && !loading) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    },
                ),
                interactionSource = interactionSource,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(iconSize),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        } else {
            IconButton(
                onClick = onClick,
                enabled = enabled && !loading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = if (variant == QodeButtonVariant.Text) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    },
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
                interactionSource = interactionSource,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(iconSize),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    }
}

/**
 * Text button styles for Qode design system
 */
enum class QodeTextButtonStyle {
    Primary,
    Secondary,
    Tertiary
}

/**
 * Text-only button component
 */
@Composable
fun QodeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: QodeTextButtonStyle = QodeTextButtonStyle.Primary,
    showUnderline: Boolean = false
) {
    val (textColor, textStyle) = when (style) {
        QodeTextButtonStyle.Primary -> Pair(
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                textDecoration = if (showUnderline) TextDecoration.Underline else null,
            ),
        )
        QodeTextButtonStyle.Secondary -> Pair(
            if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (showUnderline) TextDecoration.Underline else null,
            ),
        )
        QodeTextButtonStyle.Tertiary -> Pair(
            if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.typography.bodySmall.copy(
                textDecoration = if (showUnderline) TextDecoration.Underline else null,
            ),
        )
    }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = QodeSpacing.sm,
            vertical = QodeSpacing.xs,
        ),
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor,
        )
    }
}

/**
 * Google Sign-In button component
 */
@Composable
fun QodeGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Continue with Google"
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(QodeCorners.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        ),
        contentPadding = PaddingValues(
            horizontal = QodeSpacing.lg,
            vertical = QodeSpacing.md,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeSocialIcons.Google,
                contentDescription = "Google logo",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// MARK: - Previews

@Preview(name = "Text Buttons", showBackground = true)
@Composable
private fun QodeTextButtonPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        ) {
            QodeTextButton(
                text = "Forgot your password?",
                onClick = {},
                style = QodeTextButtonStyle.Primary,
                showUnderline = true,
            )

            QodeTextButton(
                text = "Sign up",
                onClick = {},
                style = QodeTextButtonStyle.Primary,
            )

            QodeTextButton(
                text = "Secondary Button",
                onClick = {},
                style = QodeTextButtonStyle.Secondary,
            )

            QodeTextButton(
                text = "Terms of Service",
                onClick = {},
                style = QodeTextButtonStyle.Tertiary,
                showUnderline = true,
            )

            QodeTextButton(
                text = "Disabled Button",
                onClick = {},
                enabled = false,
            )
        }
    }
}

@Preview(name = "Google Sign In Button", showBackground = true)
@Composable
private fun QodeGoogleSignInButtonPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            QodeGoogleSignInButton(
                onClick = {},
                text = "Continue with Google",
            )

            QodeGoogleSignInButton(
                onClick = {},
                enabled = false,
                text = "Continue with Google (Disabled)",
            )
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    loading: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    iconSize: androidx.compose.ui.unit.Dp
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(iconSize),
            strokeWidth = 2.dp,
        )
    } else {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.width(QodeSpacing.sm))
        }

        Text(
            text = text,
            style = textStyle,
        )

        trailingIcon?.let {
            Spacer(modifier = Modifier.width(QodeSpacing.sm))
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

// Previews
@Preview(name = "Button Variants", showBackground = true)
@Composable
private fun QodeButtonVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Primary Button",
                variant = QodeButtonVariant.Primary,
            )
            QodeButton(
                onClick = {},
                text = "Secondary Button",
                variant = QodeButtonVariant.Secondary,
            )
            QodeButton(
                onClick = {},
                text = "Text Button",
                variant = QodeButtonVariant.Text,
            )
            QodeButton(
                onClick = {},
                text = "Outlined Button",
                variant = QodeButtonVariant.Outlined,
            )
        }
    }
}

@Preview(name = "Button Sizes", showBackground = true)
@Composable
private fun QodeButtonSizesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Small Button",
                size = QodeButtonSize.Small,
            )
            QodeButton(
                onClick = {},
                text = "Medium Button",
                size = QodeButtonSize.Medium,
            )
            QodeButton(
                onClick = {},
                text = "Large Button",
                size = QodeButtonSize.Large,
            )
        }
    }
}

@Preview(name = "Button States", showBackground = true)
@Composable
private fun QodeButtonStatesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Enabled Button",
            )
            QodeButton(
                onClick = {},
                text = "Disabled Button",
                enabled = false,
            )
            QodeButton(
                onClick = {},
                text = "Loading Button",
                loading = true,
            )
            QodeButton(
                onClick = {},
                text = "With Leading Icon",
                leadingIcon = Icons.Default.Add,
            )
            QodeButton(
                onClick = {},
                text = "With Trailing Icon",
                trailingIcon = Icons.Default.Check,
            )
        }
    }
}

@Preview(name = "Icon Buttons", showBackground = true)
@Composable
private fun QodeIconButtonPreview() {
    QodeTheme {
        Row(
            modifier = Modifier.padding(QodeSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            QodeIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                variant = QodeButtonVariant.Primary,
            )
            QodeIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                variant = QodeButtonVariant.Secondary,
            )
            QodeIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                variant = QodeButtonVariant.Text,
            )
            QodeIconButton(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "Add",
                variant = QodeButtonVariant.Outlined,
            )
        }
    }
}
