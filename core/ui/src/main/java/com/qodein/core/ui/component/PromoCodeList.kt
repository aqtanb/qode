package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeCardSkeleton
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.component.QodeErrorState
import com.qodein.core.designsystem.component.QodeLoadingContent
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.take

/**
 * Data class representing the state of the promo codes list
 */
sealed class PromoCodeListState {
    object Loading : PromoCodeListState()
    data class Success(val promoCodes: List<PromoCode>) : PromoCodeListState()
    data class Error(val message: String) : PromoCodeListState()
    object Empty : PromoCodeListState()
}

/**
 * PromoCodeList component that displays a list of promo codes with loading states
 *
 * @param state The current state of the list
 * @param onPromoCodeClick Called when a promo code is clicked
 * @param onUpvoteClick Called when upvote button is clicked
 * @param onFollowStoreClick Called when follow store button is clicked
 * @param onCopyCodeClick Called when copy code button is clicked
 * @param onRefresh Called when pull-to-refresh is triggered
 * @param onLoadMore Called when more items need to be loaded
 * @param modifier Modifier to be applied to the list
 * @param isLoggedIn Whether the user is logged in
 * @param contentPadding Padding for the list content
 * @param isRefreshing Whether refresh is in progress
 * @param hasMoreItems Whether there are more items to load
 * @param isLoadingMore Whether more items are being loaded
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCodeList(
    state: PromoCodeListState,
    onPromoCodeClick: (PromoCode) -> Unit,
    onUpvoteClick: (PromoCode) -> Unit,
    onFollowStoreClick: (Store) -> Unit,
    onCopyCodeClick: (PromoCode) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(QodeSpacing.md),
    isRefreshing: Boolean = false,
    hasMoreItems: Boolean = false,
    isLoadingMore: Boolean = false
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    // Check if we need to load more items when scrolling near the end
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            hasMoreItems && !isLoadingMore && lastVisibleItemIndex > (totalItemsNumber - 3)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier,
    ) {
        when (state) {
            is PromoCodeListState.Loading -> {
                LoadingContent(contentPadding = contentPadding)
            }

            is PromoCodeListState.Success -> {
                if (state.promoCodes.isEmpty()) {
                    EmptyContent()
                } else {
                    SuccessContent(
                        promoCodes = state.promoCodes,
                        onPromoCodeClick = onPromoCodeClick,
                        onUpvoteClick = onUpvoteClick,
                        onFollowStoreClick = onFollowStoreClick,
                        onCopyCodeClick = onCopyCodeClick,
                        isLoggedIn = isLoggedIn,
                        contentPadding = contentPadding,
                        listState = listState,
                        isLoadingMore = isLoadingMore,
                    )
                }
            }

            is PromoCodeListState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = onRefresh,
                )
            }

            is PromoCodeListState.Empty -> {
                EmptyContent()
            }
        }
    }
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
    ) {
        items(3) {
            QodeCardSkeleton()
        }
    }
}

@Composable
private fun SuccessContent(
    promoCodes: List<PromoCode>,
    onPromoCodeClick: (PromoCode) -> Unit,
    onUpvoteClick: (PromoCode) -> Unit,
    onFollowStoreClick: (Store) -> Unit,
    onCopyCodeClick: (PromoCode) -> Unit,
    isLoggedIn: Boolean,
    contentPadding: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isLoadingMore: Boolean
) {
    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
    ) {
        items(
            items = promoCodes,
            key = { it.id },
        ) { promoCode ->
            PromoCodeCard(
                promoCode = promoCode,
                onCardClick = { onPromoCodeClick(promoCode) },
                onUpvoteClick = { onUpvoteClick(promoCode) },
                onFollowStoreClick = { onFollowStoreClick(promoCode.store) },
                onCopyCodeClick = { onCopyCodeClick(promoCode) },
                isLoggedIn = isLoggedIn,
            )
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QodeSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    QodeLoadingContent(message = "Loading more...")
                }
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeEmptyState(
            icon = Icons.Default.Search,
            title = "No promo codes found",
            description = "Try adjusting your filters or check back later for new codes",
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeErrorState(
            message = message,
            onRetry = onRetry,
        )
    }
}

/**
 * Compact version for use in smaller spaces
 */
