package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeChip
import com.qodein.core.designsystem.component.QodeChipSize
import com.qodein.core.designsystem.component.QodeChipVariant
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.component.QodeSectionHeader
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import kotlinx.coroutines.launch

/**
 * Tab options for following screen
 */
enum class FollowingTab(val title: String) {
    Stores("Stores"),
    Categories("Categories"),
    Discover("Discover")
}

/**
 * FollowingScreen component for managing followed stores and categories
 *
 * @param followedStores List of followed stores
 * @param followedCategories List of followed categories
 * @param suggestedStores List of suggested stores to follow
 * @param suggestedCategories List of suggested categories to follow
 * @param onStoreClick Called when a store is clicked
 * @param onCategoryClick Called when a category is clicked
 * @param onFollowStoreClick Called when follow/unfollow store is clicked
 * @param onFollowCategoryClick Called when follow/unfollow category is clicked
 * @param modifier Modifier to be applied to the component
 * @param selectedTab Currently selected tab
 * @param onTabSelected Called when a tab is selected
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowingScreen(
    followedStores: List<Store>,
    followedCategories: List<Category>,
    suggestedStores: List<Store>,
    suggestedCategories: List<Category>,
    onStoreClick: (Store) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onFollowStoreClick: (Store) -> Unit,
    onFollowCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: FollowingTab = FollowingTab.Stores,
    onTabSelected: (FollowingTab) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { FollowingTab.values().size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // Tab row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FollowingTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        onTabSelected(tab)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (FollowingTab.values()[page]) {
                FollowingTab.Stores -> {
                    FollowedStoresTab(
                        followedStores = followedStores,
                        onStoreClick = onStoreClick,
                        onFollowStoreClick = onFollowStoreClick,
                    )
                }
                FollowingTab.Categories -> {
                    FollowedCategoriesTab(
                        followedCategories = followedCategories,
                        onCategoryClick = onCategoryClick,
                        onFollowCategoryClick = onFollowCategoryClick,
                    )
                }
                FollowingTab.Discover -> {
                    DiscoverTab(
                        suggestedStores = suggestedStores,
                        suggestedCategories = suggestedCategories,
                        onStoreClick = onStoreClick,
                        onCategoryClick = onCategoryClick,
                        onFollowStoreClick = onFollowStoreClick,
                        onFollowCategoryClick = onFollowCategoryClick,
                    )
                }
            }
        }
    }
}

/**
 * Followed stores tab content
 */
@Composable
private fun FollowedStoresTab(
    followedStores: List<Store>,
    onStoreClick: (Store) -> Unit,
    onFollowStoreClick: (Store) -> Unit
) {
    if (followedStores.isEmpty()) {
        QodeEmptyState(
            icon = Icons.Default.Store,
            title = "No stores followed yet",
            description = "Follow your favorite stores to get personalized promo codes",
            action = {
                QodeButton(
                    onClick = { /* Navigate to discover */ },
                    text = "Discover Stores",
                    variant = QodeButtonVariant.Primary,
                )
            },
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        ) {
            items(followedStores) { store ->
                StoreCard(
                    store = store,
                    onStoreClick = { onStoreClick(store) },
                    onFollowClick = { onFollowStoreClick(store) },
                    isLoggedIn = true,
                    promoCodesCount = (5..25).random(), // TODO: Use real data
                )
            }
        }
    }
}

/**
 * Followed categories tab content
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FollowedCategoriesTab(
    followedCategories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onFollowCategoryClick: (Category) -> Unit
) {
    if (followedCategories.isEmpty()) {
        QodeEmptyState(
            icon = Icons.Default.Category,
            title = "No categories followed yet",
            description = "Follow categories to discover relevant promo codes",
            action = {
                QodeButton(
                    onClick = { /* Navigate to discover */ },
                    text = "Browse Categories",
                    variant = QodeButtonVariant.Primary,
                )
            },
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                ) {
                    followedCategories.forEach { category ->
                        QodeChip(
                            label = "${category.name} (${category.followersCount})",
                            onClick = { onCategoryClick(category) },
                            variant = QodeChipVariant.Input,
                            leadingIcon = category.icon,
                            onClose = { onFollowCategoryClick(category) },
                            size = QodeChipSize.Medium,
                        )
                    }
                }
            }

            items(followedCategories) { category ->
                CategoryDetailCard(
                    category = category,
                    onCategoryClick = { onCategoryClick(category) },
                    onFollowClick = { onFollowCategoryClick(category) },
                    activeCodesCount = (3..15).random(), // TODO: Use real data
                )
            }
        }
    }
}

/**
 * Discover tab content
 */
@Composable
private fun DiscoverTab(
    suggestedStores: List<Store>,
    suggestedCategories: List<Category>,
    onStoreClick: (Store) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onFollowStoreClick: (Store) -> Unit,
    onFollowCategoryClick: (Category) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(QodeSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
    ) {
        // Popular stores section
        if (suggestedStores.isNotEmpty()) {
            item {
                QodeSectionHeader(
                    title = "Popular Stores",
                    subtitle = "Join thousands of users following these stores",
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                    contentPadding = PaddingValues(horizontal = QodeSpacing.xs),
                ) {
                    items(suggestedStores.take(5)) { store ->
                        SuggestedStoreCard(
                            store = store,
                            onStoreClick = { onStoreClick(store) },
                            onFollowClick = { onFollowStoreClick(store) },
                            modifier = Modifier.width(200.dp),
                        )
                    }
                }
            }
        }

        // Popular categories section
        if (suggestedCategories.isNotEmpty()) {
            item {
                QodeSectionHeader(
                    title = "Popular Categories",
                    subtitle = "Discover codes in these trending categories",
                )
            }

            item {
                CategoryGrid(
                    categories = suggestedCategories,
                    onCategoryClick = onCategoryClick,
                    onFollowClick = onFollowCategoryClick,
                )
            }
        }

        // Trending section
        item {
            TrendingSection()
        }
    }
}

