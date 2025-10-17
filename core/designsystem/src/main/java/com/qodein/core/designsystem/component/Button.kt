package com.qodein.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Button sizes for Qode design system
 */
enum class ButtonSize {
    Small,
    Medium,
    Large
}

/**
 * Internal configuration holder for button sizing and styling
 */
private data class ButtonConfig(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val textStyle: TextStyle,
    val iconSize: Dp
)

@Composable
private fun ButtonSize.toConfig(): ButtonConfig {
    val textStyle = when (this) {
        ButtonSize.Small -> MaterialTheme.typography.labelSmall
        ButtonSize.Medium -> MaterialTheme.typography.labelLarge
        ButtonSize.Large -> MaterialTheme.typography.titleMedium
    }

    return ButtonConfig(
        height = when (this) {
            ButtonSize.Small -> SizeTokens.Button.heightSmall
            ButtonSize.Medium -> SizeTokens.Button.heightMedium
            ButtonSize.Large -> SizeTokens.Button.heightLarge
        },
        horizontalPadding = when (this) {
            ButtonSize.Small -> SpacingTokens.xs
            ButtonSize.Medium -> SpacingTokens.md
            ButtonSize.Large -> SpacingTokens.lg
        },
        verticalPadding = when (this) {
            ButtonSize.Small -> SpacingTokens.none
            ButtonSize.Medium -> SpacingTokens.xxxs
            ButtonSize.Large -> SpacingTokens.xxs
        },
        textStyle = textStyle,
        iconSize = when (this) {
            ButtonSize.Small -> SizeTokens.Icon.sizeSmall
            ButtonSize.Medium -> SizeTokens.Icon.sizeLarge
            ButtonSize.Large -> SizeTokens.Icon.sizeLarge
        },
    )
}

/**
 * Primary filled button component for Qode design system
 *
 * @param onClick Called when the button is clicked
 * @param text The text to display in the button
 * @param modifier Modifier to be applied to the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param containerColor Background color of the button
 * @param contentColor Color of text and icons
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
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.large),
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val config = size.toConfig()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast(),
        label = "button_scale",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(config.height)
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
        ),
        contentPadding = PaddingValues(
            horizontal = config.horizontalPadding,
            vertical = config.verticalPadding,
        ),
        interactionSource = interactionSource,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            loading = loading,
            textStyle = config.textStyle,
            iconSize = config.iconSize,
        )
    }
}

/**
 * Outlined button component for Qode design system
 *
 * @param onClick Called when the button is clicked
 * @param text The text to display in the button
 * @param modifier Modifier to be applied to the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param containerColor Background color of the button
 * @param contentColor Color of text and icons
 * @param borderColor Color of the border
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param shape The shape of the button
 * @param contentDescription Accessibility description for the button
 */
@Composable
fun QodeOutlinedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.large),
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val config = size.toConfig()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast(),
        label = "button_scale",
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(config.height)
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = OpacityTokens.DISABLED_CONTAINER),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
        ),
        border = BorderStroke(
            width = ShapeTokens.Border.thin,
            color = if (enabled && !loading) {
                borderColor
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED_CONTAINER)
            },
        ),
        contentPadding = PaddingValues(
            horizontal = config.horizontalPadding,
            vertical = config.verticalPadding,
        ),
        interactionSource = interactionSource,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            loading = loading,
            textStyle = config.textStyle,
            iconSize = config.iconSize,
        )
    }
}

/**
 * Text button component for Qode design system
 *
 * @param onClick Called when the button is clicked
 * @param text The text to display in the button
 * @param modifier Modifier to be applied to the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param contentColor Color of text and icons
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param shape The shape of the button
 * @param showUnderline Whether to show underline decoration
 * @param contentDescription Accessibility description for the button
 */
@Composable
fun QodeTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.large),
    showUnderline: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val config = size.toConfig()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast(),
        label = "button_scale",
    )

    val textStyle = config.textStyle.copy(
        textDecoration = if (showUnderline) TextDecoration.Underline else null,
    )

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(config.height)
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
        ),
        contentPadding = PaddingValues(
            horizontal = config.horizontalPadding,
            vertical = config.verticalPadding,
        ),
        interactionSource = interactionSource,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            loading = loading,
            textStyle = textStyle,
            iconSize = config.iconSize,
        )
    }
}

