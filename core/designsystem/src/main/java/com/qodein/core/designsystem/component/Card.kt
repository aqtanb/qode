package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Card variants for Qode design system
 */
enum class QodeCardVariant {
    Elevated,
    Filled,
    Outlined
}

@Composable
fun QodeCard(
    modifier: Modifier = Modifier,
    variant: QodeCardVariant = QodeCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(SpacingTokens.md),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val cardModifier = if (onClick != null) {
        modifier
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
    } else {
        modifier
    }

    when (variant) {
        QodeCardVariant.Elevated -> {
            ElevatedCard(
                modifier = cardModifier,
                shape = shape,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = ElevationTokens.large,
                    pressedElevation = ElevationTokens.medium,
                    hoveredElevation = ElevationTokens.extraLarge,
                ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                Column(
                    modifier = modifier.padding(contentPadding),
                    content = content,
                )
            }
        }

        QodeCardVariant.Filled -> {
            Card(
                modifier = cardModifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                ),
            ) {
                Column(
                    modifier = modifier.padding(contentPadding),
                    content = content,
                )
            }
        }

        QodeCardVariant.Outlined -> {
            OutlinedCard(
                modifier = cardModifier,
                shape = shape,
                border = BorderStroke(
                    width = ShapeTokens.Border.thin,
                    color = MaterialTheme.colorScheme.outline,
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content,
                )
            }
        }
    }
}

@Composable
fun QodeExpandableCard(
    modifier: Modifier = Modifier,
    variant: QodeCardVariant = QodeCardVariant.Elevated,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.medium),
    title: @Composable () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM),
        label = "expand_icon_rotation",
    )

    QodeCard(
        modifier = modifier.animateContentSize(
            animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM),
        ),
        variant = variant,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = { onExpandedChange(!expanded) },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                title()
            }

            IconButton(
                onClick = { onExpandedChange(!expanded) },
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM)),
        ) {
            Column(
                modifier = Modifier.padding(top = SpacingTokens.md),
                content = expandedContent,
            )
        }
    }
}

// Previews
@Preview(name = "Card Variants", showBackground = true)
@Composable
private fun QodeCardVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeCard(variant = QodeCardVariant.Elevated) {
                Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is an elevated card with a subtle shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            QodeCard(variant = QodeCardVariant.Filled) {
                Text("Filled Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is a filled card with a background color.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            QodeCard(variant = QodeCardVariant.Outlined) {
                Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is an outlined card with a border.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Expandable Card", showBackground = true)
@Composable
private fun QodeExpandableCardPreview() {
    QodeTheme {
        var expanded by remember { mutableStateOf(false) }

        QodeExpandableCard(
            modifier = Modifier.padding(SpacingTokens.md),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            title = {
                Column {
                    Text("Terms & Conditions", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap to read the full terms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            expandedContent = {
                Text(
                    "• Valid for new customers only\n" +
                        "• Cannot be combined with other offers\n" +
                        "• Expires on December 31, 2024\n" +
                        "• Minimum purchase of 10,000 KZT required",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}
