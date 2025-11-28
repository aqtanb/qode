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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Qodein themed filter chip for selection and filtering content
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param selected Whether the chip is selected/active
 * @param enabled Whether the chip is enabled
 * @param filled Whether to use filled style (true) or outlined style (false)
 * @param leadingIcon Optional composable to display before the label
 * @param trailingIcon Optional icon to display after the label
 * @param onClose Called when close button is clicked (shows close icon when provided)
 * @param shape Shape of the chip
 */
@Composable
fun QodeinFilterChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    filled: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
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
        leadingIcon = leadingIcon,
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

/**
 * Qodein themed assist chip for displaying information or simple actions
 * Perfect for non-interactive tags or informational chips
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param enabled Whether the chip is enabled
 * @param leadingIcon Optional icon to display before the label
 * @param trailingIcon Optional icon to display after the label
 * @param shape Shape of the chip
 */
@Composable
fun QodeinAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.full)
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
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
        trailingIcon = trailingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = enabled,
            borderColor = MaterialTheme.colorScheme.outline,
            borderWidth = ShapeTokens.Border.thin,
        ),
        shape = shape,
    )
}

/**
 * Qodein themed input chip for representing user input with optional removal
 * Perfect for selected items that can be removed (e.g., selected tags)
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param selected Whether the chip is selected/active
 * @param enabled Whether the chip is enabled
 * @param leadingIcon Optional composable to display before the label
 * @param onClose Called when close button is clicked (shows close icon when provided)
 * @param shape Shape of the chip
 */
@Composable
fun QodeinInputChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.full)
) {
    InputChip(
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
        leadingIcon = leadingIcon,
        trailingIcon = onClose?.let {
            {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            labelColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = ShapeTokens.Border.thin,
            selectedBorderWidth = ShapeTokens.Border.thin,
        ),
        shape = shape,
    )
}

/**
 * Qodein themed suggestion chip for presenting dynamic suggestions
 * Helps narrow user intent with actionable suggestions
 *
 * @param label Text to display on the chip
 * @param onClick Called when the chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param enabled Whether the chip is enabled
 * @param icon Optional icon to display before the label
 * @param shape Shape of the chip
 */
@Composable
fun QodeinSuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.full)
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        enabled = enabled,
        modifier = modifier,
        icon = icon?.let { iconVector ->
            {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = enabled,
            borderColor = MaterialTheme.colorScheme.outline,
            borderWidth = ShapeTokens.Border.thin,
        ),
        shape = shape,
    )
}

@ThemePreviews
@Composable
private fun QodeinChipPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Filter Chips
            Text(
                text = "Filter Chips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QodeinFilterChip(
                    label = "Filter",
                    onClick = {},
                )
                QodeinFilterChip(
                    label = "Selected",
                    onClick = {},
                    selected = true,
                    filled = true,
                )
                QodeinFilterChip(
                    label = "With Icon",
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            imageVector = QodeStatusIcons.Trending,
                            contentDescription = null,
                            modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                        )
                    },
                )
                QodeinFilterChip(
                    label = "With Close",
                    onClick = {},
                    filled = true,
                    onClose = {},
                )
            }

            // Assist Chips
            Text(
                text = "Assist Chips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QodeinAssistChip(
                    label = "Assist",
                    onClick = {},
                )
                QodeinAssistChip(
                    label = "With Icon",
                    onClick = {},
                    leadingIcon = QodeActionIcons.Next,
                )
            }

            // Input Chips
            Text(
                text = "Input Chips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QodeinInputChip(
                    label = "Input",
                    onClick = {},
                )
                QodeinInputChip(
                    label = "Selected",
                    onClick = {},
                    selected = true,
                    onClose = {},
                )
            }

            // Suggestion Chips
            Text(
                text = "Suggestion Chips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QodeinSuggestionChip(
                    label = "Suggestion",
                    onClick = {},
                )
                QodeinSuggestionChip(
                    label = "With Icon",
                    onClick = {},
                    icon = QodeStatusIcons.Trending,
                )
            }
        }
    }
}
