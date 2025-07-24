package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeBorder
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSize
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens

/**
 * Custom divider with various styles
 */
@Composable
fun QodeDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = QodeBorder.thin,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .padding(start = startIndent, end = endIndent)
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}

/**
 * Section header for grouping content
 */
@Composable
fun QodeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QodeSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}

/**
 * Empty state for lists or screens
 */
@Composable
fun QodeEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(QodeSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(QodeSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        description?.let {
            Spacer(modifier = Modifier.height(QodeSpacing.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(QodeSpacing.lg))
            it()
        }
    }
}

/**
 * Error state for failed operations
 */
@Composable
fun QodeErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: (() -> Unit)? = null
) {
    QodeEmptyState(
        modifier = modifier,
        icon = Icons.Default.Warning,
        title = "Something went wrong",
        description = message,
        action = onRetry?.let {
            {
                QodeButton(
                    onClick = it,
                    text = "Try Again",
                    variant = QodeButtonVariant.Primary,
                )
            }
        },
    )
}

/**
 * Badge component for status indicators
 */
@Composable
fun QodeBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(ShapeTokens.Corner.full),
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = QodeSpacing.sm,
                vertical = QodeSpacing.xs,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

/**
 * Avatar component for user profiles
 */
@Composable
fun QodeAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    text: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                content != null -> content()
                text != null -> {
                    Text(
                        text = text.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(size * 0.6f),
                        tint = contentColor,
                    )
                }
            }
        }
    }
}

/**
 * Info card for displaying tips or information
 */
@Composable
fun QodeInfoCard(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = RoundedCornerShape(QodeCorners.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QodeSpacing.md),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(QodeSize.iconSmall),
            )

            Spacer(modifier = Modifier.width(QodeSpacing.sm))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            onDismiss?.let {
                Spacer(modifier = Modifier.width(QodeSpacing.sm))
                IconButton(
                    onClick = it,
                    modifier = Modifier.size(QodeSize.iconSmall),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(QodeSize.iconSmall - 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * Progress indicator with label
 */
@Composable
fun QodeProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null || showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(QodeSpacing.xs))
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}

// Previews
@Preview(name = "Utility Components", showBackground = true)
@Composable
private fun QodeUtilityComponentsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            // Dividers
            Text("Dividers", style = MaterialTheme.typography.titleMedium)
            QodeDivider()
            QodeDivider(
                thickness = QodeBorder.medium,
                color = MaterialTheme.colorScheme.primary,
            )
            QodeDivider(startIndent = QodeSpacing.md, endIndent = QodeSpacing.md)

            // Section Headers
            Text("Section Headers", style = MaterialTheme.typography.titleMedium)
            QodeSectionHeader(
                title = "Popular Stores",
                subtitle = "Most used promo codes",
            )
            QodeSectionHeader(
                title = "Recent Activity",
                action = {
                    TextButton(onClick = {}) {
                        Text("View All")
                    }
                },
            )

            // Badges
            Text("Badges", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeBadge("NEW")
                QodeBadge(
                    "50% OFF",
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
                QodeBadge(
                    "VERIFIED",
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                )
            }

            // Avatars
            Text("Avatars", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm)) {
                QodeAvatar()
                QodeAvatar(text = "AK")
                QodeAvatar(
                    size = 56.dp,
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            // Info Cards
            Text("Info Cards", style = MaterialTheme.typography.titleMedium)
            QodeInfoCard(
                text = "Tip: You can save your favorite stores for quick access",
                onDismiss = {},
            )
            QodeInfoCard(
                text = "New promo codes are added daily!",
                icon = Icons.Default.Celebration,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            // Progress Indicators
            Text("Progress Indicators", style = MaterialTheme.typography.titleMedium)
            QodeProgressIndicator(
                progress = 0.7f,
                label = "Profile Completion",
            )
            QodeProgressIndicator(
                progress = 0.3f,
                showPercentage = false,
            )
        }
    }
}

@Preview(name = "Empty States", showBackground = true)
@Composable
private fun QodeEmptyStatesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.xl),
        ) {
            QodeEmptyState(
                icon = Icons.Default.Search,
                title = "No results found",
                description = "Try adjusting your search or filters",
                action = {
                    QodeButton(
                        onClick = {},
                        text = "Clear Filters",
                        variant = QodeButtonVariant.Outlined,
                    )
                },
            )

            QodeDivider()

            QodeErrorState(
                message = "Failed to load promo codes. Please check your connection.",
                onRetry = {},
            )
        }
    }
}
