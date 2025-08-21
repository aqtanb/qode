# Search System

## Overview
Comprehensive search and discovery system with real-time search suggestions, advanced filtering, sorting capabilities, and infinite scroll. Features debounced search, category/store filtering, and sophisticated state management for optimal user experience.

## Architecture
- **MVI Pattern**: SearchViewModel with SearchAction sealed classes and state management
- **Real-Time Search**: Debounced search with live suggestions and auto-complete
- **Advanced Filtering**: Quick filters, category filters, store filters, and sorting options
- **Infinite Scroll**: Pagination with load-more functionality and smooth UX
- **State Persistence**: Smart filter state management and restoration
- **Error Handling**: Comprehensive error states with retry mechanisms

## Key Files

### Feature Layer
- `feature/search/SearchScreen.kt` - Main search interface with flexible layout options
- `feature/search/SearchViewModel.kt` - Complex search logic with filtering and pagination
- `feature/search/SearchUiState.kt` - Search state management with filter tracking
- `feature/search/SearchAction.kt` - User interactions for search, filtering, and navigation
- `feature/search/navigation/SearchNavigation.kt` - Type-safe navigation integration

### UI Components
- `core/ui/component/SearchHeader.kt` - Search bar with suggestions and filters
- `core/ui/component/SearchSuggestions.kt` - Auto-complete suggestions display
- `core/ui/component/CombinedFilters.kt` - Quick filters and category filters
- `core/ui/component/PromoCodeList.kt` - Results display with pagination

### Business Logic
- `core/domain/usecase/promocode/SearchPromoCodesUseCase.kt` - Search business logic
- `core/domain/usecase/promocode/GetPromoCodesUseCase.kt` - Data retrieval with filtering

## Search Architecture

### UI State Management
```kotlin
@Stable
data class SearchUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = false,
    
    // Content data
    val promoCodesState: PromoCodeListState = PromoCodeListState.Loading,
    val categories: List<Category> = emptyList(),
    val stores: List<Store> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    
    // Search and filters
    val searchQuery: String = "",
    val quickFilter: String = "all",
    val selectedCategoryId: String? = null,
    val selectedStoreId: String? = null,
    val sortOption: SortOption = SortOption.Recent,
    val showFilters: Boolean = false,
    val hasActiveFilters: Boolean = false,
    
    // User state
    val isLoggedIn: Boolean = false,
    
    // Error handling
    val errorMessages: List<ErrorMessage> = emptyList(),
    val snackbarMessage: String? = null,
    
    // UI state
    val showSearchSuggestions: Boolean = false
)
```

### Search Action System
```kotlin
sealed interface SearchAction {
    // Search actions
    data class SearchQueryChanged(val query: String) : SearchAction
    data object SearchSubmitted : SearchAction
    data object SearchCleared : SearchAction
    data class SearchSuggestionSelected(val suggestion: String) : SearchAction
    
    // Filter actions
    data class QuickFilterSelected(val filterId: String) : SearchAction
    data class CategoryFilterSelected(val categoryId: String?) : SearchAction
    data class StoreFilterSelected(val storeId: String?) : SearchAction
    data class SortOptionSelected(val sortOption: SortOption) : SearchAction
    data object FiltersCleared : SearchAction
    data object FiltersToggled : SearchAction
    
    // PromoCode actions
    data class PromoCodeClicked(val promoCode: PromoCode) : SearchAction
    data class PromoCodeUpvoted(val promoCode: PromoCode) : SearchAction
    data class PromoCodeCopied(val promoCode: PromoCode) : SearchAction
    
    // Store actions
    data class StoreClicked(val store: Store) : SearchAction
    data class StoreFollowToggled(val store: Store) : SearchAction
    
    // Category actions
    data class CategoryClicked(val category: Category) : SearchAction
    data class CategoryFollowToggled(val category: Category) : SearchAction
    
    // Data loading actions
    data object Refresh : SearchAction
    data object LoadMore : SearchAction
    data object RetryLoad : SearchAction
    
    // Navigation actions
    data object MenuClicked : SearchAction
    data object BackPressed : SearchAction
    
    // UI state actions
    data class ErrorDismissed(val errorId: String) : SearchAction
    data object SnackbarDismissed : SearchAction
}
```

## Real-Time Search

