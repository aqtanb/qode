package com.qodein.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.scroll.RegisterScrollState
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.core.ui.util.CustomTabsUtils
import com.qodein.feature.home.ui.component.DialogCoordinator
import com.qodein.feature.home.ui.component.FiltersSection
import com.qodein.feature.home.ui.component.HeroBannerSection
import com.qodein.feature.home.ui.component.LoadingMoreIndicator
import com.qodein.feature.home.ui.component.PromoCodesErrorState
import com.qodein.feature.home.ui.component.PromoCodesLoadingState
import com.qodein.feature.home.ui.component.PromoCodesSectionHeader
import com.qodein.feature.home.ui.component.PromocodeCard
import com.qodein.feature.home.ui.component.PromocodeSectionEmptyState
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
    val listState = rememberLazyListState()
    val context = LocalContext.current
    scrollStateRegistry?.RegisterScrollState(listState)

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

    LaunchedEffect(listState, uiState.promoCodeState, uiState.isLoadingMore) {
        snapshotFlow {
            val promoState = uiState.promoCodeState
            if (promoState is PromoCodeState.Success && promoState.hasMore && !uiState.isLoadingMore) {
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
        userLanguage = userLanguage,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    listState: LazyListState,
    userLanguage: Language,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SpacingTokens.xl),
        ) {
            // Hero Banner
            item(key = BANNER_SECTION_KEY) {
                HeroBannerSection(
                    bannerState = uiState.bannerState,
                    userLanguage = userLanguage,
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
                    modifier = Modifier.padding(vertical = SpacingTokens.md),
                )
            }

            // Promo Codes Section Header
            item(key = PROMO_CODES_HEADER_KEY) {
                PromoCodesSectionHeader(
                    currentFilters = uiState.currentFilters,
                    modifier = Modifier.padding(horizontal = SpacingTokens.lg),
                )
            }

            // Promo Codes Content
            when (val promoState = uiState.promoCodeState) {
                PromoCodeState.Loading -> {
                    item(key = PROMO_CODES_LOADING_KEY) {
                        PromoCodesLoadingState(
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                    }
                }
                is PromoCodeState.Success -> {
                    items(
                        items = promoState.promoCodes,
                        key = { promoCode -> promoCode.id.value },
                    ) { promoCode ->
                        PromocodeCard(
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

                    if (uiState.isLoadingMore) {
                        item(key = PROMO_CODES_LOADING_MORE_KEY) {
                            LoadingMoreIndicator()
                        }
                    }
                }
                PromoCodeState.Empty -> {
                    item(key = PROMO_CODES_EMPTY_KEY) {
                        PromocodeSectionEmptyState(
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
        }

        DialogCoordinator(
            activeDialog = uiState.activeFilterDialog,
            currentFilters = uiState.currentFilters,
            serviceSelectionState = uiState.serviceSelectionState,
            cachedServices = uiState.cachedServices,
            onAction = onAction,
        )
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
            userLanguage = Language.ENGLISH,
            onAction = {},
        )
    }
}
