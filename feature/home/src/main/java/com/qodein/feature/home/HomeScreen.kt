package com.qodein.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.component.SortFilterBottomSheet
import com.qodein.core.ui.scroll.RegisterScrollState
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.core.ui.util.CustomTabsUtils
import com.qodein.feature.home.ui.component.FiltersSection
import com.qodein.feature.home.ui.component.HeroBannerSection
import com.qodein.feature.home.ui.component.LoadingMoreIndicator
import com.qodein.feature.home.ui.component.PromocodeCard
import com.qodein.feature.home.ui.component.PromocodeSectionEmptyState
import com.qodein.feature.home.ui.component.PromocodeSectionErrorState
import com.qodein.feature.home.ui.component.PromocodeSectionHeader
import com.qodein.feature.home.ui.component.PromocodeSectionLoadingState
import com.qodein.feature.home.ui.state.PromocodeUiState
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.SortFilter
import com.qodein.shared.ui.FilterDialogType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPromoCodeDetail: (PromocodeId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    scrollStateRegistry: ScrollStateRegistry? = null
) {
    TrackScreenViewEvent(screenName = HOME_SCREEN_NAME)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val serviceUiState by viewModel.serviceSelectionUiState.collectAsStateWithLifecycle()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = viewModel.getSavedScrollIndex(),
            firstVisibleItemScrollOffset = viewModel.getSavedScrollOffset(),
        )
    }

    val context = LocalContext.current
    scrollStateRegistry?.RegisterScrollState(listState)

    // Save scroll position when navigating away
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.saveScrollPosition(index, offset)
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.PromoCodeDetailRequested ->
                    onNavigateToPromoCodeDetail(event.promocodeId)
                is HomeEvent.BannerDetailRequested -> {
                    event.banner.ctaUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        CustomTabsUtils.launchSmartUrl(context, url)
                    }
                }
                is HomeEvent.PromoCodeCopied ->
                    {}
            }
        }
    }

    LaunchedEffect(listState, uiState.promocodeUiState, uiState.isLoadingMore) {
        snapshotFlow {
            val promoState = uiState.promocodeUiState
            if (promoState is PromocodeUiState.Success && promoState.hasMore && !uiState.isLoadingMore) {
                val layoutInfo = listState.layoutInfo
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount

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

    HomeContent(
        uiState = uiState,
        listState = listState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )

    uiState.activeFilterDialog?.let { dialogType ->
        when (dialogType) {
            FilterDialogType.Service -> {
                ServiceSelectorBottomSheet(
                    state = serviceUiState,
                    onAction = { action ->
                        viewModel.onServiceSelectionAction(action)
                    },
                    onDismiss = { viewModel.onAction(HomeAction.DismissFilterDialog) },
                )
            }

            FilterDialogType.Sort -> {
                val sheetState = rememberModalBottomSheetState()
                SortFilterBottomSheet(
                    isVisible = true,
                    currentSortBy = uiState.currentFilters.sortFilter.sortBy,
                    onSortBySelected = { sortBy ->
                        viewModel.onAction(HomeAction.ApplySortFilter(SortFilter(sortBy)))
                        viewModel.onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { viewModel.onAction(HomeAction.DismissFilterDialog) },
                    sheetState = sheetState,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    listState: LazyListState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = uiState.isRefreshing,
        onRefresh = { onAction(HomeAction.RefreshData) },
        modifier = modifier,
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = SpacingTokens.gigantic),
        ) {
            // Hero Banner
            item(key = BANNER_SECTION_KEY) {
                HeroBannerSection(
                    bannerState = uiState.bannerState,
                    userLanguage = uiState.userLanguage,
                    onBannerClick = { banner -> onAction(HomeAction.BannerClicked(banner)) },
                    onRetryBanners = { onAction(HomeAction.RetryBannersClicked) },
                )
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
                    modifier = Modifier.padding(
                        top = SpacingTokens.lg,
                        bottom = SpacingTokens.md,
                        start = SpacingTokens.sm,
                        end = SpacingTokens.sm,
                    ),
                )
            }

            // Promo Codes Section Header
            item(key = PROMO_CODES_HEADER_KEY) {
                PromocodeSectionHeader(
                    currentFilters = uiState.currentFilters,
                    modifier = Modifier.padding(SpacingTokens.xs),
                )
            }

            // Promo Codes Content
            when (val promoState = uiState.promocodeUiState) {
                PromocodeUiState.Loading -> {
                    item(key = PROMO_CODES_LOADING_KEY) {
                        PromocodeSectionLoadingState(
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                    }
                }
                is PromocodeUiState.Success -> {
                    items(
                        items = promoState.promocodes,
                        key = { promoCode -> promoCode.id.value },
                    ) { promocode ->
                        PromocodeCard(
                            promocode = promocode,
                            onCardClick = {
                                onAction(HomeAction.PromoCodeClicked(promocode.id))
                            },
                            onCopyCodeClick = {
                                onAction(HomeAction.CopyPromoCode(promocode))
                            },
                            modifier = Modifier.padding(SpacingTokens.sm),
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item(key = PROMO_CODES_LOADING_MORE_KEY) {
                            LoadingMoreIndicator()
                        }
                    }
                }
                PromocodeUiState.Empty -> {
                    item(key = PROMO_CODES_EMPTY_KEY) {
                        PromocodeSectionEmptyState(
                            modifier = Modifier.padding(start = SpacingTokens.sm, end = SpacingTokens.sm, bottom = SpacingTokens.gigantic),
                        )
                    }
                }
                is PromocodeUiState.Error -> {
                    item(key = PROMO_CODES_ERROR_KEY) {
                        PromocodeSectionErrorState(
                            errorState = promoState,
                            onRetry = { onAction(HomeAction.RetryPromoCodesClicked) },
                            modifier = Modifier.padding(start = SpacingTokens.sm, end = SpacingTokens.sm, bottom = SpacingTokens.gigantic),
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Constants

private const val HOME_SCREEN_NAME = "Home"
private const val PAGINATION_LOAD_THRESHOLD = 1
private const val BANNER_SECTION_KEY = "banner_section"
private const val FILTERS_SECTION_KEY = "filters_section"
private const val PROMO_CODES_HEADER_KEY = "promo_codes_header"
private const val PROMO_CODES_LOADING_KEY = "promo_codes_loading"
private const val PROMO_CODES_EMPTY_KEY = "promo_codes_empty"
private const val PROMO_CODES_ERROR_KEY = "promo_codes_error"
private const val PROMO_CODES_LOADING_MORE_KEY = "promo_codes_loading_more"

// MARK: - Previews

@ThemePreviews
@Composable
private fun HomeScreenPreview() {
    QodeTheme {
        HomeContent(
            uiState = HomeUiState(),
            listState = rememberLazyListState(),
            onAction = {},
        )
    }
}
