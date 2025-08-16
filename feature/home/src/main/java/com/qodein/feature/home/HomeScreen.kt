package com.qodein.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeBannerGradient
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeDivider
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeSocialIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.core.ui.component.BannerConfig
import com.qodein.core.ui.component.CouponPromoCodeCard
import com.qodein.core.ui.component.getDefaultBannerConfigs
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPromoCodeDetail: (PromoCode) -> Unit = {},
    onNavigateToBannerDetail: () -> Unit = {},
    onShowPromoCodeCopied: (PromoCode) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.PromoCodeDetailRequested -> onNavigateToPromoCodeDetail(event.promoCode)
                is HomeEvent.BannerDetailRequested -> onNavigateToBannerDetail()
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
                            )
                        }
                        else -> LoadingState()
                    }
                }

                is HomeUiState.Error -> {
                    ErrorState(
                        message = currentState.exception.message ?: stringResource(R.string.error_something_went_wrong),
                        onRetry = { viewModel.onAction(HomeAction.RetryClicked) },
                    )
                }

                is HomeUiState.Success -> {
                    HomeContent(
                        uiState = currentState,
                        listState = listState,
                        onAction = viewModel::onAction,
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
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAction: (HomeAction) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = SpacingTokens.xl),
        modifier = Modifier.navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        // 🔥 REVOLUTIONARY BANNER CAROUSEL (50% Screen Height)
        item(key = "hero_carousel") {
            AutoScrollingHeroBannerSystem()
        }

        // ⚡ MIND-BLOWING QUICK FILTERS
        item(key = "quick_filters") {
            RevolutionaryQuickFilters(onFilterSelected = { /* TODO: Handle filter */ })
        }

        // 🏆 STUNNING SECTION HEADER
        item(key = "trending_header") {
            RevolutionarySectionHeader(
                title = stringResource(R.string.home_section_title),
                subtitle = stringResource(R.string.home_section_subtitle),
                modifier = Modifier.padding(
                    start = SpacingTokens.lg,
                    end = SpacingTokens.lg,
                    top = SpacingTokens.xl,
                    bottom = SpacingTokens.md,
                ),
            )
        }

        // Promo Codes Grid or Empty State
        if (uiState.promoCodes.isEmpty()) {
            item(key = "empty_promo_codes") {
                PromoCodesEmptyState()
            }
        } else {
            items(
                items = uiState.promoCodes,
                key = { it.id.value },
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
        }

        // Loading More Indicator
        if (uiState.isLoadingMore) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
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
        }

        // Bottom spacer
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }
}

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

@Composable
private fun AutoScrollingHeroBannerSystem(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current

    // Calculate 50% screen height including status bar
    val screenHeight = configuration.screenHeightDp
    val bannerHeight = (screenHeight * 0.5f)

    AutoScrollingBanner(
        items = getDefaultBannerConfigs(),
        autoScrollDelay = 4.seconds,
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight.dp),
    ) { bannerConfig, index ->
        PromotionalBanner(
            bannerConfig = bannerConfig,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PromotionalBanner(
    bannerConfig: BannerConfig,
    modifier: Modifier = Modifier
) {
    val gradientColorScheme = bannerConfig.gradientScheme
    val primaryGradientColor = bannerConfig.primaryColor
    val brandName = bannerConfig.brandName

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Use the new gradient system
        QodeBannerGradient(
            colors = gradientColorScheme,
            height = 1000.dp, // Large enough to cover the card
            modifier = Modifier.fillMaxSize(),
        )
        // Floating background elements
        Icon(
            imageVector = QodeCommerceIcons.Flash,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .size(SizeTokens.Decoration.sizeXXLarge * 3)
                .align(Alignment.TopEnd)
                .offset(x = SpacingTokens.xl, y = -SpacingTokens.lg),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top section
            Column {
                Text(
                    text = "Your Country",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.lg),
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QodeDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "Kazakhstan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { /* TODO: Handle country selection */ }
                            .padding(horizontal = SpacingTokens.sm),
                    )
                    QodeDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(SpacingTokens.lg))

                Column(modifier = Modifier.padding(horizontal = SpacingTokens.lg)) {
                    Text(
                        text = brandName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )

                    Text(
                        text = "Exclusive deals and premium offers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = SpacingTokens.xs),
                    )
                }
            }

            // Bottom section with CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.banner_up_to),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "${bannerConfig.discountPercent}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(R.string.banner_off),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(
                                start = SpacingTokens.xs,
                                bottom = SpacingTokens.sm,
                            ),
                        )
                    }
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(ShapeTokens.Corner.large),
                    modifier = Modifier.clickable { /* TODO: Handle banner click */ },
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = SpacingTokens.lg,
                            vertical = SpacingTokens.md,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        Text(
                            text = stringResource(R.string.banner_claim_now),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryGradientColor,
                        )
                        Icon(
                            imageVector = QodeActionIcons.Forward,
                            contentDescription = null,
                            modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                            tint = primaryGradientColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevolutionaryQuickFilters(
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            items(6) { index ->
                ModernCircularFilterChip(
                    nameRes = when (index) {
                        0 -> R.string.filter_kaspi
                        1 -> R.string.filter_top_rated
                        2 -> R.string.filter_latest
                        3 -> R.string.filter_food
                        4 -> R.string.filter_fashion
                        else -> R.string.filter_tech
                    },
                    icon = when (index) {
                        0 -> QodeCommerceIcons.Voucher
                        1 -> QodeStatusIcons.Silver
                        2 -> QodeNavigationIcons.Trending
                        3 -> QodeCategoryIcons.Coffee
                        4 -> QodeCategoryIcons.Clothing
                        else -> QodeSocialIcons.Beeline
                    },
                    onClick = { onFilterSelected("filter_$index") },
                    isSelected = index == 0,
                )
            }
        }
    }
}

@Composable
private fun ModernCircularFilterChip(
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
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Black
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = ShapeTokens.Border.medium,
                    color = Color.Black,
                ),
            ) {
                // White inner border
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ShapeTokens.Border.thick),
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = ShapeTokens.Border.thin,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.White
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
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
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

@Composable
private fun RevolutionarySectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
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
        )
    }
}
