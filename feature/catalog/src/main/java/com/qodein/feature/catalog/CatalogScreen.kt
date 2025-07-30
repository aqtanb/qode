package com.qodein.feature.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.component.QodeErrorState
import com.qodein.core.designsystem.component.QodeLoadingContent
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.component.CombinedFilters
import com.qodein.core.ui.component.PromoCodeList
import com.qodein.core.ui.component.PromoCodeListState
import com.qodein.core.ui.component.SearchHeader
import com.qodein.core.ui.component.SearchSuggestions
import com.qodein.core.ui.component.SortOption
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = hiltViewModel(),
    onPromoCodeClick: (PromoCode) -> Unit = {},
    onStoreClick: (Store) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    showTopBar: Boolean = false // Control whether to show the top bar
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.handleAction(CatalogAction.SnackbarDismissed)
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.errorMessages) {
        uiState.errorMessages.forEach { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.handleAction(CatalogAction.ErrorDismissed(error.id))
        }
    }

    if (showTopBar) {
        // Use scaffold with top bar for standalone usage
        Scaffold(
            topBar = {
                CatalogTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.handleAction(CatalogAction.SearchQueryChanged(query))
                    },
                    onSearchSubmit = {
                        viewModel.handleAction(CatalogAction.SearchSubmitted)
                    },
                    onMenuClick = {
                        viewModel.handleAction(CatalogAction.MenuClicked)
                    },
                    selectedSort = uiState.sortOption,
                    onSortChange = { sortOption ->
                        viewModel.handleAction(CatalogAction.SortOptionSelected(sortOption))
                    },
                    hasActiveFilters = uiState.hasActiveFilters,
                    onClearFilters = {
                        viewModel.handleAction(CatalogAction.FiltersCleared)
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            modifier = modifier,
        ) { innerPadding ->
            CatalogContent(
                uiState = uiState,
                onAction = viewModel::handleAction,
                onPromoCodeClick = onPromoCodeClick,
                onStoreClick = onStoreClick,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        // Content only for use within app navigation (app already has top bar)
        CatalogContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            onPromoCodeClick = onPromoCodeClick,
            onStoreClick = onStoreClick,
            onCategoryClick = onCategoryClick,
            modifier = modifier,
            showLocalSearch = true, // Show search within content area
        )
    }
}

/**
 * Top app bar for the catalog screen with search functionality
 */
@Composable
private fun CatalogTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onMenuClick: () -> Unit,
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchHeader(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onMenuClick = onMenuClick,
        onSearchSubmit = onSearchSubmit,
        placeholder = "Search promo codes, stores...",
        showFilters = true,
        selectedSort = selectedSort,
        onSortChange = onSortChange,
        hasActiveFilters = hasActiveFilters,
        onClearFilters = onClearFilters,
        modifier = modifier,
    )
}

/**
 * Main content area of the catalog screen
 */
@Composable
private fun CatalogContent(
    uiState: CatalogUiState,
    onAction: (CatalogAction) -> Unit,
    onPromoCodeClick: (PromoCode) -> Unit,
    onStoreClick: (Store) -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
    showLocalSearch: Boolean = false
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Local search bar (only when app doesn't have global search)
        if (showLocalSearch) {
            LocalSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query ->
                    onAction(CatalogAction.SearchQueryChanged(query))
                },
                onSearchSubmit = {
                    onAction(CatalogAction.SearchSubmitted)
                },
                selectedSort = uiState.sortOption,
                onSortChange = { sortOption ->
                    onAction(CatalogAction.SortOptionSelected(sortOption))
                },
                hasActiveFilters = uiState.hasActiveFilters,
                onClearFilters = {
                    onAction(CatalogAction.FiltersCleared)
                },
                modifier = Modifier.padding(QodeSpacing.md),
            )
        }

        // Search suggestions overlay
        AnimatedVisibility(
            visible = uiState.showSearchSuggestions && uiState.searchSuggestions.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            SearchSuggestions(
                suggestions = uiState.searchSuggestions,
                onSuggestionClick = { suggestion ->
                    onAction(CatalogAction.SearchSuggestionSelected(suggestion))
                },
            )
        }

        // Filters section
        AnimatedVisibility(
            visible = !uiState.showSearchSuggestions,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            CatalogFilters(
                quickFilter = uiState.quickFilter,
                selectedCategoryId = uiState.selectedCategoryId,
                categories = uiState.categories,
                onQuickFilterClick = { filterId ->
                    onAction(CatalogAction.QuickFilterSelected(filterId))
                },
                onCategoryClick = { categoryId ->
                    onAction(CatalogAction.CategoryFilterSelected(categoryId))
                },
            )
        }

        // Filter summary
        if (uiState.hasActiveFilters && !uiState.showSearchSuggestions) {
            FilterSummary(
                filterSummary = uiState.getFilterSummary(),
                activeFiltersCount = uiState.getActiveFiltersCount(),
                modifier = Modifier.padding(horizontal = QodeSpacing.md, vertical = QodeSpacing.sm),
            )
        }

        // Main content based on state
        when {
            uiState.isLoading -> {
                CatalogLoadingContent()
            }

            uiState.isError -> {
                CatalogErrorContent(
                    message = (uiState.promoCodesState as? PromoCodeListState.Error)?.message ?: "An error occurred",
                    onRetry = { onAction(CatalogAction.RetryLoad) },
                )
            }

            uiState.isEmpty -> {
                CatalogEmptyContent(
                    hasFilters = uiState.hasActiveFilters,
                    onClearFilters = { onAction(CatalogAction.FiltersCleared) },
                )
            }

            else -> {
                CatalogPromoCodesList(
                    uiState = uiState,
                    onAction = onAction,
                    onPromoCodeClick = onPromoCodeClick,
                    onStoreClick = onStoreClick,
                )
            }
        }
    }
}

