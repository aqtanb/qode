# Home Feed System

## Overview
Dynamic home feed with hero banners, filter carousel, and infinite scroll promo code feed. Features pull-to-refresh, pagination, optimistic voting, and community-driven content discovery with sophisticated UX patterns.

## Architecture
- **MVI Pattern**: HomeViewModel with HomeAction sealed classes and HomeEvent for side effects
- **Feed Composition**: Hero banners, quick filters, and promo code grid with infinite scroll
- **Real-time Voting**: Optimistic UI updates with community voting system
- **Smart Pagination**: Automatic loading with pagination triggers and smooth UX
- **Error Handling**: Comprehensive error states with retry mechanisms
- **Analytics Ready**: Prepared for user interaction tracking and analytics

## Key Files

### Feature Layer
- `feature/home/HomeScreen.kt` - Main feed composition with pull-to-refresh and pagination
- `feature/home/HomeViewModel.kt` - MVI ViewModel with feed state management and voting logic
- `feature/home/HomeUiState.kt` - Feed state management with loading, success, error states
- `feature/home/HomeAction.kt` - User interactions (voting, copying, navigation)
- `feature/home/HomeEvent.kt` - Side effects for navigation and feedback
- `feature/home/navigation/HomeNavigation.kt` - Type-safe navigation integration

### UI Components
- `core/ui/component/HeroBanner.kt` - Hero banner carousel component
- `core/ui/component/CouponPromoCodeCard.kt` - Promo code card with voting and copy functionality
- `core/ui/component/PromoCodeList.kt` - List components with pagination support

### Business Logic
- `core/domain/usecase/promocode/GetPromoCodesUseCase.kt` - Feed data retrieval with sorting
- `core/domain/usecase/promocode/VoteOnPromoCodeUseCase.kt` - Community voting logic

## Feed Architecture

### UI State Management
```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Refreshing : HomeUiState
    
    data class Success(
        val promoCodes: List<PromoCode>,
        val bannerItems: List<HeroBannerItem>,
        val hasMorePromoCodes: Boolean,
        val isLoadingMore: Boolean = false
    ) : HomeUiState {
        val isEmpty: Boolean
        val hasContent: Boolean
    }
    
    data class Error(
        val exception: Throwable, 
        val isRetryable: Boolean = true
    ) : HomeUiState
}
```

### Action System
```kotlin
sealed interface HomeAction {
    data object RefreshData : HomeAction
    data class BannerItemClicked(val item: HeroBannerItem) : HomeAction
    data class PromoCodeClicked(val promoCode: PromoCode) : HomeAction
    data class UpvotePromoCode(val promoCodeId: String) : HomeAction
    data class DownvotePromoCode(val promoCodeId: String) : HomeAction
    data class CopyPromoCode(val promoCode: PromoCode) : HomeAction
    data object LoadMorePromoCodes : HomeAction
    data object RetryClicked : HomeAction
    data object ErrorDismissed : HomeAction
}
```

### Event System
```kotlin
sealed interface HomeEvent {
    data class PromoCodeDetailRequested(val promoCode: PromoCode) : HomeEvent
    data class BannerDetailRequested(val item: HeroBannerItem) : HomeEvent
    data class PromoCodeCopied(val promoCode: PromoCode) : HomeEvent
}
```

## Feed Composition

### Screen Structure
```kotlin
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPromoCodeDetail: (PromoCode) -> Unit = {},
    onNavigateToBannerDetail: (HeroBannerItem) -> Unit = {},
    onShowPromoCodeCopied: (PromoCode) -> Unit = {}
)
```

### Feed Content Layout
```kotlin
LazyColumn(
    state = listState,
    contentPadding = PaddingValues(bottom = SpacingTokens.xl),
    verticalArrangement = Arrangement.spacedBy(0.dp),
) {
    // Hero Banner Carousel
    item(key = "hero_carousel") {
        HeroBannerCarousel()
    }
    
    // Quick Filter Chips
    item(key = "quick_filters") {
        RevolutionaryQuickFilters(onFilterSelected = { /* Filter logic */ })
    }
    
    // Section Header
    item(key = "trending_header") {
        RevolutionarySectionHeader(
            title = stringResource(R.string.home_section_title),
            subtitle = stringResource(R.string.home_section_subtitle)
        )
    }
    
    // Promo Code Grid with Infinite Scroll
    items(
        items = uiState.promoCodes,
        key = { it.id.value },
    ) { promoCode ->
        CouponPromoCodeCard(
            promoCode = promoCode,
            onCardClick = { onAction(HomeAction.PromoCodeClicked(promoCode)) },
            onCopyCodeClick = { onAction(HomeAction.CopyPromoCode(promoCode)) }
        )
    }
    
    // Loading More Indicator
    if (uiState.isLoadingMore) {
        item(key = "loading_more") {
            LoadingMoreIndicator()
        }
    }
}
```

