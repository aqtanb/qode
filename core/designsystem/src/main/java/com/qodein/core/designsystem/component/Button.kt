package com.qodein.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Button variants for Qode design system
 */
enum class QodeButtonVariant {
    Primary,
    Secondary,
    Text,
    Outlined,
    Error
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
 * Production-ready button component using modern design tokens
 *
 * @param onClick Called when the button is clicked
 * @param text The text to display in the button
 * @param modifier Modifier to be applied to the button
 * @param variant The visual style variant of the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param shape The shape of the button
 * @param contentDescription Accessibility description for the button
 */
@Composable
fun QodeButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: QodeButtonVariant = QodeButtonVariant.Primary,
    size: QodeButtonSize = QodeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.medium),
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Modern animation using design tokens
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast,
        label = "button_scale",
    )

    // Consistent sizing using modern tokens
    val buttonHeight = when (size) {
        QodeButtonSize.Small -> SizeTokens.Button.heightSmall
        QodeButtonSize.Medium -> SizeTokens.Button.heightMedium
        QodeButtonSize.Large -> SizeTokens.Button.heightLarge
    }

    val horizontalPadding = when (size) {
        QodeButtonSize.Small -> SpacingTokens.Button.horizontalPaddingSmall
        QodeButtonSize.Medium -> SpacingTokens.Button.horizontalPadding
        QodeButtonSize.Large -> SpacingTokens.Button.horizontalPaddingLarge
    }

    val textStyle = when (size) {
        QodeButtonSize.Small -> MaterialTheme.typography.labelMedium
        QodeButtonSize.Medium -> MaterialTheme.typography.labelLarge
        QodeButtonSize.Large -> MaterialTheme.typography.titleMedium
    }

    val iconSize = when (size) {
        QodeButtonSize.Small -> SizeTokens.Icon.sizeSmall
        QodeButtonSize.Medium -> SizeTokens.Icon.sizeLarge
        QodeButtonSize.Large -> SizeTokens.Icon.sizeLarge
    }

    val buttonModifier = modifier
        .height(buttonHeight)
        .widthIn(min = SizeTokens.Button.widthMin, max = SizeTokens.Button.widthMax)
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
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
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
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
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
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
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
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
                ),
                border = BorderStroke(
                    width = ShapeTokens.Border.thin,
                    color = if (enabled && !loading) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER)
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

        QodeButtonVariant.Error -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
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
 * Icon-only button variant using modern design tokens
 */