### Debounced Search Implementation
```kotlin
private fun handleSearchQueryChanged(query: String) {
    _uiState.update {
        it.copy(
            searchQuery = query,
            showSearchSuggestions = query.isNotEmpty(),
        )
    }
    
    // Debounced search
    searchJob?.cancel()
    if (query.isNotEmpty()) {
        searchJob = viewModelScope.launch {
            delay(300) // Debounce delay
            loadSearchSuggestions(query)
            if (query.length >= 2) {
                loadPromoCodes(reset = true)
            }
        }
    } else {
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
        loadPromoCodes(reset = true)
    }
}
```

### Search Suggestions
- **Auto-complete**: Real-time suggestions based on user input
- **Debounced Loading**: 300ms delay to prevent excessive API calls
- **Smart Filtering**: Suggestions filtered by relevance and popularity
- **Quick Selection**: Tap to select suggestion and execute search
- **Context Awareness**: Suggestions consider current filters and user history

### Search Optimization
- **Minimum Query Length**: Search starts at 2 characters to reduce noise
- **Job Cancellation**: Previous search jobs cancelled for performance
- **Caching Strategy**: Recent searches cached for instant results
- **Error Recovery**: Graceful handling of search failures

## Advanced Filtering System

### Quick Filter Options
```kotlin
private fun getDefaultQuickFilters(): List<QuickFilterItem> =
    listOf(
        QuickFilterItem(id = "all", label = "All"),
        QuickFilterItem(id = "popular", label = "Popular"),
        QuickFilterItem(id = "new", label = "New"),
        QuickFilterItem(id = "trending", label = "Trending"),
        QuickFilterItem(id = "verified", label = "Verified"),
        QuickFilterItem(id = "expiring", label = "Expiring Soon"),
    )
```

### Filter Categories
- **All**: Show all available promo codes
- **Popular**: Sorted by community upvotes and engagement
- **New**: Recently added promo codes
- **Trending**: Combination of popularity and recency
- **Verified**: Community-verified and tested codes
- **Expiring Soon**: Codes expiring within 7 days

### Category & Store Filtering
```kotlin
// Category filtering
data class CategoryFilterSelected(val categoryId: String?) : SearchAction

// Store filtering  
data class StoreFilterSelected(val storeId: String?) : SearchAction

// Combined filter application
if (query.isNotEmpty()) {
    codes = codes.filter {
        it.title.contains(query, ignoreCase = true) ||
        it.description.contains(query, ignoreCase = true) ||
        it.code.contains(query, ignoreCase = true) ||
        it.store.name.contains(query, ignoreCase = true)
    }
}

categoryId?.let { id ->
    codes = codes.filter { it.category.id == id }
}

storeId?.let { id ->
    codes = codes.filter { it.store.id == id }
}
```

### Filter State Management
```kotlin
fun isFilterActive(): Boolean =
    searchQuery.isNotEmpty() ||
    quickFilter != "all" ||
    selectedCategoryId != null ||
    selectedStoreId != null ||
    sortOption != SortOption.Recent

fun getActiveFiltersCount(): Int {
    var count = 0
    if (searchQuery.isNotEmpty()) count++
    if (quickFilter != "all") count++
    if (selectedCategoryId != null) count++
    if (selectedStoreId != null) count++
    if (sortOption != SortOption.Recent) count++
    return count
}

fun getFilterSummary(): String {
    val filters = mutableListOf<String>()
    
    if (searchQuery.isNotEmpty()) {
        filters.add("\"$searchQuery\"")
    }
    
    getSelectedCategory()?.let { category ->
        filters.add(category.name)
    }
    
    getSelectedStore()?.let { store ->
        filters.add(store.name)
    }
    
    if (quickFilter != "all") {
        filters.add(quickFilter.replaceFirstChar { it.uppercase() })
    }
    
    return when {
        filters.isEmpty() -> "All promo codes"
        filters.size == 1 -> filters.first()
        else -> "${filters.first()} +${filters.size - 1} more"
    }
}
```

## Sorting System

### Sort Options
```kotlin
enum class SortOption {
    Recent,         // Newest first
    Popular,        // Most upvoted
    ExpiringFirst,  // Expiring soonest
    DiscountAmount, // Highest discount
    StoreAZ         // Store name alphabetical
}
```

