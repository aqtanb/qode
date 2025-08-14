package com.qodein.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.EnhancedPromoCodeCard
import com.qodein.core.ui.component.HeroBannerItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPromoCodeDetail: (PromoCode) -> Unit = {},
    onNavigateToBannerDetail: (HeroBannerItem) -> Unit = {},
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
                is HomeEvent.BannerDetailRequested -> onNavigateToBannerDetail(event.item)
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
                    LoadingState()
                }

                is HomeUiState.Error -> {
                    ErrorState(
                        message = currentState.exception.message ?: "Something went wrong",
                        onRetry = { viewModel.onAction(HomeAction.RetryClicked) },
                    )
                }

                is HomeUiState.Success -> {
                    if (currentState.isEmpty) {
                        EmptyState()
                    } else {
                        HomeContent(
                            uiState = currentState,
                            listState = listState,
                            onAction = viewModel::onAction,
                        )
                    }
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
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 🔥 REVOLUTIONARY BANNER CAROUSEL
        item(key = "hero_carousel") {
            HeroBannerCarousel()
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

        // Promo Codes Grid
        items(
            items = uiState.promoCodes,
            key = { it.id.value },
        ) { promoCode ->
            EnhancedPromoCodeCard(
                promoCode = promoCode,
                onCardClick = {
                    onAction(HomeAction.PromoCodeClicked(promoCode))
                },
                onUpvoteClick = {
                    onAction(HomeAction.UpvotePromoCode(promoCode.id.value))
                },
                onCopyCodeClick = {
                    onAction(HomeAction.CopyPromoCode(promoCode))
                },
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.lg,
                    vertical = SpacingTokens.xs,
                ),
                onDownvoteClick = {
                    onAction(HomeAction.DownvotePromoCode(promoCode.id.value))
                },
            )
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
                        strokeWidth = 2.dp,
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
            text = "Oops! Something went wrong",
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
            text = "Try Again",
            variant = QodeButtonVariant.Primary,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No promo codes available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        Text(
            text = "Check back later for amazing deals and discounts!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// MARK: - Private Composables

@Composable
private fun WelcomeHeroSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(
            bottomStart = SpacingTokens.xl,
            bottomEnd = SpacingTokens.xl,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SpacingTokens.lg,
                vertical = SpacingTokens.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Status indicator
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(SpacingTokens.lg),
                modifier = Modifier.padding(bottom = SpacingTokens.sm),
            ) {
                Text(
                    text = stringResource(R.string.home_trending_badge),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.md,
                        vertical = SpacingTokens.xs,
                    ),
                )
            }

            Text(
                text = stringResource(R.string.home_welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
            )

            Text(
                text = stringResource(R.string.home_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )

            // Stats row
            Row(
                modifier = Modifier.padding(top = SpacingTokens.md),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                StatsItem(
                    value = stringResource(R.string.stats_active_deals),
                    label = stringResource(R.string.stats_active_deals_label),
                    icon = QodeCommerceIcons.Offer,
                )
                StatsItem(
                    value = stringResource(R.string.stats_happy_users),
                    label = stringResource(R.string.stats_happy_users_label),
                    icon = QodeStatusIcons.Verified,
                )
                StatsItem(
                    value = stringResource(R.string.stats_total_saved),
                    label = stringResource(R.string.stats_total_saved_label),
                    icon = QodeCommerceIcons.Cashback,
                )
            }
        }
    }
}

@Composable
private fun StatsItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(SpacingTokens.lg),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun FeaturedBannerSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.lg),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(SpacingTokens.lg),
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeCommerceIcons.Flash,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(SpacingTokens.xl),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SpacingTokens.md),
            ) {
                Text(
                    text = stringResource(R.string.home_featured_deals),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = stringResource(R.string.home_featured_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }

            Icon(
                imageVector = QodeActionIcons.Next,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(SpacingTokens.lg),
            )
        }
    }
}

