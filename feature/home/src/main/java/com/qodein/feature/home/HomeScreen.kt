package com.qodein.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeSocialIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.CouponPromoCodeCard
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.core.ui.util.CustomTabsUtils
import com.qodein.feature.home.component.CategoryFilterDialog
import com.qodein.feature.home.component.HeroBannerSection
import com.qodein.feature.home.component.ServiceFilterDialog
import com.qodein.feature.home.component.SortFilterDialog
import com.qodein.feature.home.component.TypeFilterDialog
import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.FilterState
import com.qodein.feature.home.model.PromoCodeTypeFilter
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.PromoCode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    userLanguage: Language,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPromoCodeDetail: (PromoCode) -> Unit = {},
    onNavigateToBannerDetail: (Banner) -> Unit = {},
    onShowPromoCodeCopied: (PromoCode) -> Unit = {}
) {
    // Track screen view
    TrackScreenViewEvent(screenName = "Home")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    // Handle events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.PromoCodeDetailRequested -> onNavigateToPromoCodeDetail(event.promoCode)
                is HomeEvent.BannerDetailRequested -> {
                    // Handle banner CTA URL navigation with CustomTabs
                    val banner = event.banner
                    val ctaUrl = banner.ctaUrl
                    if (ctaUrl?.isNotBlank() == true) {
                        // Use branded CustomTabs for better UX
                        CustomTabsUtils.launchCustomTab(
                            context = context,
                            url = ctaUrl,
                        )
                    } else {
                        // No URL, use the navigation callback
                        onNavigateToBannerDetail(banner)
                    }
                }
                is HomeEvent.PromoCodeCopied -> onShowPromoCodeCopied(event.promoCode)
            }
        }
    }

    // Pagination trigger - only for Success state
    val shouldLoadMore by remember {
        derivedStateOf {
            val currentState = uiState
            if (currentState !is HomeUiState.Success) return@derivedStateOf false

            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 3 && currentState.hasMorePromoCodes && !currentState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.onAction(HomeAction.LoadMorePromoCodes)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState is HomeUiState.Refreshing,
            state = pullToRefreshState,
            onRefresh = {
                viewModel.onAction(HomeAction.RefreshData)
            },
        ) {
            val currentState = uiState
            when (currentState) {
                is HomeUiState.Loading -> {
                    LoadingState()
                }

                is HomeUiState.Refreshing -> {
                    // Show content during refresh, not loading screen
                    when {
                        currentState.previousData != null -> {
                            HomeContent(
                                uiState = currentState.previousData,
                                listState = listState,
                                onAction = viewModel::onAction,
                                userLanguage = userLanguage,
                            )
                        }
                        else -> LoadingState()
                    }
                }

                is HomeUiState.Error -> {
                    ErrorState(
                        message = currentState.errorType.toLocalizedMessage(),
                        onRetry = { viewModel.onAction(HomeAction.RetryClicked) },
                    )
                }

                is HomeUiState.Success -> {
                    HomeContent(
                        uiState = currentState,
                        listState = listState,
                        onAction = viewModel::onAction,
                        userLanguage = userLanguage,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Success,
    listState: LazyListState,
    onAction: (HomeAction) -> Unit,
    userLanguage: Language
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = SpacingTokens.xl),
        modifier = Modifier.navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        // Section 1: Hero Banner Section
        item(key = "hero_banner_section") {
            HeroBannerSection(
                banners = uiState.banners,
                onBannerClick = { banner -> onAction(HomeAction.BannerClicked(banner)) },
                userLanguage = userLanguage,
            )
        }

        // Section 2: Quick Filters Section
        item(key = "quick_filters_section") {
            QuickFiltersSection(
                currentFilters = uiState.currentFilters,
                onFilterSelected = { filterType -> onAction(HomeAction.ShowFilterDialog(filterType)) },
            )
        }

        // Section 3: Promo Codes Section
        item(key = "promo_codes_section") {
            PromoCodesSection(
                promoCodes = uiState.promoCodes,
                isLoadingMore = uiState.isLoadingMore,
                onAction = onAction,
            )
        }

        // Bottom spacer for navigation bar
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }

    // Filter Dialogs
    uiState.activeFilterDialog?.let { dialogType ->
        when (dialogType) {
            FilterDialogType.Type -> {
                TypeFilterDialog(
                    currentFilter = uiState.currentFilters.typeFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplyTypeFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                )
            }
            FilterDialogType.Category -> {
                CategoryFilterDialog(
                    currentFilter = uiState.currentFilters.categoryFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplyCategoryFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                )
            }
            FilterDialogType.Service -> {
                // TODO: Get available services from ViewModel
                ServiceFilterDialog(
                    currentFilter = uiState.currentFilters.serviceFilter,
                    availableServices = emptyList(), // TODO: Pass actual services
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplyServiceFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                )
            }
            FilterDialogType.Sort -> {
                SortFilterDialog(
                    currentFilter = uiState.currentFilters.sortFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplySortFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                )
            }
        }
    }
}

// MARK: - Loading & Error States

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_something_went_wrong),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        QodeButton(
            onClick = onRetry,
            text = stringResource(R.string.error_retry),
            variant = QodeButtonVariant.Primary,
        )
    }
}