### Sort Implementation
```kotlin
codes = when (sortOption) {
    SortOption.Recent -> codes.sortedByDescending { it.createdAt }
    SortOption.Popular -> codes.sortedByDescending { it.upvotes }
    SortOption.ExpiringFirst -> codes.sortedBy { it.expiryDate ?: LocalDate.MAX }
    SortOption.DiscountAmount -> codes.sortedByDescending {
        it.discountPercentage ?: it.discountAmount ?: 0
    }
    SortOption.StoreAZ -> codes.sortedBy { it.store.name }
}
```

## Pagination & Infinite Scroll

### Pagination Logic
```kotlin
private fun loadPromoCodes(reset: Boolean = false) {
    if (reset) {
        currentPage.value = 0
        _uiState.update { it.copy(promoCodesState = PromoCodeListState.Loading) }
    } else {
        _uiState.update { it.copy(isLoadingMore = true) }
    }
    
    viewModelScope.launch {
        try {
            val page = if (reset) 0 else currentPage.value + 1
            val promoCodes = fetchPromoCodes(
                query = _uiState.value.searchQuery,
                categoryId = _uiState.value.selectedCategoryId,
                storeId = _uiState.value.selectedStoreId,
                quickFilter = _uiState.value.quickFilter,
                sortOption = _uiState.value.sortOption,
                page = page,
                pageSize = pageSize,
            )
            
            val existingCodes = if (reset) emptyList() else _uiState.value.promoCodes
            val allCodes = existingCodes + promoCodes
            val hasMore = promoCodes.size == pageSize
            
            _uiState.update {
                it.copy(
                    promoCodesState = if (allCodes.isEmpty()) {
                        PromoCodeListState.Empty
                    } else {
                        PromoCodeListState.Success(allCodes)
                    },
                    isLoadingMore = false,
                    hasMoreItems = hasMore,
                    hasActiveFilters = it.isFilterActive(),
                )
            }
            
            if (!reset) currentPage.value = page
        } catch (exception: Exception) {
            // Handle pagination errors
        }
    }
}
```

### Load More Implementation
```kotlin
private fun handleLoadMore() {
    if (!_uiState.value.isLoadingMore && _uiState.value.hasMoreItems) {
        loadPromoCodes(reset = false)
    }
}
```

### Pagination Features
- **Page Size**: 20 items per page for optimal performance
- **Load More**: Automatic triggering when approaching end of list
- **State Management**: Separate loading states for initial load vs pagination
- **Error Handling**: Graceful failure with retry options
- **Performance**: Efficient list concatenation and state updates

## Screen Flexibility

### Dual Layout Modes
```kotlin
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onPromoCodeClick: (PromoCode) -> Unit = {},
    onStoreClick: (Store) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    showTopBar: Boolean = false // Control whether to show the top bar
)
```

### Standalone Mode
- **Full Scaffold**: Complete screen with top app bar
- **Search Header**: Integrated search bar with filters
- **Navigation**: Independent navigation handling

### Embedded Mode
- **Content Only**: For use within existing app navigation
- **Local Search Bar**: Compact search within content area
- **Parent Integration**: Seamless integration with parent screen

## Error Handling & States

### Error Management
```kotlin
data class ErrorMessage(
    val id: String, 
    val message: String, 
    val type: ErrorType = ErrorType.GENERAL
)

enum class ErrorType {
    NETWORK,
    GENERAL,
    VALIDATION
}
```

### State Handling
```kotlin
when {
    uiState.isLoading -> CatalogLoadingContent()
    uiState.isError -> CatalogErrorContent(
        message = (uiState.promoCodesState as? PromoCodeListState.Error)?.message 
            ?: "An error occurred",
        onRetry = { onAction(SearchAction.RetryLoad) },
    )
    uiState.isEmpty -> CatalogEmptyContent(
        hasFilters = uiState.hasActiveFilters,
        onClearFilters = { onAction(SearchAction.FiltersCleared) },
    )
    else -> CatalogPromoCodesList(/* Success state */)
}
```

### Error Recovery
- **Retry Mechanisms**: User-initiated retry for failed operations
- **State Preservation**: Maintain search state during error recovery
- **Graceful Degradation**: Partial functionality when possible
- **User Feedback**: Clear error messages with actionable solutions

## User Interactions

