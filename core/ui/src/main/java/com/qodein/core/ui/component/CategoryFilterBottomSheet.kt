package com.qodein.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.CategoryFilter
import com.qodein.shared.model.Service

/**
 * Category filter bottom sheet component
 * Beautiful category selector with gradient backgrounds, icons, and smooth animations
 */
@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun CategoryFilterBottomSheet(
    isVisible: Boolean,
    currentFilter: CategoryFilter,
    onFilterSelected: (CategoryFilter) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    // Filter out UNSPECIFIED and sort alphabetically
    val availableCategories = Service.Companion.Categories.ALL
        .filter { it != Service.Companion.Categories.UNSPECIFIED }
        .sortedBy { it }

    SharedFilterBottomSheet(
        isVisible = isVisible,
        title = stringResource(R.string.filter_category_title),
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // All Categories option with outlined style
            FilterChip(
                selected = currentFilter is CategoryFilter.All,
                onClick = {
                    onFilterSelected(CategoryFilter.All)
                    onDismiss()
                },
                label = {
                    Text(
                        text = stringResource(R.string.filter_category_all),
                        fontWeight = if (currentFilter is CategoryFilter.All) FontWeight.SemiBold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter is CategoryFilter.All,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 2.dp,
                ),
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )

            // Category grid with gradient chips - smaller size
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                for (category in availableCategories) {
                    val isSelected = currentFilter is CategoryFilter.Selected && currentFilter.contains(category)
                    val gradient = CategoryIconHelper.getCategoryGradient(category)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newFilter = when (currentFilter) {
                                CategoryFilter.All -> CategoryFilter.Selected(setOf(category))
                                is CategoryFilter.Selected -> currentFilter.toggle(category)
                            }
                            onFilterSelected(newFilter)
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getCategoryIcon(category),
                                    contentDescription = category,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(SpacingTokens.xs))
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        modifier = Modifier.height(36.dp),
                        colors = if (isSelected) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Transparent,
                                selectedLabelColor = Color.White,
                            )
                        } else {
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        border = if (isSelected) {
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = true,
                                selectedBorderColor = Color.Transparent,
                            )
                        } else {
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            )
                        },
                    )
                }
            }

            // Close button with outlined style
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.action_close),
                    modifier = Modifier.padding(SpacingTokens.sm),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }

            // Bottom spacing for gesture area
            Spacer(modifier = Modifier.height(SpacingTokens.lg))
        }
    }
}

/**
 * Simple category chip with bright gradients for better visibility
 */
@Composable
private fun GradientCategoryChip(
    category: String,
    isSelected: Boolean,
    gradient: QodeColorScheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple bright gradients for category groups
    val categoryGradient = CategoryIconHelper.getCategoryGradientColors(category)

    val backgroundColor = if (isSelected) {
        Brush.linearGradient(categoryGradient)
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }

    val contentColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(56.dp),
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        colors = CardDefaults.cardColors(Color.Transparent),
        border = if (!isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(
                    horizontal = SpacingTokens.md,
                    vertical = SpacingTokens.sm,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Category icon with circular background
                Box(
                    modifier = Modifier
                        .size(SizeTokens.Avatar.sizeSmall)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Color.White.copy(alpha = 0.2f)
                            } else {
                                Color.Transparent
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CategoryIconHelper.getCategoryIcon(category),
                        contentDescription = category,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(SpacingTokens.sm))

                // Category text
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor,
                )
            }
        }
    }
}

/**
 * Enhanced filter chip for "All Categories" option
 */
@Composable
private fun EnhancedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: @Composable (() -> Unit)?,
    gradient: QodeColorScheme?,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                icon?.invoke()
                if (icon != null) {
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
                }
                Text(
                    text = text,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        modifier = modifier.height(48.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = Color.Transparent,
        ),
    )
}

// MARK: - Previews

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview(name = "Category Filter - All Selected", showBackground = true)
@Composable
private fun CategoryFilterBottomSheetAllSelectedPreview() {
    QodeTheme {
        Surface {
            CategoryFilterBottomSheet(
                isVisible = true,
                currentFilter = CategoryFilter.All,
                onFilterSelected = { },
                onDismiss = { },
                sheetState = rememberModalBottomSheetState(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Preview(name = "Category Filter - Multiple Selected", showBackground = true)
@Composable
private fun CategoryFilterBottomSheetMultipleSelectedPreview() {
    QodeTheme {
        Surface {
            CategoryFilterBottomSheet(
                isVisible = true,
                currentFilter = CategoryFilter.Selected(
                    setOf(
                        Service.Companion.Categories.FOOD,
                        Service.Companion.Categories.STREAMING,
                        Service.Companion.Categories.SHOPPING,
                    ),
                ),
                onFilterSelected = { },
                onDismiss = { },
                sheetState = rememberModalBottomSheetState(),
            )
        }
    }
}

@Preview(name = "Gradient Category Chip - Selected", showBackground = true)
@Composable
private fun GradientCategoryChipSelectedPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier.padding(SpacingTokens.lg),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                GradientCategoryChip(
                    category = Service.Companion.Categories.FOOD,
                    isSelected = true,
                    gradient = CategoryIconHelper.getCategoryGradient(Service.Companion.Categories.FOOD),
                    onClick = { },
                )

                GradientCategoryChip(
                    category = Service.Companion.Categories.STREAMING,
                    isSelected = false,
                    gradient = CategoryIconHelper.getCategoryGradient(Service.Companion.Categories.STREAMING),
                    onClick = { },
                )

                GradientCategoryChip(
                    category = Service.Companion.Categories.GAMING,
                    isSelected = true,
                    gradient = CategoryIconHelper.getCategoryGradient(Service.Companion.Categories.GAMING),
                    onClick = { },
                )
            }
        }
    }
}