/**
 * Internal configuration holder for icon button sizing
 */
private data class IconButtonConfig(val buttonSize: Dp, val iconSize: Dp)

@Composable
private fun ButtonSize.toIconButtonConfig(): IconButtonConfig =
    IconButtonConfig(
        buttonSize = when (this) {
            ButtonSize.Small -> SizeTokens.IconButton.sizeSmall
            ButtonSize.Medium -> SizeTokens.IconButton.sizeMedium
            ButtonSize.Large -> SizeTokens.IconButton.sizeLarge
        },
        iconSize = when (this) {
            ButtonSize.Small -> SizeTokens.Icon.sizeSmall
            ButtonSize.Medium -> SizeTokens.Icon.sizeLarge
            ButtonSize.Large -> SizeTokens.Icon.sizeXLarge
        },
    )

/**
 * Icon-only button component for Qode design system
 *
 * @param onClick Called when the button is clicked
 * @param icon The icon to display
 * @param contentDescription Accessibility description for the button
 * @param modifier Modifier to be applied to the button
 * @param size The size of the button
 * @param enabled Whether the button is enabled
 * @param loading Whether the button is in loading state
 * @param outlined Whether to use outlined style
 * @param containerColor Background color of the button (transparent for outlined)
 * @param contentColor Color of the icon
 * @param borderColor Color of the border (only used when outlined = true)
 */
@Composable
fun QodeinIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    outlined: Boolean = false,
    containerColor: Color = if (outlined) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = if (outlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.outline
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val config = size.toIconButtonConfig()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) MotionTokens.Scale.ICON_PRESSED else 1f,
        animationSpec = AnimationTokens.Spec.fast(),
        label = "icon_button_scale",
    )

    Box(
        modifier = modifier
            .size(config.buttonSize)
            .scale(scale),
    ) {
        if (outlined) {
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
                        borderColor
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
                    iconSize = config.iconSize,
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
                    disabledContainerColor = if (containerColor == Color.Transparent) {
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
                    iconSize = config.iconSize,
                    contentColor = contentColor,
                )
            }
        }
    }
}

/**
 * Button content with optimized icon-text layout and proper spacing
 */
@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    loading: Boolean,
    textStyle: TextStyle,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    if (loading) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                strokeWidth = ShapeTokens.Border.thin,
            )
        }
    } else {
        Row(
            modifier = Modifier.padding(vertical = SpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(end = SpacingTokens.xs),
                )
            }

            Text(
                text = text,
                style = textStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            trailingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(start = SpacingTokens.xs),
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

// MARK: - Previews

@PreviewLightDark
@Composable
private fun QodeButtonVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Button",
            )
            QodeOutlinedButton(
                onClick = {},
                text = "Button",
            )
            QodeTextButton(
                onClick = {},
                text = "Button",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeButtonSizesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Small Button",
                size = ButtonSize.Small,
            )
            QodeButton(
                onClick = {},
                text = "Medium Button",
                size = ButtonSize.Medium,
            )
            QodeButton(
                onClick = {},
                text = "Large Button",
                size = ButtonSize.Large,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeButtonWithIconPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Button",
                leadingIcon = Icons.Default.Add,
            )
            QodeOutlinedButton(
                onClick = {},
                text = "Button",
                trailingIcon = Icons.Default.ArrowForward,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeButtonLoadingPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Button",
                loading = true,
            )
            QodeOutlinedButton(
                onClick = {},
                text = "Button",
                loading = true,
            )
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                loading = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeButtonDisabledPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeButton(
                onClick = {},
                text = "Button",
                enabled = false,
            )
            QodeOutlinedButton(
                onClick = {},
                text = "Button",
                enabled = false,
            )
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                enabled = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QodeinIconButtonPreview() {
    QodeTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                size = ButtonSize.Small,
            )
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                size = ButtonSize.Medium,
            )
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                size = ButtonSize.Large,
            )
            QodeinIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite",
                size = ButtonSize.Medium,
                outlined = true,
            )
        }
    }
}
