package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeChip
import com.qodein.core.designsystem.component.QodeChipSize
import com.qodein.core.designsystem.component.QodeChipVariant
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.Category

data class FilterItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val isSelected: Boolean = false,
    val count: Int? = null
)

@Composable
fun CategoryFilter(
    filters: List<FilterItem>,
    onFilterClick: (FilterItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Column(modifier = modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = QodeSpacing.md, vertical = QodeSpacing.sm),
            )
        }

        LazyRow(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(horizontal = QodeSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(filters) { filter ->
                val labelText = if (filter.count != null && filter.count > 0) {
                    "${filter.label} (${filter.count})"
                } else {
                    filter.label
                }

                QodeChip(
                    label = labelText,
                    onClick = { onFilterClick(filter) },
                    selected = filter.isSelected,
                    variant = QodeChipVariant.Filter,
                    size = QodeChipSize.Medium,
                    leadingIcon = filter.icon,
                )
            }
        }
    }
}

@Composable
fun QuickFilters(
    selectedFilter: String,
    onFilterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickFilters = listOf(
        FilterItem(id = "all", label = "All", isSelected = selectedFilter == "all"),
        FilterItem(id = "popular", label = "Popular", icon = Icons.Default.Star, isSelected = selectedFilter == "popular"),
        FilterItem(id = "new", label = "New", icon = Icons.Default.Whatshot, isSelected = selectedFilter == "new"),
        FilterItem(
            id = "trending",
            label = "Trending",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            isSelected = selectedFilter == "trending",
        ),
        FilterItem(id = "verified", label = "Verified", icon = Icons.Default.Star, isSelected = selectedFilter == "verified"),
        FilterItem(id = "expiring", label = "Expiring Soon", isSelected = selectedFilter == "expiring"),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = QodeSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(quickFilters) { filter ->
            QodeChip(
                label = filter.label,
                onClick = { onFilterClick(filter.id) },
                selected = filter.isSelected,
                variant = QodeChipVariant.Filter,
                size = QodeChipSize.Medium,
                leadingIcon = filter.icon,
            )
        }
    }
}

@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategoryClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
    showAllOption: Boolean = true
) {
    val filters = mutableListOf<FilterItem>()

    if (showAllOption) {
        filters.add(
            FilterItem(
                id = "all",
                label = "All Categories",
                icon = Icons.Default.Category,
                isSelected = selectedCategoryId == null,
            ),
        )
    }

    categories.forEach { category ->
        filters.add(
            FilterItem(
                id = category.id,
                label = category.name,
                icon = category.icon,
                isSelected = selectedCategoryId == category.id,
                count = if (category.followersCount > 0) category.followersCount else null,
            ),
        )
    }

    CategoryFilter(
        filters = filters,
        onFilterClick = { filter ->
            if (filter.id == "all") {
                onCategoryClick(null)
            } else {
                onCategoryClick(filter.id)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CombinedFilters(
    quickFilter: String,
    selectedCategoryId: String?,
    categories: List<Category>,
    onQuickFilterClick: (String) -> Unit,
    onCategoryClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        QuickFilters(
            selectedFilter = quickFilter,
            onFilterClick = onQuickFilterClick,
        )

        if (categories.isNotEmpty()) {
            CategoryFilters(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.padding(top = QodeSpacing.sm),
            )
        }
    }
}

private fun getSampleCategories(): List<Category> =
    listOf(
        Category(id = "electronics", name = "Electronics", icon = Icons.Default.ElectricBolt, followersCount = 2500),
        Category(id = "fashion", name = "Fashion", icon = Icons.Default.Face, followersCount = 1800),
        Category(id = "food", name = "Food & Drinks", icon = Icons.Default.LocalDining, followersCount = 3200),
        Category(id = "beauty", name = "Beauty", icon = Icons.Default.Face, followersCount = 950),
        Category(id = "sports", name = "Sports", icon = Icons.Default.FitnessCenter, followersCount = 650),
        Category(id = "home", name = "Home & Garden", icon = Icons.Default.Home, followersCount = 420),
        Category(id = "books", name = "Books", icon = Icons.AutoMirrored.Filled.MenuBook, followersCount = 280),
    )

@Preview(name = "CategoryFilter Components", showBackground = true)
@Composable
private fun CategoryFilterPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(vertical = QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            Text(
                "Quick Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = QodeSpacing.md),
            )
            QuickFilters(
                selectedFilter = "popular",
                onFilterClick = {},
            )

            Text(
                "Category Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = QodeSpacing.md),
            )
            CategoryFilters(
                categories = getSampleCategories(),
                selectedCategoryId = "electronics",
                onCategoryClick = {},
            )

            Text(
                "Combined Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = QodeSpacing.md),
            )
            CombinedFilters(
                quickFilter = "new",
                selectedCategoryId = "food",
                categories = getSampleCategories(),
                onQuickFilterClick = {},
                onCategoryClick = {},
            )
        }
    }
}