@Composable
private fun PromoCodesEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = QodeBusinessIcons.Asset,
            contentDescription = null,
            modifier = Modifier.size(SizeTokens.Avatar.sizeLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        Text(
            text = stringResource(R.string.empty_no_promo_codes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        Text(
            text = stringResource(R.string.empty_check_back_later),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// MARK: - Section 2: Quick Filters

/**
 * Section 2: Quick Filters Section
 * Contains filter chips for type, category, service, and sort
 */
@Composable
private fun QuickFiltersSection(
    currentFilters: FilterState,
    onFilterSelected: (FilterDialogType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // Type Filter
            item(key = "type_filter") {
                FilterChip(
                    nameRes = R.string.filter_chip_type,
                    icon = QodeCommerceIcons.Voucher,
                    onClick = { onFilterSelected(FilterDialogType.Type) },
                    isSelected = currentFilters.typeFilter !is PromoCodeTypeFilter.All,
                )
            }

            // Category Filter
            item(key = "category_filter") {
                FilterChip(
                    nameRes = R.string.filter_chip_category,
                    icon = QodeCategoryIcons.Coffee,
                    onClick = { onFilterSelected(FilterDialogType.Category) },
                    isSelected = currentFilters.categoryFilter !is CategoryFilter.All,
                )
            }

            // Service Filter
            item(key = "service_filter") {
                FilterChip(
                    nameRes = R.string.filter_chip_service,
                    icon = QodeSocialIcons.Beeline,
                    onClick = { onFilterSelected(FilterDialogType.Service) },
                    isSelected = currentFilters.serviceFilter !is ServiceFilter.All,
                )
            }

            // Sort Filter
            item(key = "sort_filter") {
                FilterChip(
                    nameRes = R.string.filter_chip_sort,
                    icon = QodeStatusIcons.Silver,
                    onClick = { onFilterSelected(FilterDialogType.Sort) },
                    isSelected = true, // Sort is always selected (has default value)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    nameRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        // Circular container with black border and white inner border
        Box(
            modifier = Modifier.size(SizeTokens.Avatar.sizeMedium + SpacingTokens.sm),
            contentAlignment = Alignment.Center,
        ) {
            // Black outer border
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                // White inner border
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ShapeTokens.Border.thick),
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = ShapeTokens.Border.thin,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(nameRes),
                            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }

        // Category title under circle
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// MARK: - Section 3: Promo Codes

/**
 * Section 3: Promo Codes Section
 * Contains section header and list of promotional codes
 */
@Composable
private fun PromoCodesSection(
    promoCodes: List<PromoCode>,
    isLoadingMore: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Section header
        PromoCodesSectionHeader(
            modifier = Modifier.padding(horizontal = SpacingTokens.lg),
        )

        // Content based on state
        if (promoCodes.isEmpty()) {
            PromoCodesEmptyState()
        } else {
            PromoCodesList(
                promoCodes = promoCodes,
                onAction = onAction,
            )
        }

        // Loading indicator
        if (isLoadingMore) {
            LoadingMoreIndicator()
        }
    }
}

@Composable
private fun PromoCodesSectionHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.home_section_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun PromoCodesList(
    promoCodes: List<PromoCode>,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        promoCodes.forEach { promoCode ->
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
    }
}

@Composable
private fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ShapeTokens.Border.medium,
        )
    }
}

// MARK: - Preview Functions

@Preview(name = "HomeScreen", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    QodeTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                promoCodes = emptyList(),
                hasMorePromoCodes = false,
                isLoadingMore = false,
            ),
            listState = rememberLazyListState(),
            onAction = { },
            userLanguage = Language.ENGLISH,
        )
    }
}
