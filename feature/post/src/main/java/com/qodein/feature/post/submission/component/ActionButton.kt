package com.qodein.feature.post.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Action button with icon, label, and count indicator
 */
@Composable
internal fun ActionButton(
    icon: ImageVector,
    label: String,
    count: Int,
    maxCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    Box(
        modifier = modifier
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            )
            .border(
                width = if (isActive) ShapeTokens.Border.medium else ShapeTokens.Border.thin,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
            )
            .clickable(onClick = onClick)
            .padding(SpacingTokens.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            // Count badge
            Box(
                modifier = Modifier
                    .background(
                        color = if (count > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        shape = CircleShape,
                    )
                    .padding(horizontal = SpacingTokens.xs, vertical = 2.dp),
            ) {
                Text(
                    text = "$count/$maxCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (count > 0) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
