package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Types of coming soon features
 */
enum class ComingSoonType {
    Feature, // Regular feature coming soon
    Premium, // Premium/paid feature
    InDevelopment, // Currently being developed
    Locked // Requires specific conditions
}

/**
 * ComingSoonCard component for features not yet implemented
 *
 * @param title The title of the feature
 * @param description Brief description of what's coming
 * @param type The type of coming soon feature
 * @param icon Optional icon for the feature
 * @param estimatedDate Optional estimated release date
 * @param onNotifyMeClick Called when user wants to be notified
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun ComingSoonCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    type: ComingSoonType = ComingSoonType.Feature,
    icon: ImageVector? = null,
    estimatedDate: String? = null,
    onNotifyMeClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coming_soon_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
        ),
        label = "shimmer_alpha",
    )

    val containerColor = when (type) {
        ComingSoonType.Feature -> MaterialTheme.colorScheme.surfaceVariant
        ComingSoonType.Premium -> MaterialTheme.colorScheme.tertiaryContainer
        ComingSoonType.InDevelopment -> MaterialTheme.colorScheme.primaryContainer
        ComingSoonType.Locked -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (type) {
        ComingSoonType.Feature -> MaterialTheme.colorScheme.onSurfaceVariant
        ComingSoonType.Premium -> MaterialTheme.colorScheme.onTertiaryContainer
        ComingSoonType.InDevelopment -> MaterialTheme.colorScheme.onPrimaryContainer
        ComingSoonType.Locked -> MaterialTheme.colorScheme.onErrorContainer
    }

    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Outlined,
    ) {
        Box {
            // Shimmer overlay for in-development items
            if (type == ComingSoonType.InDevelopment) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = shimmerAlpha * 0.1f),
                                    Color.Transparent,
                                ),
                            ),
                        )
                        .clip(RoundedCornerShape(QodeCorners.md)),
                )
            }

            Column {
                // Header with icon and badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Feature icon
                        val displayIcon = icon ?: when (type) {
                            ComingSoonType.Feature -> Icons.Default.Upcoming
                            ComingSoonType.Premium -> Icons.Default.Star
                            ComingSoonType.InDevelopment -> Icons.Default.Construction
                            ComingSoonType.Locked -> Icons.Default.Lock
                        }

                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(QodeCorners.sm),
                            color = containerColor,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = displayIcon,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(QodeSpacing.sm))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            estimatedDate?.let {
                                Text(
                                    text = "Expected: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Status badge
                    QodeBadge(
                        text = when (type) {
                            ComingSoonType.Feature -> "COMING SOON"
                            ComingSoonType.Premium -> "PREMIUM"
                            ComingSoonType.InDevelopment -> "IN PROGRESS"
                            ComingSoonType.Locked -> "LOCKED"
                        },
                        containerColor = containerColor,
                        contentColor = contentColor,
                    )
                }

                Spacer(modifier = Modifier.height(QodeSpacing.md))

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Action button for notifications
                if (onNotifyMeClick != null && type != ComingSoonType.Locked) {
                    Spacer(modifier = Modifier.height(QodeSpacing.md))
                    QodeButton(
                        onClick = onNotifyMeClick,
                        text = when (type) {
                            ComingSoonType.Premium -> "Learn More"
                            else -> "Notify Me"
                        },
                        variant = QodeButtonVariant.Outlined,
                        leadingIcon = if (type != ComingSoonType.Premium) Icons.Default.Notifications else null,
                    )
                }
            }
        }
    }
}

/**
 * Compact coming soon indicator for list items
 */
