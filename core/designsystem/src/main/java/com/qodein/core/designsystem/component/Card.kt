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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeElevation
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Card variants for Qode design system
 */
enum class QodeCardVariant {
    Elevated,
    Filled,
    Outlined
}

/**
 * Production-ready card component for Qode design system
 *
 * @param modifier Modifier to be applied to the card
 * @param variant The visual variant of the card
 * @param onClick Optional click handler (makes the card clickable)
 * @param shape The shape of the card
 * @param enabled Whether the card is enabled (for clickable cards)
 * @param contentPadding Padding for the card content
 * @param content The content of the card
 */
@Composable
fun QodeCard(
    modifier: Modifier = Modifier,
    variant: QodeCardVariant = QodeCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(QodeCorners.md),
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
                    defaultElevation = QodeElevation.sm,
                    pressedElevation = QodeElevation.xs,
                    hoveredElevation = QodeElevation.md,
                ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
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

        QodeCardVariant.Filled -> {
            Card(
                modifier = cardModifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content,
                )
            }
        }

        QodeCardVariant.Outlined -> {
            OutlinedCard(
                modifier = cardModifier,
                shape = shape,
                border = BorderStroke(
                    width = QodeBorder.thin,
                    color = MaterialTheme.colorScheme.outline,
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
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

/**
 * An expandable card that can show/hide additional content
 *
 * @param modifier Modifier to be applied to the card
 * @param variant The visual variant of the card
 * @param shape The shape of the card
 * @param title The title content of the card
 * @param expandedContent The content to show when expanded
 * @param expanded Whether the card is expanded
 * @param onExpandedChange Called when expansion state changes
 * @param enabled Whether the card is enabled
 */
@Composable
fun QodeExpandableCard(
    modifier: Modifier = Modifier,
    variant: QodeCardVariant = QodeCardVariant.Elevated,
    shape: Shape = RoundedCornerShape(QodeCorners.md),
    title: @Composable () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
        label = "expand_icon_rotation",
    )

    QodeCard(
        modifier = modifier.animateContentSize(
            animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
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
            enter = expandVertically(animationSpec = tween(durationMillis = QodeAnimation.MEDIUM)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = QodeAnimation.MEDIUM)),
        ) {
            Column(
                modifier = Modifier.padding(top = SpacingTokens.md),
                content = expandedContent,
            )
        }
    }
}

/**
 * A card with actions at the bottom
 *
 * @param modifier Modifier to be applied to the card
 * @param variant The visual variant of the card
 * @param shape The shape of the card
 * @param content The main content of the card
 * @param actions The action buttons to display at the bottom
 */
@Composable
fun QodeCardWithActions(
    modifier: Modifier = Modifier,
    variant: QodeCardVariant = QodeCardVariant.Elevated,
    shape: Shape = RoundedCornerShape(QodeCorners.md),
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    QodeCard(
        modifier = modifier,
        variant = variant,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            content = content,
        )

        HorizontalDivider(
            thickness = QodeBorder.thin,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
            horizontalArrangement = Arrangement.End,
            content = actions,
        )
    }
}

/**
 * A simple list item card for displaying items in a list
 *
 * @param modifier Modifier to be applied to the card
 * @param title The title text
 * @param subtitle Optional subtitle text
 * @param leadingContent Optional leading content (e.g., icon, avatar)
 * @param trailingContent Optional trailing content (e.g., icon, switch)
 * @param onClick Optional click handler
 * @param enabled Whether the card is enabled
 */
@Composable
fun QodeListCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(enabled = enabled, onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.let {
                it()
                Spacer(modifier = Modifier.width(SpacingTokens.md))
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            trailingContent?.let {
                Spacer(modifier = Modifier.width(SpacingTokens.md))
                it()
            }
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

@Preview(name = "Card with Actions", showBackground = true)
@Composable
private fun QodeCardWithActionsPreview() {
    QodeTheme {
        QodeCardWithActions(
            modifier = Modifier.padding(SpacingTokens.md),
            content = {
                Text("Promo Code: SAVE20", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(SpacingTokens.xs))
                Text(
                    "Get 20% off on all electronics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            actions = {
                QodeButton(
                    onClick = {},
                    text = "Copy",
                    variant = QodeButtonVariant.Text,
                    size = QodeButtonSize.Small,
                )
                QodeButton(
                    onClick = {},
                    text = "Use Now",
                    variant = QodeButtonVariant.Primary,
                    size = QodeButtonSize.Small,
                )
            },
        )
    }
}

@Preview(name = "List Cards", showBackground = true)
@Composable
private fun QodeListCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            QodeListCard(
                title = "Kaspi Bank",
                subtitle = "5 active promo codes",
                leadingContent = {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(QodeCorners.sm),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            QodeListCard(
                title = "Notifications",
                subtitle = "Get alerts for new promo codes",
                leadingContent = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(QodeSize.iconMedium),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        modifier = Modifier.height(QodeSize.iconMedium),
                    )
                },
            )
        }
    }
}
