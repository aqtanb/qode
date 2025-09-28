package com.qodein.feature.feed.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Tag

/**
 * Enhanced filters component for Feed screen
 * Shows active filters and suggested tags with modern design
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedFilters(
    selectedTags: List<Tag>,
    suggestedTags: List<Tag>,
    onTagSelected: (Tag) -> Unit,
    onTagRemoved: (Tag) -> Unit,
    onClearAllTags: () -> Unit,
    modifier: Modifier = Modifier,
    showSuggested: Boolean = true
) {
    Column(modifier = modifier) {
        // Active filters section
        if (selectedTags.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.md),
                shape = RoundedCornerShape(ShapeTokens.Corner.large),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(SpacingTokens.sm)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    ),
                            )
                            Text(
                                text = "Active filters (${selectedTags.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Surface(
                            onClick = onClearAllTags,
                            shape = RoundedCornerShape(ShapeTokens.Corner.large),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = "Clear all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(
                                    horizontal = SpacingTokens.sm,
                                    vertical = SpacingTokens.xs,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SpacingTokens.sm))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        selectedTags.forEach { tag ->
                            ActiveFilterTag(
                                tag = tag,
                                onRemove = { onTagRemoved(tag) },
                            )
                        }
                    }
                }
            }
        }

        // Suggested tags section
        if (showSuggested && suggestedTags.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = "Trending tags",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    suggestedTags.take(8).forEach { tag ->
                        SuggestedTag(
                            tag = tag,
                            onClick = { onTagSelected(tag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterTag(
    tag: Tag,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {},
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(
                start = SpacingTokens.lg,
                end = SpacingTokens.sm,
                top = SpacingTokens.sm,
                bottom = SpacingTokens.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                modifier = Modifier.size(20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedTag(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = SpacingTokens.lg,
                vertical = SpacingTokens.sm + 2.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                    ),
            )

            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

// MARK: - Previews

@Preview(name = "Feed Filters - With Active Tags", showBackground = true)
@Composable
private fun FeedFiltersWithActiveTagsPreview() {
    QodeTheme {
        FeedFilters(
            selectedTags = listOf(
                Tag.create("streaming", "#FF6B6B"),
                Tag.create("gaming", "#96CEB4"),
                Tag.create("fitness", "#45B7D1"),
            ),
            suggestedTags = listOf(
                Tag.create("music", "#4ECDC4"),
                Tag.create("food", "#FFEAA7"),
                Tag.create("mobile", "#DDA0DD"),
                Tag.create("education", "#98D8C8"),
            ),
            onTagSelected = {},
            onTagRemoved = {},
            onClearAllTags = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Feed Filters - Suggested Only", showBackground = true)
@Composable
private fun FeedFiltersSuggestedOnlyPreview() {
    QodeTheme {
        FeedFilters(
            selectedTags = emptyList(),
            suggestedTags = listOf(
                Tag.create("streaming", "#FF6B6B"),
                Tag.create("music", "#4ECDC4"),
                Tag.create("fitness", "#45B7D1"),
                Tag.create("gaming", "#96CEB4"),
                Tag.create("food", "#FFEAA7"),
                Tag.create("mobile", "#DDA0DD"),
            ),
            onTagSelected = {},
            onTagRemoved = {},
            onClearAllTags = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Feed Filters - No Suggested", showBackground = true)
@Composable
private fun FeedFiltersNoSuggestedPreview() {
    QodeTheme {
        FeedFilters(
            selectedTags = listOf(
                Tag.create("streaming", "#FF6B6B"),
            ),
            suggestedTags = emptyList(),
            onTagSelected = {},
            onTagRemoved = {},
            onClearAllTags = {},
            showSuggested = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