/**
 * Category detail card for followed categories
 */
@Composable
private fun CategoryDetailCard(
    category: Category,
    onCategoryClick: () -> Unit,
    onFollowClick: () -> Unit,
    activeCodesCount: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    QodeCard(
        modifier = modifier.animateContentSize(
            animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
        ),
        variant = QodeCardVariant.Outlined,
        onClick = onCategoryClick,
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(QodeCorners.md),
                        color = category.color.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(QodeSpacing.md))

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "$activeCodesCount active codes • ${category.followersCount} followers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QodeButton(
                        onClick = onFollowClick,
                        text = if (category.isFollowed) "Following" else "Follow",
                        variant = if (category.isFollowed) QodeButtonVariant.Outlined else QodeButtonVariant.Primary,
                        size = QodeButtonSize.Small,
                    )

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                        )
                    }
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(QodeAnimation.MEDIUM)),
                exit = shrinkVertically(animationSpec = tween(QodeAnimation.MEDIUM)),
            ) {
                Column(
                    modifier = Modifier.padding(top = QodeSpacing.md),
                ) {
                    Text(
                        text = "Recent activity in ${category.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(QodeSpacing.sm))

                    // Mock recent codes
                    repeat(3) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = QodeSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(QodeSpacing.sm))
                            Text(
                                text = "New code from Sample Store ${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${index + 1}h ago",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Suggested store card for discover section
 */
@Composable
private fun SuggestedStoreCard(
    store: Store,
    onStoreClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onStoreClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Store logo placeholder
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(QodeCorners.md),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(QodeSpacing.sm))

            Text(
                text = store.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "${store.followersCount} followers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(QodeSpacing.sm))

            QodeButton(
                onClick = onFollowClick,
                text = "Follow",
                variant = QodeButtonVariant.Primary,
                size = QodeButtonSize.Small,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Category grid for discover section
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGrid(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onFollowClick: (Category) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
    ) {
        categories.forEach { category ->
            QodeChip(
                label = "${category.name} (${category.followersCount})",
                onClick = { onCategoryClick(category) },
                variant = QodeChipVariant.Suggestion,
                leadingIcon = category.icon,
                size = QodeChipSize.Medium,
            )
        }
    }
}

/**
 * Trending section with stats
 */
@Composable
private fun TrendingSection() {
    QodeCard(
        variant = QodeCardVariant.Filled,
    ) {
        Column {
            QodeSectionHeader(
                title = "Trending This Week",
                subtitle = "Most popular follows and saves",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TrendingItem(
                    icon = Icons.Default.Store,
                    title = "Kaspi Bank",
                    subtitle = "+2.3K followers",
                )
                TrendingItem(
                    icon = Icons.Default.Category,
                    title = "Electronics",
                    subtitle = "+1.8K followers",
                )
                TrendingItem(
                    icon = Icons.Default.PersonAdd,
                    title = "New Users",
                    subtitle = "+856 this week",
                )
            }
        }
    }
}

@Composable
private fun TrendingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(QodeSpacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Sample data for preview
private fun getSampleFollowedStores(): List<Store> {
    return listOf(
        Store(
            id = "kaspi",
            name = "Kaspi Bank",
            category = StoreCategory.Electronics,
            followersCount = 15420,
            isFollowed = true,
        ),
        Store(
            id = "arbuz",
            name = "Arbuz.kz",
            category = StoreCategory.Food,
            followersCount = 8630,
            isFollowed = true,
        ),
        Store(
            id = "magnum",
            name = "Magnum",
            category = StoreCategory.Food,
            followersCount = 12100,
            isFollowed = true,
        ),
    )
}

private fun getSampleFollowedCategories(): List<Category> {
    return listOf(
        Category(
            id = "electronics",
            name = "Electronics",
            icon = Icons.Default.Category,
            followersCount = 5420,
            isFollowed = true,
        ),
        Category(
            id = "food",
            name = "Food & Drinks",
            icon = Icons.Default.Category,
            followersCount = 3210,
            isFollowed = true,
        ),
    )
}

// Preview
@Preview(name = "FollowingScreen", showBackground = true)
@Composable
private fun FollowingScreenPreview() {
    QodeTheme {
        FollowingScreen(
            followedStores = getSampleFollowedStores(),
            followedCategories = getSampleFollowedCategories(),
            suggestedStores = getSampleFollowedStores().map { it.copy(isFollowed = false) },
            suggestedCategories = getSampleFollowedCategories().map { it.copy(isFollowed = false) },
            onStoreClick = {},
            onCategoryClick = {},
            onFollowStoreClick = {},
            onFollowCategoryClick = {},
        )
    }
}