@Composable
fun CompactPromoCodeList(
    promoCodes: List<PromoCode>,
    onPromoCodeClick: (PromoCode) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    maxItems: Int = 5
) {
    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = QodeSpacing.md,
                    vertical = QodeSpacing.sm,
                ),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        ) {
            promoCodes.take(maxItems).forEach { promoCode ->
                PromoCodeCard(
                    promoCode = promoCode,
                    onCardClick = { onPromoCodeClick(promoCode) },
                    onUpvoteClick = { },
                    onFollowStoreClick = { },
                    onCopyCodeClick = { },
                    modifier = Modifier.padding(horizontal = QodeSpacing.md),
                )
            }
        }
    }
}

// Sample data for preview
private fun getSamplePromoCodes(): List<PromoCode> {
    val sampleStore = Store(
        id = "kaspi",
        name = "Kaspi Bank",
        category = StoreCategory.Electronics,
        followersCount = 1250,
    )

    val sampleCategory = Category(
        id = "electronics",
        name = "Electronics",
    )

    return listOf(
        PromoCode(
            id = "1",
            code = "SAVE20",
            title = "20% off all electronics",
            description = "Get amazing discounts on laptops, phones, and more. Limited time offer!",
            store = sampleStore,
            category = sampleCategory,
            discountPercentage = 20,
            minimumOrderAmount = 50000,
            upvotes = 15,
            isVerified = true,
            createdAt = LocalDateTime.now().minusHours(2),
            expiryDate = LocalDate.now().plusDays(5),
        ),
        PromoCode(
            id = "2",
            code = "FIRST50",
            title = "50% off first order",
            description = "Special discount for new customers",
            store = sampleStore.copy(name = "Arbuz.kz", isFollowed = true),
            category = sampleCategory,
            discountPercentage = 50,
            isFirstOrderOnly = true,
            isSingleUse = true,
            upvotes = 8,
            isUpvoted = true,
            createdAt = LocalDateTime.now().minusDays(1),
        ),
        PromoCode(
            id = "3",
            code = "MEGA10K",
            title = "10,000 KZT off orders above 100,000 KZT",
            description = "Huge savings on big orders",
            store = sampleStore.copy(name = "Magnum"),
            category = sampleCategory,
            discountAmount = 10000,
            minimumOrderAmount = 100000,
            upvotes = 25,
            isVerified = true,
            createdAt = LocalDateTime.now().minusHours(6),
        ),
    )
}

// Preview
@Preview(name = "PromoCodeList States", showBackground = true)
@Composable
private fun PromoCodeListPreview() {
    QodeTheme {
        val samplePromoCodes = getSamplePromoCodes()

        PromoCodeList(
            state = PromoCodeListState.Success(samplePromoCodes),
            onPromoCodeClick = {},
            onUpvoteClick = {},
            onFollowStoreClick = {},
            onCopyCodeClick = {},
            onRefresh = {},
            onLoadMore = {},
            isLoggedIn = true,
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun PromoCodeListEmptyPreview() {
    QodeTheme {
        PromoCodeList(
            state = PromoCodeListState.Empty,
            onPromoCodeClick = {},
            onUpvoteClick = {},
            onFollowStoreClick = {},
            onCopyCodeClick = {},
            onRefresh = {},
            onLoadMore = {},
        )
    }
}

@Preview(name = "Compact List", showBackground = true)
@Composable
private fun CompactPromoCodeListPreview() {
    QodeTheme {
        CompactPromoCodeList(
            promoCodes = getSamplePromoCodes(),
            onPromoCodeClick = {},
            title = "Popular This Week",
            maxItems = 2,
        )
    }
}
