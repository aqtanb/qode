package com.qodein.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Applies conditional click handling to a modifier.
 * If onClick is provided, adds clickable modifier with button semantics.
 */
private fun Modifier.conditionalClickable(
    onClick: (() -> Unit)?,
    enabled: Boolean,
    interactionSource: MutableInteractionSource
): Modifier =
    if (onClick != null) {
        this
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
    } else {
        this
    }

/**
 * Elevated card with shadow elevation following Qode design system.
 *
 * Use for content that needs visual hierarchy and separation from the background.
 *
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler. When provided, card becomes clickable with button semantics
 * @param shape Shape of the card corners
 * @param enabled Whether the card and its click interaction are enabled
 * @param contentPadding Padding applied to the content inside the card
 * @param content The content displayed inside the card
 */
@Composable
fun QodeinElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(SpacingTokens.md),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = modifier.conditionalClickable(onClick, enabled, interactionSource),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = ElevationTokens.large,
            pressedElevation = ElevationTokens.medium,
            hoveredElevation = ElevationTokens.extraLarge,
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = OpacityTokens.DISABLED),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

/**
 * Outlined card with border and transparent background following Qode design system.
 *
 * Use for content that needs subtle visual separation without heavy elevation.
 *
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler. When provided, card becomes clickable with button semantics
 * @param shape Shape of the card corners
 * @param enabled Whether the card and its click interaction are enabled
 * @param contentPadding Padding applied to the content inside the card
 * @param content The content displayed inside the card
 */
@Composable
fun QodeinOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(SpacingTokens.md),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedCard(
        modifier = modifier.conditionalClickable(onClick, enabled, interactionSource),
        shape = shape,
        border = BorderStroke(
            width = ShapeTokens.Border.thin,
            color = MaterialTheme.colorScheme.outline,
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = OpacityTokens.DISABLED),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.DISABLED),
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

/**
 * Filled card with colored background following Qode design system.
 *
 * The default card variant with a filled primaryContainer background.
 * Use for content that needs emphasis with a colored container.
 *
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler. When provided, card becomes clickable with button semantics
 * @param shape Shape of the card corners
 * @param enabled Whether the card and its click interaction are enabled
 * @param contentPadding Padding applied to the content inside the card
 * @param content The content displayed inside the card
 */
@Composable
fun QodeinCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(SpacingTokens.md),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier.conditionalClickable(onClick, enabled, interactionSource),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = OpacityTokens.DISABLED),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = OpacityTokens.DISABLED),
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

// MARK: Previews

@PreviewLightDark
@Composable
private fun QodeCardVariantsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            QodeinElevatedCard {
                Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is an elevated card with a shadow.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            QodeinCard {
                Text("Filled Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is a filled card with a background color.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            QodeinOutlinedCard {
                Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is an outlined card with a border.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
