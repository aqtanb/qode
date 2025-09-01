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
import androidx.compose.ui.text.style.TextOverflow
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
            // All Categories option with circular outlined style
            OutlinedButton(
                onClick = {
                    onFilterSelected(CategoryFilter.All)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                border = BorderStroke(
                    width = if (currentFilter is CategoryFilter.All) 2.dp else 1.dp,
                    color = if (currentFilter is CategoryFilter.All) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = if (currentFilter is CategoryFilter.All) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            ) {
                Text(
                    text = stringResource(R.string.filter_category_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )

            // Category chips with service-like design
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                for (category in availableCategories) {
                    val isSelected = currentFilter is CategoryFilter.Selected && currentFilter.contains(category)

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
                            Text(
                                text = category,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = CategoryIconHelper.getCategoryIcon(category),
                                contentDescription = category,
                                modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                            )
                        },
                        modifier = Modifier,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
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
    // Default gradient colors
    val categoryGradient = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
    )

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
                    gradient = QodeColorScheme.BannerOrange,
                    onClick = { },
                )

                GradientCategoryChip(
                    category = Service.Companion.Categories.STREAMING,
                    isSelected = false,
                    gradient = QodeColorScheme.BannerPurple,
                    onClick = { },
                )

                GradientCategoryChip(
                    category = Service.Companion.Categories.GAMING,
                    isSelected = true,
                    gradient = QodeColorScheme.BannerPurple,
                    onClick = { },
                )
            }
        }
    }
}
