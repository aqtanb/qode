package com.qodein.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Chip variants for Qode design system
 */
enum class QodeChipVariant {
    Filter, // For filtering content
    Suggestion, // For suggestions/recommendations
    Input, // For user inputs (e.g., tags)
    Action // For actionable chips
}

/**
 * Chip size options
 */
enum class QodeChipSize {
    Small,
    Medium
}

/**
 * Production-ready chip component for Qode design system
 *
 * @param onClick Called when the chip is clicked (null for non-interactive chips)
 * @param modifier Modifier to be applied to the chip
 * @param variant The variant of the chip
 * @param size The size of the chip
 * @param selected Whether the chip is selected (for Filter variant)
 * @param enabled Whether the chip is enabled
 * @param label The text label of the chip
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon (ignored if onClose is provided)
 * @param onClose Called when close icon is clicked (shows close icon when provided)
 * @param shape The shape of the chip
 * @param contentDescription Accessibility description
 */
@Composable
fun QodeChip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: QodeChipVariant = QodeChipVariant.Filter,
    size: QodeChipSize = QodeChipSize.Medium,
    selected: Boolean = false,
    enabled: Boolean = true,
    label: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClose: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(QodeCorners.sm),
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && onClick != null) 0.96f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "chip_scale",
    )

    // Height and padding based on size
    val chipHeight = when (size) {
        QodeChipSize.Small -> QodeSize.chipHeightSmall
        QodeChipSize.Medium -> QodeSize.chipHeight
    }

    val horizontalPadding = when (size) {
        QodeChipSize.Small -> QodeSpacing.sm
        QodeChipSize.Medium -> QodeSpacing.md
    }

    val textStyle = when (size) {
        QodeChipSize.Small -> MaterialTheme.typography.labelSmall
        QodeChipSize.Medium -> MaterialTheme.typography.labelMedium
    }

    val iconSize = when (size) {
        QodeChipSize.Small -> 14.dp
        QodeChipSize.Medium -> 18.dp
    }

    // Colors based on variant and state
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            variant == QodeChipVariant.Filter && selected -> MaterialTheme.colorScheme.primary
            variant == QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.secondaryContainer
            variant == QodeChipVariant.Input -> MaterialTheme.colorScheme.surfaceVariant
            variant == QodeChipVariant.Action -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
        label = "container_color",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            variant == QodeChipVariant.Filter && selected -> MaterialTheme.colorScheme.onPrimary
            variant == QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.onSecondaryContainer
            variant == QodeChipVariant.Input -> MaterialTheme.colorScheme.onSurfaceVariant
            variant == QodeChipVariant.Action -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
        label = "content_color",
    )

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        variant == QodeChipVariant.Filter && !selected -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }

    // Determine if chip should have a border
    val showBorder = variant == QodeChipVariant.Filter && !selected

    Surface(
        onClick = onClick ?: {},
        modifier = modifier
            .height(chipHeight)
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        enabled = enabled && onClick != null,
        shape = shape,
        color = containerColor,
        border = if (showBorder) {
            BorderStroke(QodeBorder.thin, borderColor)
        } else {
            null
        },
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leading icon or checkmark for selected filter chips
            when {
                variant == QodeChipVariant.Filter && selected -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = contentColor,
                    )
                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                }
                leadingIcon != null -> {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = contentColor,
                    )
                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                }
            }

            // Label
            Text(
                text = label,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Trailing icon or close button
            when {
                onClose != null -> {
                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(iconSize),
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(iconSize - 2.dp),
                            tint = contentColor,
                        )
                    }
                }
                trailingIcon != null -> {
                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = contentColor,
                    )
                }
            }
        }
    }
}

/**
 * A group of chips for easy layout
 */
@Composable
fun QodeChipGroup(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = QodeSpacing.sm,
    content: @Composable () -> Unit
) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        content()
    }
}

// Previews
@Preview(name = "Chip Variants", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun QodeChipVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            // Filter chips
            Text("Filter Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "All",
                    variant = QodeChipVariant.Filter,
                    selected = true,
                    onClick = {},
                )
                QodeChip(
                    label = "Electronics",
                    variant = QodeChipVariant.Filter,
                    selected = false,
                    onClick = {},
                )
                QodeChip(
                    label = "Fashion",
                    variant = QodeChipVariant.Filter,
                    selected = false,
                    onClick = {},
                    enabled = false,
                )
            }

            // Suggestion chips
            Text("Suggestion Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "Popular",
                    variant = QodeChipVariant.Suggestion,
                    leadingIcon = Icons.Default.Star,
                    onClick = {},
                )
                QodeChip(
                    label = "New",
                    variant = QodeChipVariant.Suggestion,
                    onClick = {},
                )
                QodeChip(
                    label = "Trending",
                    variant = QodeChipVariant.Suggestion,
                    trailingIcon = Icons.Default.KeyboardArrowUp,
                    onClick = {},
                )
            }

            // Input chips
            Text("Input Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "Kaspi",
                    variant = QodeChipVariant.Input,
                    onClose = {},
                )
                QodeChip(
                    label = "Arbuz",
                    variant = QodeChipVariant.Input,
                    onClose = {},
                )
                QodeChip(
                    label = "Magnum",
                    variant = QodeChipVariant.Input,
                    leadingIcon = Icons.Default.ShoppingCart,
                    onClose = {},
                )
            }

            // Action chips
            Text("Action Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "Share",
                    variant = QodeChipVariant.Action,
                    leadingIcon = Icons.Default.Share,
                    onClick = {},
                )
                QodeChip(
                    label = "Copy Code",
                    variant = QodeChipVariant.Action,
                    leadingIcon = Icons.Default.Edit,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(name = "Chip Sizes", showBackground = true)
@Composable
private fun QodeChipSizesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            Text("Small Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "Small",
                    size = QodeChipSize.Small,
                    onClick = {},
                )
                QodeChip(
                    label = "With Icon",
                    size = QodeChipSize.Small,
                    leadingIcon = Icons.Default.Star,
                    onClick = {},
                )
                QodeChip(
                    label = "Closeable",
                    size = QodeChipSize.Small,
                    variant = QodeChipVariant.Input,
                    onClose = {},
                )
            }

            Text("Medium Chips", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeChip(
                    label = "Medium",
                    size = QodeChipSize.Medium,
                    onClick = {},
                )
                QodeChip(
                    label = "With Icon",
                    size = QodeChipSize.Medium,
                    leadingIcon = Icons.Default.Star,
                    onClick = {},
                )
                QodeChip(
                    label = "Closeable",
                    size = QodeChipSize.Medium,
                    variant = QodeChipVariant.Input,
                    onClose = {},
                )
            }
        }
    }
}

@Preview(name = "Chip Group", showBackground = true)
@Composable
private fun QodeChipGroupPreview() {
    QodeTheme {
        val categories = listOf(
            "All" to true,
            "Food & Drinks" to false,
            "Electronics" to false,
            "Fashion" to false,
            "Beauty" to false,
            "Sports" to false,
            "Home & Garden" to false,
            "Travel" to false,
        )

        Column(modifier = Modifier.padding(QodeSpacing.md)) {
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = QodeSpacing.sm),
            )

            QodeChipGroup {
                categories.forEach { (category, selected) ->
                    QodeChip(
                        label = category,
                        variant = QodeChipVariant.Filter,
                        selected = selected,
                        onClick = {},
                    )
                }
            }
        }
    }
}