@Composable
private fun FilterCarousel(
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    data class FilterItem(val id: String, val nameRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

    val filters = listOf(
        FilterItem("kaspi", R.string.filter_kaspi, QodeCommerceIcons.Voucher),
        FilterItem("highest", R.string.filter_top_rated, QodeStatusIcons.Gold),
        FilterItem("latest", R.string.filter_latest, QodeActionIcons.Up),
        FilterItem("food", R.string.filter_food, QodeCommerceIcons.ShoppingCart),
        FilterItem("fashion", R.string.filter_fashion, QodeActionIcons.Like),
        FilterItem("tech", R.string.filter_tech, QodeNavigationIcons.Settings),
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        items(filters) { filter ->
            FilterChip(
                nameRes = filter.nameRes,
                icon = filter.icon,
                onClick = { onFilterSelected(filter.id) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    nameRes: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val iconBackgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val iconTintColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(SpacingTokens.xl),
        color = backgroundColor,
        tonalElevation = if (isSelected) ElevationTokens.small else ElevationTokens.none,
        shadowElevation = if (isSelected) ElevationTokens.small else ElevationTokens.none,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = SpacingTokens.md,
                vertical = SpacingTokens.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Surface(
                shape = CircleShape,
                color = iconBackgroundColor,
                modifier = Modifier.size(SpacingTokens.xl),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(nameRes),
                        tint = iconTintColor,
                        modifier = Modifier.size(SpacingTokens.md),
                    )
                }
            }

            Text(
                text = stringResource(nameRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SectionHeader(
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = QodeActionIcons.Next,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(SpacingTokens.lg)
                        .padding(SpacingTokens.xs),
                )
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// MARK: - 🚀 REVOLUTIONARY COMPOSABLES

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
                    0 -> "Kaspi Gold" 1 -> "Wildberries" else -> "Lamoda"
                },
                gradientColors = when (index) {
                    0 -> listOf(0xFF6366F1, 0xFF8B5CF6)
                    1 -> listOf(0xFFEC4899, 0xFFF97316)
                    else -> listOf(0xFF10B981, 0xFF059669)
                },
            )
        }
    }
}

@Composable
private fun RevolutionaryBannerCard(
    title: String,
    discount: String,
    brand: String,
    gradientColors: List<Long>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(160.dp),
    ) {
        // Gradient Background with glass effect
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(SpacingTokens.lg),
            color = androidx.compose.ui.graphics.Color(gradientColors[0]).copy(alpha = 0.1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = gradientColors.map { androidx.compose.ui.graphics.Color(it) },
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(1000f, 1000f),
                        ),
                    ),
            ) {
                // Floating elements
                Icon(
                    imageVector = QodeCommerceIcons.Flash,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-20).dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SpacingTokens.lg),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Top section
                    Column {
                        Surface(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(SpacingTokens.sm),
                        ) {
                            Text(
                                text = stringResource(R.string.banner_limited_time),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(
                                    horizontal = SpacingTokens.sm,
                                    vertical = SpacingTokens.xs,
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.sm))

                        Text(
                            text = brand,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White,
                        )
                    }

                    // Bottom section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.banner_up_to),
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Text(
                                    text = discount,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = androidx.compose.ui.graphics.Color.White,
                                )
                                Text(
                                    text = stringResource(R.string.banner_off),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.padding(start = SpacingTokens.xs, bottom = SpacingTokens.xs),
                                )
                            }
                        }

                        Surface(
                            color = androidx.compose.ui.graphics.Color.White,
                            shape = RoundedCornerShape(SpacingTokens.lg),
                        ) {
                            Text(
                                text = stringResource(R.string.banner_claim_now),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color(gradientColors[0]),
                                modifier = Modifier.padding(
                                    horizontal = SpacingTokens.md,
                                    vertical = SpacingTokens.sm,
                                ),
                            )
                        }
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
        modifier = modifier.padding(vertical = SpacingTokens.lg),
    ) {
        Text(
            text = stringResource(R.string.home_quick_filters),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = SpacingTokens.lg,
                end = SpacingTokens.lg,
                bottom = SpacingTokens.lg,
            ),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            items(6) { index ->
                RevolutionaryFilterChip(
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
                        1 -> QodeStatusIcons.Gold
                        2 -> QodeActionIcons.Up
                        3 -> QodeCommerceIcons.ShoppingCart
                        4 -> QodeActionIcons.Like
                        else -> QodeNavigationIcons.Settings
                    },
                    onClick = { onFilterSelected("filter_$index") },
                    isSelected = index == 0,
                )
            }
        }
    }
}

@Composable
private fun RevolutionaryFilterChip(
    nameRes: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .width(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(SpacingTokens.lg),
        color = backgroundColor,
        tonalElevation = if (isSelected) ElevationTokens.medium else ElevationTokens.none,
        shadowElevation = if (isSelected) ElevationTokens.small else ElevationTokens.none,
    ) {
        Column(
            modifier = Modifier.padding(vertical = SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    contentColor.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(SpacingTokens.xl),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(nameRes),
                        tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(SpacingTokens.lg),
                    )
                }
            }

            Text(
                text = stringResource(nameRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(SpacingTokens.lg),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.md,
                        vertical = SpacingTokens.sm,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Icon(
                        imageVector = QodeActionIcons.Next,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(SpacingTokens.md),
                    )
                }
            }
        }
    }
}

// MARK: - Preview Functions

@Preview(name = "HomeScreen", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    QodeTheme {
        // Note: Preview would need mock data
        // HomeScreen()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Home Screen Preview")
        }
    }
}