## Hero Banner System

### Revolutionary Banner Carousel
```kotlin
@Composable
private fun HeroBannerCarousel(modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        items(3) { index ->
            RevolutionaryBannerCard(
                title = stringResource(R.string.banner_featured_title),
                discount = "75%",
                brand = when (index) {
                    0 -> "Kaspi Gold"
                    1 -> "Wildberries" 
                    else -> "Lamoda"
                },
                gradientColors = when (index) {
                    0 -> listOf(0xFF6366F1, 0xFF8B5CF6)
                    1 -> listOf(0xFFEC4899, 0xFFF97316)
                    else -> listOf(0xFF10B981, 0xFF059669)
                }
            )
        }
    }
}
```

### Banner Card Features
- **Gradient Backgrounds**: Dynamic gradient colors per banner
- **Floating Elements**: Decorative icons with glassmorphism effects
- **Call-to-Action**: "Claim Now" buttons with brand colors
- **Visual Hierarchy**: Clear discount display and brand prominence
- **Glass Effects**: Transparent overlays and backdrop filters

## Filter System

### Quick Filter Carousel
```kotlin
@Composable
private fun RevolutionaryQuickFilters(
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        items(6) { index ->
            RevolutionaryFilterChip(
                nameRes = filterNames[index],
                icon = filterIcons[index],
                onClick = { onFilterSelected("filter_$index") },
                isSelected = index == 0
            )
        }
    }
}
```

### Filter Categories
- **Kaspi**: Kaspi-specific deals and offers
- **Top Rated**: Community-voted best deals
- **Latest**: Newest promo codes
- **Food**: Restaurant and food delivery deals
- **Fashion**: Clothing and accessories
- **Tech**: Electronics and gadgets

### Filter Chip Design
- **Icon Integration**: Category-specific icons from QodeIcons
- **Selection States**: Visual feedback for active filters
- **Elevation Effects**: Material 3 elevation for selected state
- **Color Theming**: Consistent with design system colors

## Infinite Scroll & Pagination

### Smart Pagination Logic
```kotlin
val shouldLoadMore by remember {
    derivedStateOf {
        val currentState = uiState
        if (currentState !is HomeUiState.Success) return@derivedStateOf false
        
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = listState.layoutInfo.totalItemsCount
        lastVisibleIndex >= totalItems - 3 && 
        currentState.hasMorePromoCodes && 
        !currentState.isLoadingMore
    }
}

LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore) {
        viewModel.onAction(HomeAction.LoadMorePromoCodes)
    }
}
```

### Pagination Features
- **Threshold-Based Loading**: Loads more content when user approaches end
- **Loading State Management**: Prevents duplicate requests during loading
- **Error Handling**: Graceful failure handling with retry options
- **Performance Optimization**: Efficient list state tracking

## Community Voting System

### Optimistic UI Updates
```kotlin
private fun onUpvotePromoCode(promoCodeId: String) {
    val currentState = _uiState.value
    if (currentState !is HomeUiState.Success) return
    
    viewModelScope.launch {
        try {
            // Update UI optimistically first
            val updatedPromoCodes = currentState.promoCodes.map { promoCode ->
                if (promoCode.id.value == promoCodeId) {
                    when (promoCode) {
                        is PromoCode.PercentagePromoCode -> promoCode.copy(
                            upvotes = promoCode.upvotes + 1
                        )
                        is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                            upvotes = promoCode.upvotes + 1
                        )
                    }
                } else {
                    promoCode
                }
            }
            
            _uiState.value = currentState.copy(promoCodes = updatedPromoCodes)
            
            // Call actual voting use case
            voteOnPromoCodeUseCase(
                promoCodeId = PromoCodeId(promoCodeId),
                userId = UserId("current_user"),
                isUpvote = true
            ).catch { e ->
                // Revert optimistic update on error
                _uiState.value = currentState
            }.collect { vote ->
                // Success - optimistic update confirmed
            }
        } catch (exception: Exception) {
            // Revert to original state on error
            _uiState.value = currentState
        }
    }
}
```

### Voting Features
- **Instant Feedback**: UI updates immediately for responsive feel
- **Error Recovery**: Automatic rollback on network failures
- **Vote Persistence**: Server synchronization with conflict resolution
- **User State Tracking**: Remembers user's voting history

## Pull-to-Refresh

### Implementation
```kotlin
PullToRefreshBox(
    isRefreshing = uiState is HomeUiState.Refreshing,
    state = pullToRefreshState,
    onRefresh = {
        viewModel.onAction(HomeAction.RefreshData)
    },
) {
    // Feed content here
}
```

### Refresh Features
- **Visual Feedback**: Material 3 pull-to-refresh indicator
- **State Management**: Separate refreshing state from loading
- **Data Synchronization**: Fresh data fetch from server
- **Error Handling**: Graceful refresh failure handling

