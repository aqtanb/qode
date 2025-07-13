package com.qodein.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Chip variants for different use cases
 */
enum class QodeChipVariant {
    Filter, // For filtering content
    Choice, // For single selection
    Input, // For user input/tags
    Action, // For triggering actions
    Suggestion // For suggestions/recommendations
}

/**
 * Chip sizes
 */
enum class QodeChipSize {
    Small,
    Medium,
    Large
}

/**
 * Qode themed chip component
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param selected Whether the chip is selected/active
 * @param enabled Whether the chip is enabled
 * @param variant Visual variant of the chip
 * @param size Size variant of the chip
 * @param leadingIcon Optional icon to display before the label
 * @param trailingIcon Optional icon to display after the label
 * @param onClose Called when close button is clicked (shows close icon when provided)
 */
@Composable
fun QodeChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    variant: QodeChipVariant = QodeChipVariant.Filter,
    size: QodeChipSize = QodeChipSize.Medium,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClose: (() -> Unit)? = null
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = when (size) {
                    QodeChipSize.Small -> MaterialTheme.typography.labelSmall
                    QodeChipSize.Medium -> MaterialTheme.typography.labelMedium
                    QodeChipSize.Large -> MaterialTheme.typography.labelLarge
                },
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
                    modifier = Modifier.size(
                        when (size) {
                            QodeChipSize.Small -> 14.dp
                            QodeChipSize.Medium -> 18.dp
                            QodeChipSize.Large -> 20.dp
                        },
                    ),
                )
            }
        },
        trailingIcon = when {
            onClose != null -> {
                {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(
                            when (size) {
                                QodeChipSize.Small -> 16.dp
                                QodeChipSize.Medium -> 20.dp
                                QodeChipSize.Large -> 24.dp
                            },
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(
                                when (size) {
                                    QodeChipSize.Small -> 12.dp
                                    QodeChipSize.Medium -> 16.dp
                                    QodeChipSize.Large -> 18.dp
                                },
                            ),
                        )
                    }
                }
            }
            trailingIcon != null -> {
                {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(
                            when (size) {
                                QodeChipSize.Small -> 14.dp
                                QodeChipSize.Medium -> 18.dp
                                QodeChipSize.Large -> 20.dp
                            },
                        ),
                    )
                }
            }
            else -> null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = when (variant) {
                QodeChipVariant.Filter -> MaterialTheme.colorScheme.surface
                QodeChipVariant.Choice -> MaterialTheme.colorScheme.surface
                QodeChipVariant.Input -> MaterialTheme.colorScheme.surfaceVariant
                QodeChipVariant.Action -> MaterialTheme.colorScheme.primaryContainer
                QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.secondaryContainer
            },
            labelColor = when (variant) {
                QodeChipVariant.Action -> MaterialTheme.colorScheme.onPrimaryContainer
                QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            selectedContainerColor = when (variant) {
                QodeChipVariant.Action -> MaterialTheme.colorScheme.primary
                QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            selectedLabelColor = when (variant) {
                QodeChipVariant.Action -> MaterialTheme.colorScheme.onPrimary
                QodeChipVariant.Suggestion -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            },
        ),
        border = if (!selected && variant == QodeChipVariant.Filter) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            null
        },
    )
}

// Previews
@Preview(name = "Chip Variants", showBackground = true)
@Composable
private fun QodeChipVariantsPreview() {
    QodeTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            QodeChip(
                label = "Filter Chip",
                onClick = {},
                variant = QodeChipVariant.Filter,
                leadingIcon = QodeIcons.PromoCode,
            )

            QodeChip(
                label = "Selected Filter",
                onClick = {},
                selected = true,
                variant = QodeChipVariant.Filter,
                leadingIcon = QodeIcons.Verified,
            )

            QodeChip(
                label = "Action Chip",
                onClick = {},
                variant = QodeChipVariant.Action,
                leadingIcon = QodeIcons.Follow,
            )

            QodeChip(
                label = "Choice Chip",
                onClick = {},
                variant = QodeChipVariant.Choice,
                selected = true,
            )

            QodeChip(
                label = "Input with Close",
                onClick = {},
                variant = QodeChipVariant.Input,
                onClose = {},
            )

            QodeChip(
                label = "Suggestion Chip",
                onClick = {},
                variant = QodeChipVariant.Suggestion,
                leadingIcon = QodeIcons.Trending,
            )
        }
    }
}

@Preview(name = "Chip Sizes", showBackground = true)
@Composable
private fun QodeChipSizesPreview() {
    QodeTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            QodeChip(
                label = "Small Chip",
                onClick = {},
                size = QodeChipSize.Small,
                leadingIcon = QodeIcons.Store,
            )

            QodeChip(
                label = "Medium Chip",
                onClick = {},
                size = QodeChipSize.Medium,
                leadingIcon = QodeIcons.Store,
            )

            QodeChip(
                label = "Large Chip",
                onClick = {},
                size = QodeChipSize.Large,
                leadingIcon = QodeIcons.Store,
            )
        }
    }
}
