package com.qodein.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Qode themed chip component
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param selected Whether the chip is selected/active
 * @param enabled Whether the chip is enabled
 * @param filled Whether to use filled style (true) or outlined style (false)
 * @param leadingIcon Optional icon to display before the label
 * @param trailingIcon Optional icon to display after the label
 * @param onClose Called when close button is clicked (shows close icon when provided)
 * @param shape Shape of the chip
 */
@Composable
fun QodeinChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    filled: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClose: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.full)
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        selected = selected,
        enabled = enabled,
        modifier = modifier,
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }
        },
        trailingIcon = when {
            onClose != null -> {
                {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(SizeTokens.IconButton.sizeSmall),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                        )
                    }
                }
            }
            trailingIcon != null -> {
                {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                    )
                }
            }
            else -> null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = when {
                filled && selected -> MaterialTheme.colorScheme.primaryContainer
                filled && !selected -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            },
            labelColor = when {
                filled && selected -> MaterialTheme.colorScheme.onPrimaryContainer
                filled && !selected -> MaterialTheme.colorScheme.onSecondaryContainer
                selected -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            selectedContainerColor = if (filled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
            selectedLabelColor = if (filled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        border = if (!filled) {
            BorderStroke(
                width = ShapeTokens.Border.thin,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
            )
        } else {
            null
        },
        shape = shape,
    )
}

@PreviewLightDark
@Composable
private fun QodeinChipPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Main content
            QodeinChip(
                label = "Chip",
                onClick = {},
            )

            // Filled variant
            QodeinChip(
                label = "Filled",
                onClick = {},
                filled = true,
            )

            // Outlined variant
            QodeinChip(
                label = "Outlined",
                onClick = {},
                filled = false,
            )

            // With leading icon
            QodeinChip(
                label = "Leading Icon",
                onClick = {},
                leadingIcon = QodeStatusIcons.Trending,
            )

            // With trailing icon
            QodeinChip(
                label = "Trailing Icon",
                onClick = {},
                trailingIcon = QodeActionIcons.Next,
            )

            // With close
            QodeinChip(
                label = "With Close",
                onClick = {},
                filled = true,
                onClose = {},
            )
        }
    }
}