@Composable
fun ComingSoonBadge(
    modifier: Modifier = Modifier,
    type: ComingSoonType = ComingSoonType.Feature
) {
    val containerColor = when (type) {
        ComingSoonType.Feature -> MaterialTheme.colorScheme.surfaceVariant
        ComingSoonType.Premium -> Color(0xFFFFD700) // Gold color
        ComingSoonType.InDevelopment -> MaterialTheme.colorScheme.primaryContainer
        ComingSoonType.Locked -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (type) {
        ComingSoonType.Feature -> MaterialTheme.colorScheme.onSurfaceVariant
        ComingSoonType.Premium -> Color.Black
        ComingSoonType.InDevelopment -> MaterialTheme.colorScheme.onPrimaryContainer
        ComingSoonType.Locked -> MaterialTheme.colorScheme.onErrorContainer
    }

    QodeBadge(
        text = when (type) {
            ComingSoonType.Feature -> "SOON"
            ComingSoonType.Premium -> "PRO"
            ComingSoonType.InDevelopment -> "BETA"
            ComingSoonType.Locked -> "LOCKED"
        },
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}

/**
 * Feature roadmap component
 */
@Composable
fun FeatureRoadmap(
    features: List<RoadmapItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Coming Soon",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(QodeSpacing.md),
        )

        features.forEach { item ->
            RoadmapItemCard(
                item = item,
                modifier = Modifier.padding(
                    horizontal = QodeSpacing.md,
                    vertical = QodeSpacing.xs,
                ),
            )
        }
    }
}

@Composable
private fun RoadmapItemCard(
    item: RoadmapItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(QodeCorners.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QodeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Progress indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (item.isInProgress) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(6.dp),
                    ),
            )

            Spacer(modifier = Modifier.width(QodeSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item.estimatedQuarter?.let { quarter ->
                Text(
                    text = quarter,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Data class for roadmap items
 */
data class RoadmapItem(val title: String, val description: String, val estimatedQuarter: String? = null, val isInProgress: Boolean = false)

// Sample roadmap data
private fun getSampleRoadmap(): List<RoadmapItem> =
    listOf(
        RoadmapItem(
            title = "Push Notifications",
            description = "Get notified when new codes are added for your favorite stores",
            estimatedQuarter = "Q1 2025",
            isInProgress = true,
        ),
        RoadmapItem(
            title = "Favorites System",
            description = "Save and organize your favorite promo codes",
            estimatedQuarter = "Q1 2025",
            isInProgress = true,
        ),
        RoadmapItem(
            title = "Family Subscriptions",
            description = "Share premium features with family members",
            estimatedQuarter = "Q2 2025",
        ),
        RoadmapItem(
            title = "AI Recommendations",
            description = "Smart suggestions based on your shopping habits",
            estimatedQuarter = "Q2 2025",
        ),
        RoadmapItem(
            title = "Merchant Dashboard",
            description = "Tools for stores to manage their own promo codes",
            estimatedQuarter = "Q3 2025",
        ),
    )

// Preview
@Preview(name = "ComingSoon Components", showBackground = true)
@Composable
private fun ComingSoonPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            ComingSoonCard(
                title = "Push Notifications",
                description = "Get instant alerts when new promo codes are added for your favorite stores and categories.",
                type = ComingSoonType.InDevelopment,
                icon = Icons.Default.Notifications,
                estimatedDate = "February 2025",
                onNotifyMeClick = {},
            )

            ComingSoonCard(
                title = "Family Subscriptions",
                description = "Share premium features with up to 6 family members. Get exclusive early access to deals.",
                type = ComingSoonType.Premium,
                icon = Icons.Default.Star,
                estimatedDate = "Q2 2025",
                onNotifyMeClick = {},
            )

            ComingSoonCard(
                title = "Advanced Analytics",
                description = "This feature requires a premium subscription to access detailed usage statistics.",
                type = ComingSoonType.Locked,
                icon = Icons.Default.Timeline,
            )

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                ComingSoonBadge(type = ComingSoonType.Feature)
                ComingSoonBadge(type = ComingSoonType.Premium)
                ComingSoonBadge(type = ComingSoonType.InDevelopment)
                ComingSoonBadge(type = ComingSoonType.Locked)
            }
        }
    }
}

@Preview(name = "Feature Roadmap", showBackground = true)
@Composable
private fun FeatureRoadmapPreview() {
    QodeTheme {
        FeatureRoadmap(
            features = getSampleRoadmap(),
        )
    }
}