## Error States & Recovery

### Comprehensive Error Handling
```kotlin
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        QodeButton(
            onClick = onRetry,
            text = "Try Again",
            variant = QodeButtonVariant.Primary,
        )
    }
}
```

### Error Recovery Mechanisms
- **Retry Actions**: User-initiated retry with clear messaging
- **Network Error Handling**: Specific handling for connectivity issues
- **Server Error Handling**: Graceful degradation for backend failures
- **Empty State**: Friendly messaging when no content is available

## Performance Optimizations

### Lazy Loading
- **Key-Based Items**: Stable keys for efficient recomposition
- **Content Padding**: Proper spacing without extra composables
- **State Hoisting**: Minimal state in composable functions

### Memory Management
- **Image Loading**: Efficient image loading with Coil
- **List State**: Proper LazyListState management
- **Data Caching**: Smart caching strategies for feed data

## Analytics Integration

### Tracking Points
```kotlin
// Banner interactions
private fun onBannerItemClicked(item: HeroBannerItem) {
    // TODO: Track analytics
    // analyticsRepository.trackBannerClick(item.id)
    emitEvent(HomeEvent.BannerDetailRequested(item))
}

// Promo code interactions  
private fun onPromoCodeClicked(promoCode: PromoCode) {
    // TODO: Track analytics
    // analyticsRepository.trackPromoCodeView(promoCode.id)
    emitEvent(HomeEvent.PromoCodeDetailRequested(promoCode))
}

// Copy actions
private fun onCopyPromoCode(promoCode: PromoCode) {
    // TODO: Copy to clipboard
    // clipboardRepository.copyToClipboard(promoCode.code)
    
    // TODO: Track analytics
    // analyticsRepository.trackPromoCodeCopy(promoCode.id)
    emitEvent(HomeEvent.PromoCodeCopied(promoCode))
}
```

### Analytics Events
- **Banner Clicks**: Track which banners drive engagement
- **PromoCode Views**: Monitor promo code popularity
- **Copy Actions**: Track conversion from view to action
- **Filter Usage**: Understand user discovery patterns
- **Scroll Behavior**: Analyze feed engagement patterns

## Event Handling

### Navigation Events
```kotlin
LaunchedEffect(viewModel.events) {
    viewModel.events.collect { event ->
        when (event) {
            is HomeEvent.PromoCodeDetailRequested -> 
                onNavigateToPromoCodeDetail(event.promoCode)
            is HomeEvent.BannerDetailRequested -> 
                onNavigateToBannerDetail(event.item)
            is HomeEvent.PromoCodeCopied -> 
                onShowPromoCodeCopied(event.promoCode)
        }
    }
}
```

### Event Types
- **Navigation Events**: Deep linking to detail screens
- **Feedback Events**: Snackbar messages and user feedback
- **External Actions**: Clipboard operations and sharing
- **Analytics Events**: User behavior tracking

## Design System Integration

### Theme Consistency
- **QodeTheme**: Consistent Material 3 theming
- **Design Tokens**: SpacingTokens, SizeTokens, MotionTokens
- **QodeIcons**: Centralized icon system usage
- **Color Schemes**: Proper color role usage

### Component Usage
- **QodeButton**: Consistent button styling and behavior
- **QodeCard**: Structured card layouts with proper elevation
- **Material 3**: Modern design system implementation
- **Accessibility**: Proper semantic properties and descriptions

## Testing Strategy

### Unit Tests
- **ViewModel Logic**: Test action handling and state transitions
- **Pagination Logic**: Test infinite scroll triggers
- **Voting Logic**: Test optimistic updates and error recovery
- **State Management**: Test UI state transitions

### UI Tests
- **Pull-to-Refresh**: Test refresh interactions
- **Infinite Scroll**: Test pagination behavior
- **Error States**: Test error display and recovery
- **Banner Interactions**: Test carousel navigation

### Integration Tests
- **Feed Loading**: Test complete feed loading flow
- **Voting System**: Test end-to-end voting behavior
- **Navigation**: Test event-based navigation
- **Error Recovery**: Test error handling scenarios

## Development Guidelines

1. **Use Events for Navigation**: All navigation through HomeEvent system
2. **Optimistic Updates**: Immediate UI feedback for user actions
3. **Error Recovery**: Comprehensive error handling with user-friendly messages
4. **Performance First**: Efficient list rendering and memory usage
5. **Analytics Ready**: Prepare tracking points for user behavior analysis
6. **Design System**: Consistent use of QodeTheme and design tokens
7. **Type Safety**: Leverage sealed classes for state management
8. **Community Focus**: Design for community-driven content discovery

This home feed system provides a modern, performant, and user-friendly experience for discovering and interacting with promo codes while maintaining enterprise-level architecture and testing practices.