@Composable
fun QodeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    variant: QodeButtonVariant = QodeButtonVariant.Primary,
    size: QodeButtonSize = QodeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.ICON_PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast,
        label = "icon_button_scale",
    )

    // Consistent sizing using modern tokens
    val buttonSize = when (size) {
        QodeButtonSize.Small -> SizeTokens.IconButton.sizeSmall
        QodeButtonSize.Medium -> SizeTokens.IconButton.sizeMedium
        QodeButtonSize.Large -> SizeTokens.IconButton.sizeLarge
    }

    val iconSize = when (size) {
        QodeButtonSize.Small -> SizeTokens.Icon.sizeSmall
        QodeButtonSize.Medium -> SizeTokens.Icon.sizeLarge
        QodeButtonSize.Large -> SizeTokens.Icon.sizeXLarge
    }

    val containerColor = when (variant) {
        QodeButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        QodeButtonVariant.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        QodeButtonVariant.Text -> Color.Transparent
        QodeButtonVariant.Outlined -> Color.Transparent
        QodeButtonVariant.Error -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (variant) {
        QodeButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        QodeButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        QodeButtonVariant.Text -> MaterialTheme.colorScheme.primary
        QodeButtonVariant.Outlined -> MaterialTheme.colorScheme.primary
        QodeButtonVariant.Error -> MaterialTheme.colorScheme.onErrorContainer
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
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
                ),
                border = BorderStroke(
                    width = ShapeTokens.Border.thin,
                    color = if (enabled && !loading) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER)
                    },
                ),
                interactionSource = interactionSource,
            ) {
                IconButtonContent(
                    icon = icon,
                    contentDescription = contentDescription,
                    loading = loading,
                    iconSize = iconSize,
                    contentColor = contentColor,
                )
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
                        MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER)
                    },
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
                ),
                interactionSource = interactionSource,
            ) {
                IconButtonContent(
                    icon = icon,
                    contentDescription = contentDescription,
                    loading = loading,
                    iconSize = iconSize,
                    contentColor = contentColor,
                )
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
 * Text-only button component using modern design tokens
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
            if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
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
            horizontal = SpacingTokens.xs,
            vertical = SpacingTokens.xs,
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
 * Button content with consistent icon-text layout
 */
@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    loading: Boolean,
    textStyle: TextStyle,
    iconSize: Dp
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(iconSize),
            strokeWidth = ShapeTokens.Border.thin,
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (leadingIcon != null || trailingIcon != null) {
                Arrangement.Start
            } else {
                Arrangement.Center
            },
        ) {
            // Leading icon
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(modifier = Modifier.width(SpacingTokens.Button.iconSpacing))
            }

            // Text
            Text(
                text = text,
                style = textStyle,
                modifier = if (leadingIcon != null || trailingIcon != null) {
                    Modifier.weight(1f)
                } else {
                    Modifier
                },
                textAlign = TextAlign.Center,
            )

            // Trailing icon
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(SpacingTokens.Button.iconSpacing))
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

/**
 * Icon button content with loading state
 */
@Composable
private fun IconButtonContent(
    icon: ImageVector,
    contentDescription: String,
    loading: Boolean,
    iconSize: Dp,
    contentColor: Color
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(iconSize),
            strokeWidth = ShapeTokens.Border.thin,
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

// MARK: - Previews using modern tokens

@Preview(name = "Button Variants", showBackground = true)
@Composable
private fun QodeButtonVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Primary Button", // TODO: Use stringResource(R.string.button_primary)
                variant = QodeButtonVariant.Primary,
            )
            QodeButton(
                onClick = {},
                text = "Secondary Button", // TODO: Use stringResource(R.string.button_secondary)
                variant = QodeButtonVariant.Secondary,
            )
            QodeButton(
                onClick = {},
                text = "Text Button", // TODO: Use stringResource(R.string.button_text)
                variant = QodeButtonVariant.Text,
            )
            QodeButton(
                onClick = {},
                text = "Outlined Button", // TODO: Use stringResource(R.string.button_outlined)
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
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Small Button", // TODO: Use stringResource(R.string.button_small)
                size = QodeButtonSize.Small,
            )
            QodeButton(
                onClick = {},
                text = "Medium Button", // TODO: Use stringResource(R.string.button_medium)
                size = QodeButtonSize.Medium,
            )
            QodeButton(
                onClick = {},
                text = "Large Button", // TODO: Use stringResource(R.string.button_large)
                size = QodeButtonSize.Large,
            )
        }
    }
}

@Preview(name = "Modern Token Showcase", showBackground = true)
@Composable
private fun ModernTokenShowcasePreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Text(
                text = "Modern Design Tokens in Action",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = SpacingTokens.sm),
            )

            QodeButton(
                onClick = {},
                text = "Fast Animation",
                leadingIcon = Icons.Default.Add,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Favorite,
                    contentDescription = "Like",
                    size = QodeButtonSize.Small,
                )
                QodeIconButton(
                    onClick = {},
                    icon = Icons.Default.Check,
                    contentDescription = "Check",
                    size = QodeButtonSize.Medium,
                    variant = QodeButtonVariant.Outlined,
                )
            }

            QodeTextButton(
                text = "Modern Text Button",
                onClick = {},
                showUnderline = true,
            )
        }
    }
}
