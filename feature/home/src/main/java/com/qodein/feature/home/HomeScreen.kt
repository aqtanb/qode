package com.qodein.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.scroll.RegisterScrollState
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.core.ui.util.CustomTabsUtils
import com.qodein.feature.home.ui.component.CouponPromoCodeCard
import com.qodein.feature.home.ui.component.DialogCoordinator
import com.qodein.feature.home.ui.component.FiltersSection
import com.qodein.feature.home.ui.component.HeroBannerSection
import com.qodein.feature.home.ui.component.LoadingMoreIndicator
import com.qodein.feature.home.ui.component.PromoCodesEmptyState
import com.qodein.feature.home.ui.component.PromoCodesErrorState
import com.qodein.feature.home.ui.component.PromoCodesLoadingState
import com.qodein.feature.home.ui.component.PromoCodesSectionHeader
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.PromoCode

/**
 * Clean, modular HomeScreen following modern Compose patterns
 * Uses component-based architecture with proper state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userLanguage: Language,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPromoCodeDetail: (PromoCode) -> Unit = {},
    onNavigateToBannerDetail: (Banner) -> Unit = {},
    onShowPromoCodeCopied: (PromoCode) -> Unit = {},
    scrollStateRegistry: ScrollStateRegistry? = null
) {
    TrackScreenViewEvent(screenName = HOME_SCREEN_NAME)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    // Register scroll state for bottom navigation auto-hiding
    scrollStateRegistry?.RegisterScrollState(listState)

    // Handle navigation events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.PromoCodeDetailRequested ->
                    onNavigateToPromoCodeDetail(event.promoCode)
                is HomeEvent.BannerDetailRequested -> {
                    event.banner.ctaUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        CustomTabsUtils.launchCustomTab(context, url)
                    } ?: onNavigateToBannerDetail(event.banner)
                }
                is HomeEvent.PromoCodeCopied ->
                    onShowPromoCodeCopied(event.promoCode)
            }
        }
    }

    // Handle pagination
    LaunchedEffect(listState, uiState.promoCodeState, uiState.isLoadingMore) {
        snapshotFlow {
            val promoState = uiState.promoCodeState
            // Only handle pagination when we have successful promo code data
            if (promoState is PromoCodeState.Success && promoState.hasMore && !uiState.isLoadingMore) {
                val layoutInfo = listState.layoutInfo
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount

                // Trigger when user scrolls to near the end
                lastVisibleIndex >= totalItems - PAGINATION_LOAD_THRESHOLD
            } else {
                false
            }
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) {
                viewModel.onAction(HomeAction.LoadMorePromoCodes)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = false,
            state = pullToRefreshState,
            onRefresh = { viewModel.onAction(HomeAction.RefreshData) },
        ) {
            // Always show HomeContent - individual sections handle their own states
            HomeContent(
                uiState = uiState,
                listState = listState,
                userLanguage = userLanguage,
                onAction = viewModel::onAction,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    listState: LazyListState,
    userLanguage: Language,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate screen height and peek height (30% for bottom sheet peek)
    val screenHeight = LocalWindowInfo.current.let { windowInfo ->
        with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }
    }
    val peekHeight = screenHeight * SHEET_PEEK_HEIGHT_PERCENTAGE

    // Bottom sheet scaffold state
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(),
    )

    // Simple, reliable animation based on sheet state
    val bannerScale by animateFloatAsState(
        targetValue = if (bottomSheetScaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
            BANNER_SCALE_FACTOR
        } else {
            1f
        },
        animationSpec = tween(durationMillis = BANNER_ANIMATION_DURATION_MS),
        label = "bannerScale",
    )

    val bannerAlpha by animateFloatAsState(
        targetValue = if (bottomSheetScaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
            BANNER_ALPHA_FACTOR
        } else {
            1f
        },
        animationSpec = tween(durationMillis = BANNER_ANIMATION_DURATION_MS),
        label = "bannerAlpha",
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            // Bottom sheet content - filters and promo codes with rounded corners
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = SHEET_CORNER_RADIUS_DP.dp,
                            topEnd = SHEET_CORNER_RADIUS_DP.dp,
                        ),
                    ),
            ) {
                BottomSheetContent(
                    uiState = uiState,
                    listState = listState,
                    onAction = onAction,
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
        sheetPeekHeight = peekHeight,
        sheetDragHandle = null, // We'll add custom drag handle
        modifier = modifier,
    ) {
        // Main content - animated banner that responds to sheet drag
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = bannerScale,
                    scaleY = bannerScale,
                    alpha = bannerAlpha,
                ),
        ) {
            HeroBannerSection(
                bannerState = uiState.bannerState,
                userLanguage = userLanguage,
                onBannerClick = { banner -> onAction(HomeAction.BannerClicked(banner)) },
                onRetryBanners = { onAction(HomeAction.RetryBannersClicked) },
            )
        }
    }

    // Dialog Management
    DialogCoordinator(
        activeDialog = uiState.activeFilterDialog,
        currentFilters = uiState.currentFilters,
        serviceSearchState = uiState.serviceSearchState,
        onAction = onAction,
    )
}

@Composable
private fun BottomSheetContent(
    uiState: HomeUiState,
    listState: LazyListState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(bottom = SpacingTokens.xl),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Drag handle item at the top
        item(key = "drag_handle") {
            DragHandle()
        }

        // Filters Section
        item(key = FILTERS_SECTION_KEY) {
            FiltersSection(
                currentFilters = uiState.currentFilters,
                onFilterSelected = { filterType ->
                    onAction(HomeAction.ShowFilterDialog(filterType))
                },
                onResetFilters = {
                    onAction(HomeAction.ResetFilters)
                },
            )
        }

        // Promo Codes Section Header
        item(key = PROMO_CODES_HEADER_KEY) {
            PromoCodesSectionHeader(
                currentFilters = uiState.currentFilters,
                modifier = Modifier.padding(horizontal = SpacingTokens.lg),
            )
        }

        // Promo Codes Content - handle each state appropriately
        when (val promoState = uiState.promoCodeState) {
            PromoCodeState.Loading -> {
                item(key = PROMO_CODES_LOADING_KEY) {
                    PromoCodesLoadingState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.xl),
                    )
                }
            }
            is PromoCodeState.Success -> {
                items(
                    items = promoState.promoCodes,
                    key = { promoCode -> promoCode.id.value },
                ) { promoCode ->
                    CouponPromoCodeCard(
                        promoCode = promoCode,
                        onCardClick = {
                            onAction(HomeAction.PromoCodeClicked(promoCode))
                        },
                        onCopyCodeClick = {
                            onAction(HomeAction.CopyPromoCode(promoCode))
                        },
                        modifier = Modifier.padding(
                            horizontal = SpacingTokens.lg,
                            vertical = SpacingTokens.xs,
                        ),
                    )
                }

                // Loading more indicator
                if (uiState.isLoadingMore) {
                    item(key = PROMO_CODES_LOADING_MORE_KEY) {
                        LoadingMoreIndicator()
                    }
                }
            }
            PromoCodeState.Empty -> {
                item(key = PROMO_CODES_EMPTY_KEY) {
                    PromoCodesEmptyState(
                        modifier = Modifier.padding(horizontal = SpacingTokens.lg),
                    )
                }
            }
            is PromoCodeState.Error -> {
                item(key = PROMO_CODES_ERROR_KEY) {
                    PromoCodesErrorState(
                        errorState = promoState,
                        onRetry = { onAction(HomeAction.RetryPromoCodesClicked) },
                        modifier = Modifier.padding(horizontal = SpacingTokens.lg),
                    )
                }
            }
        }

        // Bottom spacer
        item(key = BOTTOM_SPACER_KEY) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.sm),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(DRAG_HANDLE_WIDTH_DP.dp)
                .height(DRAG_HANDLE_HEIGHT_DP.dp)
                .background(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DRAG_HANDLE_ALPHA),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DRAG_HANDLE_CORNER_RADIUS_DP.dp),
                ),
        )
    }
}

// MARK: - Constants

private const val HOME_SCREEN_NAME = "Home"
private const val PAGINATION_LOAD_THRESHOLD = 1

// Bottom Sheet Configuration
private const val SHEET_PEEK_HEIGHT_PERCENTAGE = 0.3f
private const val DRAG_HANDLE_WIDTH_DP = 40
private const val DRAG_HANDLE_HEIGHT_DP = 4
private const val DRAG_HANDLE_CORNER_RADIUS_DP = 2
private const val DRAG_HANDLE_ALPHA = 0.4f

// Animation Configuration
private const val SHEET_CORNER_RADIUS_DP = 20
private const val BANNER_ANIMATION_DURATION_MS = 700
private const val BANNER_SCALE_FACTOR = 0.85f // Much more dramatic shrinking!
private const val BANNER_ALPHA_FACTOR = 0.5f // Way more fade out!

// Content keys for LazyColumn items
private const val BANNER_SECTION_KEY = "banner_section"
private const val FILTERS_SECTION_KEY = "filters_section"
private const val PROMO_CODES_HEADER_KEY = "promo_codes_header"
private const val PROMO_CODES_LOADING_KEY = "promo_codes_loading"
private const val PROMO_CODES_EMPTY_KEY = "promo_codes_empty"
private const val PROMO_CODES_ERROR_KEY = "promo_codes_error"
private const val PROMO_CODES_LOADING_MORE_KEY = "promo_codes_loading_more"
private const val BOTTOM_SPACER_KEY = "bottom_spacer"

// MARK: - Previews

@Preview(name = "HomeScreen", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    QodeTheme {
        HomeScreen(
            userLanguage = Language.ENGLISH,
        )
    }
}
