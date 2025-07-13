package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.theme.QodeElevation
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Sort options for promo codes
 */
enum class SortOption(val label: String) {
    Recent("Most Recent"),
    Popular("Most Popular"),
    ExpiringFirst("Expiring First"),
    DiscountAmount("Highest Discount"),
    StoreAZ("Store A-Z")
}

/**
 * SearchHeader component with search functionality and filters
 *
 * @param searchQuery Current search query
 * @param onSearchQueryChange Called when search query changes
 * @param onMenuClick Called when menu button is clicked
 * @param onSearchSubmit Called when search is submitted
 * @param modifier Modifier to be applied to the component
 * @param placeholder Placeholder text for search field
 * @param showFilters Whether to show filter options
 * @param selectedSort Current sort option
 * @param onSortChange Called when sort option changes
 * @param hasActiveFilters Whether there are active filters
 * @param onClearFilters Called when clear filters is clicked
 */
@Composable
fun SearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search promo codes...",
    showFilters: Boolean = true,
    selectedSort: SortOption = SortOption.Recent,
    onSortChange: (SortOption) -> Unit = {},
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {}
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = QodeElevation.xs,
    ) {
        Column(
            modifier = Modifier.padding(QodeSpacing.md),
        ) {
            // Main search row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                // Menu button
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open menu",
                    )
                }

                // Search field
                QodeTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    variant = QodeTextFieldVariant.Search,
                    placeholder = placeholder,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchSubmit() },
                    ),
                )

                // Filter/Sort controls
                if (showFilters) {
                    // Sort button
                    IconButton(
                        onClick = { showSortMenu = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort options",
                        )
                    }

                    // Sort dropdown menu
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (selectedSort == option) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                },
                                onClick = {
                                    onSortChange(option)
                                    showSortMenu = false
                                },
                            )
                        }
                    }
                }
            }

            // Active filters indicator
            AnimatedVisibility(
                visible = hasActiveFilters,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QodeSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(QodeSpacing.xs))
                        Text(
                            text = "Filters active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    IconButton(
                        onClick = onClearFilters,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear filters",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact search header for smaller screens or specific use cases
 */
@Composable
fun CompactSearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = QodeElevation.xs,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QodeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        ) {
            onBackClick?.let { backClick ->
                IconButton(onClick = backClick) {
                    Icon(
                        imageVector = Icons.Default.Clear, // TODO: Use back arrow icon
                        contentDescription = "Go back",
                    )
                }
            }

            QodeTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                variant = QodeTextFieldVariant.Search,
                placeholder = placeholder,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearchSubmit() },
                ),
            )
        }
    }
}

/**
 * Search suggestions component
 */
@Composable
fun SearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { onSuggestionClick(suggestion) },
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

// Preview
@Preview(name = "SearchHeader", showBackground = true)
@Composable
private fun SearchHeaderPreview() {
    QodeTheme {
        var searchQuery by remember { mutableStateOf("") }
        var sortOption by remember { mutableStateOf(SortOption.Recent) }
        var hasActiveFilters by remember { mutableStateOf(false) }

        Column {
            SearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onMenuClick = {},
                onSearchSubmit = {},
                selectedSort = sortOption,
                onSortChange = { sortOption = it },
                hasActiveFilters = hasActiveFilters,
                onClearFilters = { hasActiveFilters = false },
            )

            Spacer(modifier = Modifier.height(QodeSpacing.md))

            // Toggle button for testing
            androidx.compose.material3.Button(
                onClick = { hasActiveFilters = !hasActiveFilters },
                modifier = Modifier.padding(QodeSpacing.md),
            ) {
                Text("Toggle Active Filters")
            }
        }
    }
}

@Preview(name = "Compact SearchHeader", showBackground = true)
@Composable
private fun CompactSearchHeaderPreview() {
    QodeTheme {
        var searchQuery by remember { mutableStateOf("electronics") }

        Column {
            CompactSearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchSubmit = {},
                onBackClick = {},
            )

            SearchSuggestions(
                suggestions = listOf(
                    "electronics deals",
                    "kaspi bank promo",
                    "fashion discounts",
                    "food delivery codes",
                ),
                onSuggestionClick = { searchQuery = it },
            )
        }
    }
}