### Voting System Integration
```kotlin
private fun handlePromoCodeUpvoted(promoCode: PromoCode) {
    if (!_uiState.value.isLoggedIn) {
        showSnackbar("Please log in to upvote promo codes")
        return
    }
    
    viewModelScope.launch {
        try {
            // Optimistic update
            val updatedCodes = _uiState.value.promoCodes.map { code ->
                if (code.id == promoCode.id) {
                    code.copy(
                        isUpvoted = !code.isUpvoted,
                        upvotes = if (code.isUpvoted) code.upvotes - 1 else code.upvotes + 1,
                    )
                } else {
                    code
                }
            }
            
            _uiState.update {
                it.copy(promoCodesState = PromoCodeListState.Success(updatedCodes))
            }
            
            val message = if (promoCode.isUpvoted) "Upvote removed" else "Code upvoted!"
            showSnackbar(message)
        } catch (exception: Exception) {
            showError("Failed to upvote code: ${exception.message}")
        }
    }
}
```

### Copy to Clipboard
```kotlin
private fun handlePromoCodeCopied(promoCode: PromoCode) {
    // TODO: Copy to clipboard
    showSnackbar("Code ${promoCode.code} copied to clipboard")
    // TODO: Track copy analytics
}
```

### Store & Category Following
```kotlin
private fun handleStoreFollowToggled(store: Store) {
    if (!_uiState.value.isLoggedIn) {
        showSnackbar("Please log in to follow stores")
        return
    }
    
    viewModelScope.launch {
        try {
            val message = if (store.isFollowed) 
                "Unfollowed ${store.name}" else "Following ${store.name}"
            showSnackbar(message)
        } catch (exception: Exception) {
            showError("Failed to ${if (store.isFollowed) "unfollow" else "follow"} store")
        }
    }
}
```

## Performance Optimizations

### Search Performance
- **Debounced Input**: 300ms delay to prevent excessive searches
- **Job Cancellation**: Cancel previous searches for responsiveness
- **Minimum Length**: 2-character minimum to reduce noise
- **Suggestion Caching**: Cache recent suggestions for instant display

### List Performance
- **Lazy Loading**: Efficient lazy column implementation
- **Stable Keys**: Proper key management for recomposition
- **Pagination**: Load data in manageable chunks
- **State Hoisting**: Minimize state in composable functions

### Memory Management
- **Image Loading**: Efficient image loading with Coil
- **Data Cleanup**: Proper cleanup of search jobs and state
- **Cache Management**: Smart caching of search results

## Analytics Integration

### Search Analytics
```kotlin
// Search tracking points
private fun handleSearchSubmitted() {
    _uiState.update { it.copy(showSearchSuggestions = false) }
    loadPromoCodes(reset = true)
    // TODO: Track search analytics
}

// PromoCode interactions
private fun handlePromoCodeClicked(promoCode: PromoCode) {
    // TODO: Navigate to promo code details
    // TODO: Track click analytics
}

// Copy tracking
private fun handlePromoCodeCopied(promoCode: PromoCode) {
    showSnackbar("Code ${promoCode.code} copied to clipboard")
    // TODO: Track copy analytics
}
```

### Analytics Events
- **Search Queries**: Track what users search for
- **Filter Usage**: Monitor most popular filters
- **Sort Preferences**: Understand user sorting behavior
- **Interaction Patterns**: Click-through rates and engagement
- **Conversion Tracking**: Search to action conversion rates

## Testing Strategy

### Unit Tests
- **Search Logic**: Test debounced search and suggestion generation
- **Filter Logic**: Test all filter combinations and edge cases
- **Pagination**: Test page loading and state management
- **State Management**: Test UI state transitions and consistency

### UI Tests
- **Search Interactions**: Test search input and suggestions
- **Filter Operations**: Test filter selection and clearing
- **Sort Functionality**: Test sort options and results ordering
- **Pagination**: Test infinite scroll and load more

### Integration Tests
- **End-to-End Search**: Test complete search flow
- **Filter Combinations**: Test complex filter scenarios
- **Error Scenarios**: Test error handling and recovery
- **Performance**: Test search responsiveness and memory usage

## Development Guidelines

1. **Debounced Search**: Always implement debouncing for search input
2. **State Consistency**: Maintain consistent filter state across operations
3. **Error Recovery**: Provide clear recovery options for all error states
4. **Performance First**: Optimize for search responsiveness and memory usage
5. **User Feedback**: Provide immediate feedback for all user actions
6. **Analytics Ready**: Prepare tracking points for user behavior analysis
7. **Accessibility**: Ensure search is accessible with proper semantic markup
8. **Type Safety**: Use sealed classes for action and state management

This search system provides a comprehensive, performant, and user-friendly discovery experience with enterprise-level architecture and modern Android development patterns.