/**
 * Filters section with quick filters and category filters
 */
@Composable
private fun CatalogFilters(
    quickFilter: String,
    selectedCategoryId: String?,
    categories: List<Category>,
    onQuickFilterClick: (String) -> Unit,
    onCategoryClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    CombinedFilters(
        quickFilter = quickFilter,
        selectedCategoryId = selectedCategoryId,
        categories = categories,
        onQuickFilterClick = onQuickFilterClick,
        onCategoryClick = onCategoryClick,
        modifier = modifier,
    )
}

/**
 * Filter summary showing active filters
 */
@Composable
private fun FilterSummary(
    filterSummary: String,
    activeFiltersCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Showing results for:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = filterSummary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        if (activeFiltersCount > 1) {
            Text(
                text = "$activeFiltersCount filters applied",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Loading state content
 */
@Composable
private fun CatalogLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeLoadingContent(message = "Loading promo codes...")
    }
}

/**
 * Error state content
 */
@Composable
private fun CatalogErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeErrorState(
            message = message,
            onRetry = onRetry,
        )
    }
}

/**
 * Empty state content
 */
@Composable
private fun CatalogEmptyContent(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeEmptyState(
            icon = Icons.Default.Search,
            title = if (hasFilters) "No results found" else "No promo codes available",
            description = if (hasFilters) {
                "Try adjusting your search or filters to find what you're looking for"
            } else {
                "Check back later for new promo codes"
            },
            action = if (hasFilters) {
                {
                    androidx.compose.material3.TextButton(onClick = onClearFilters) {
                        Text("Clear Filters")
                    }
                }
            } else {
                null
            },
        )
    }
}

/**
 * Promo codes list content
 */
@Composable
private fun CatalogPromoCodesList(
    uiState: CatalogUiState,
    onAction: (CatalogAction) -> Unit,
    onPromoCodeClick: (PromoCode) -> Unit,
    onStoreClick: (Store) -> Unit,
    modifier: Modifier = Modifier
) {
    PromoCodeList(
        state = uiState.promoCodesState,
        onPromoCodeClick = { promoCode ->
            onAction(CatalogAction.PromoCodeClicked(promoCode))
            onPromoCodeClick(promoCode)
        },
        onUpvoteClick = { promoCode ->
            onAction(CatalogAction.PromoCodeUpvoted(promoCode))
        },
        onFollowStoreClick = { store ->
            onAction(CatalogAction.StoreFollowToggled(store))
            onStoreClick(store)
        },
        onCopyCodeClick = { promoCode ->
            onAction(CatalogAction.PromoCodeCopied(promoCode))
        },
        onRefresh = {
            onAction(CatalogAction.Refresh)
        },
        onLoadMore = {
            onAction(CatalogAction.LoadMore)
        },
        isLoggedIn = uiState.isLoggedIn,
        contentPadding = PaddingValues(QodeSpacing.md),
        isRefreshing = uiState.isRefreshing,
        hasMoreItems = uiState.hasMoreItems,
        isLoadingMore = uiState.isLoadingMore,
        modifier = modifier,
    )
}

/**
 * Local search bar for when catalog is used within app navigation
 */
@Composable
private fun LocalSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Search field
        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search promo codes...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            modifier = Modifier.weight(1f),
        )

        // Sort button
        var showSortMenu by remember { mutableStateOf(false) }

        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort options",
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        },
                        leadingIcon = if (selectedSort == option) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        // Clear filters button (only show if filters are active)
        if (hasActiveFilters) {
            IconButton(onClick = onClearFilters) {
                Icon(
                    imageVector = Icons.Default.FilterAltOff,
                    contentDescription = "Clear filters",
                )
            }
        }
    }
}

@Composable
private fun CatalogScreenPreview() {
    QodeTheme {
        // Mock preview state
        val mockUiState = CatalogUiState(
            promoCodesState = PromoCodeListState.Success(
                com.qodein.core.ui.preview.PreviewData.samplePromoCodes.take(3),
            ),
            categories = com.qodein.core.ui.preview.PreviewData.sampleCategories,
            stores = com.qodein.core.ui.preview.PreviewData.sampleStores,
            searchQuery = "",
            isLoggedIn = true,
            hasActiveFilters = false,
        )

        CatalogContent(
            uiState = mockUiState,
            onAction = {},
            onPromoCodeClick = {},
            onStoreClick = {},
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Catalog Screen - Empty", showBackground = true)
@Composable
private fun CatalogScreenEmptyPreview() {
    QodeTheme {
        val mockUiState = CatalogUiState(
            promoCodesState = PromoCodeListState.Empty,
            hasActiveFilters = true,
        )

        CatalogContent(
            uiState = mockUiState,
            onAction = {},
            onPromoCodeClick = {},
            onStoreClick = {},
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Catalog Screen - Loading", showBackground = true)
@Composable
private fun CatalogScreenLoadingPreview() {
    QodeTheme {
        val mockUiState = CatalogUiState(
            isLoading = true,
            promoCodesState = PromoCodeListState.Loading,
        )

        CatalogContent(
            uiState = mockUiState,
            onAction = {},
            onPromoCodeClick = {},
            onStoreClick = {},
            onCategoryClick = {},
        )
    }
}
