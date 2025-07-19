// core/ui/src/main/kotlin/com/qodein/core/ui/component/StatsCard.kt
package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Data class for stat information
 */
data class StatData(
    val value: String,
    val label: String,
    val change: String? = null,
    val changeType: ChangeType = ChangeType.Neutral,
    val icon: ImageVector? = null,
    val color: Color? = null
)

/**
 * Change type for statistics
 */
enum class ChangeType {
    Positive,
    Negative,
    Neutral
}

/**
 * Stat card size variants
 */
enum class StatCardSize {
    Small,
    Medium,
    Large
}

/**
 * Simple stat card component
 *
 * @param statData The statistic data to display
 * @param onClick Optional click handler
 * @param modifier Modifier to be applied to the card
 * @param size Size variant of the card
 * @param variant Visual variant of the card
 */
@Composable
fun StatsCard(
    statData: StatData,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: StatCardSize = StatCardSize.Medium,
    variant: QodeCardVariant = QodeCardVariant.Elevated
) {
    QodeCard(
        modifier = modifier,
        variant = variant,
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = when (size) {
                StatCardSize.Small -> Alignment.CenterHorizontally
                else -> Alignment.Start
            },
        ) {
            // Icon and value row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (size == StatCardSize.Small) {
                    Arrangement.Center
                } else {
                    Arrangement.SpaceBetween
                },
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    horizontalAlignment = if (size == StatCardSize.Small) {
                        Alignment.CenterHorizontally
                    } else {
                        Alignment.Start
                    },
                ) {
                    Text(
                        text = statData.value,
                        style = when (size) {
                            StatCardSize.Small -> MaterialTheme.typography.titleMedium
                            StatCardSize.Medium -> MaterialTheme.typography.headlineSmall
                            StatCardSize.Large -> MaterialTheme.typography.headlineMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = statData.color ?: MaterialTheme.colorScheme.onSurface,
                        textAlign = if (size == StatCardSize.Small) TextAlign.Center else TextAlign.Start,
                    )

                    Text(
                        text = statData.label,
                        style = when (size) {
                            StatCardSize.Small -> MaterialTheme.typography.bodySmall
                            else -> MaterialTheme.typography.bodyMedium
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = if (size == StatCardSize.Small) TextAlign.Center else TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Icon
                if (size != StatCardSize.Small && statData.icon != null) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(QodeCorners.sm),
                        color = (statData.color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = statData.icon,
                                contentDescription = null,
                                tint = statData.color ?: MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // Change indicator
            statData.change?.let { change ->
                Spacer(modifier = Modifier.height(QodeSpacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (size == StatCardSize.Small) {
                        Arrangement.Center
                    } else {
                        Arrangement.Start
                    },
                ) {
                    val changeColor = when (statData.changeType) {
                        ChangeType.Positive -> Color(0xFF4CAF50)
                        ChangeType.Negative -> MaterialTheme.colorScheme.error
                        ChangeType.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    val changeIcon = when (statData.changeType) {
                        ChangeType.Positive -> Icons.Default.TrendingUp
                        ChangeType.Negative -> Icons.Default.TrendingDown
                        ChangeType.Neutral -> Icons.Default.Analytics
                    }

                    Icon(
                        imageVector = changeIcon,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(16.dp),
                    )

                    Spacer(modifier = Modifier.width(QodeSpacing.xs))

                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Small icon at bottom for small cards
            if (size == StatCardSize.Small && statData.icon != null) {
                Spacer(modifier = Modifier.height(QodeSpacing.sm))
                Icon(
                    imageVector = statData.icon,
                    contentDescription = null,
                    tint = statData.color ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/**
 * Progress stat card with animated progress bar
 *
 * @param title Title of the stat
 * @param current Current value
 * @param target Target value
 * @param unit Unit of measurement
 * @param icon Optional icon
 * @param color Color for progress bar and icon
 * @param onClick Optional click handler
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun ProgressStatsCard(
    title: String,
    current: Int,
    target: Int,
    unit: String = "",
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = QodeAnimation.SLOW),
        label = "progress_animation",
    )

    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = "$current / $target $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                icon?.let {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(QodeCorners.sm),
                        color = color.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(QodeSpacing.md))

            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = "${target - current} to go",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(QodeSpacing.xs))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
            }
        }
    }
}

/**
 * Comparison stat card showing two values side by side
 *
 * @param title Title of the comparison
 * @param primaryStat Primary statistic
 * @param secondaryStat Secondary statistic
 * @param onClick Optional click handler
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun ComparisonStatsCard(
    title: String,
    primaryStat: StatData,
    secondaryStat: StatData,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onClick,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = QodeSpacing.md),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Primary stat
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = primaryStat.value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryStat.color ?: MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = primaryStat.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    primaryStat.change?.let { change ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = QodeSpacing.xs),
                        ) {
                            val changeColor = when (primaryStat.changeType) {
                                ChangeType.Positive -> Color(0xFF4CAF50)
                                ChangeType.Negative -> MaterialTheme.colorScheme.error
                                ChangeType.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Icon(
                                imageVector = if (primaryStat.changeType == ChangeType.Positive) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = changeColor,
                                modifier = Modifier.size(16.dp),
                            )

                            Text(
                                text = change,
                                style = MaterialTheme.typography.bodySmall,
                                color = changeColor,
                            )
                        }
                    }
                }

                // Secondary stat
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = secondaryStat.value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = secondaryStat.color ?: MaterialTheme.colorScheme.secondary,
                    )

                    Text(
                        text = secondaryStat.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    )

                    secondaryStat.change?.let { change ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = QodeSpacing.xs),
                        ) {
                            val changeColor = when (secondaryStat.changeType) {
                                ChangeType.Positive -> Color(0xFF4CAF50)
                                ChangeType.Negative -> MaterialTheme.colorScheme.error
                                ChangeType.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Icon(
                                imageVector = if (secondaryStat.changeType == ChangeType.Positive) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = changeColor,
                                modifier = Modifier.size(16.dp),
                            )

                            Text(
                                text = change,
                                style = MaterialTheme.typography.bodySmall,
                                color = changeColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grid of multiple stats
 *
 * @param stats List of statistics to display
 * @param columns Number of columns in the grid
 * @param onStatClick Called when a stat is clicked
 * @param modifier Modifier to be applied to the grid
 */
@Composable
fun StatsGrid(
    stats: List<StatData>,
    columns: Int = 2,
    onStatClick: ((StatData) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
    ) {
        stats.chunked(columns).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                rowStats.forEach { stat ->
                    StatsCard(
                        statData = stat,
                        onClick = onStatClick?.let { { it(stat) } },
                        size = StatCardSize.Small,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Fill remaining space if row is not complete
                repeat(columns - rowStats.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Sample data and helper functions
private fun formatNumber(number: Int): String = NumberFormat.getNumberInstance(Locale.US).format(number)

// Preview
@Preview(name = "StatsCard Components", showBackground = true)
@Composable
private fun StatsCardPreview() {
    QodeTheme {
        @Composable
        fun getSampleStats(): List<StatData> =
            listOf(
                StatData(
                    value = "1,245",
                    label = "Total Codes",
                    change = "+15.2% this month",
                    changeType = ChangeType.Positive,
                    icon = QodeCommerceIcons.PromoCode,
                    color = MaterialTheme.colorScheme.primary,
                ),
                StatData(
                    value = "25.8K",
                    label = "Active Users",
                    change = "+8.5% this week",
                    changeType = ChangeType.Positive,
                    icon = Icons.Default.Group,
                    color = Color(0xFF4CAF50),
                ),
                StatData(
                    value = "2.5M ₸",
                    label = "Total Savings",
                    change = "+125K this month",
                    changeType = ChangeType.Positive,
                    icon = Icons.Default.MonetizationOn,
                    color = Color(0xFFFF9800),
                ),
                StatData(
                    value = "94.2%",
                    label = "Verified Codes",
                    change = "-2.1% this month",
                    changeType = ChangeType.Negative,
                    icon = Icons.Default.Star,
                    color = Color(0xFF9C27B0),
                ),
                StatData(
                    value = "156",
                    label = "Stores",
                    change = "+12 this month",
                    changeType = ChangeType.Positive,
                    icon = Icons.Default.Store,
                    color = Color(0xFF2196F3),
                ),
                StatData(
                    value = "8",
                    label = "Categories",
                    icon = Icons.Default.Category,
                    color = Color(0xFF607D8B),
                ),
            )

        Column(
            modifier = Modifier.padding(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            val sampleStats = getSampleStats()

            // Single stat cards
            Text("Individual Stats", style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                StatsCard(
                    statData = sampleStats[0],
                    size = StatCardSize.Medium,
                    modifier = Modifier.weight(1f),
                )

                StatsCard(
                    statData = sampleStats[1],
                    size = StatCardSize.Medium,
                    modifier = Modifier.weight(1f),
                )
            }

            // Progress card
            Text("Progress Stats", style = MaterialTheme.typography.titleMedium)
            ProgressStatsCard(
                title = "Monthly Goal",
                current = 78,
                target = 100,
                unit = "codes",
                icon = QodeCommerceIcons.PromoCode,
                color = MaterialTheme.colorScheme.primary,
            )

            // Comparison card
            Text("Comparison Stats", style = MaterialTheme.typography.titleMedium)
            ComparisonStatsCard(
                title = "This Month vs Last Month",
                primaryStat = StatData(
                    value = "245",
                    label = "This Month",
                    change = "+15.2%",
                    changeType = ChangeType.Positive,
                    color = MaterialTheme.colorScheme.primary,
                ),
                secondaryStat = StatData(
                    value = "213",
                    label = "Last Month",
                    change = "+8.5%",
                    changeType = ChangeType.Positive,
                    color = MaterialTheme.colorScheme.secondary,
                ),
            )

            // Stats grid
            Text("Stats Grid", style = MaterialTheme.typography.titleMedium)
            StatsGrid(
                stats = sampleStats.take(4),
                columns = 2,
            )
        }
    }
}
