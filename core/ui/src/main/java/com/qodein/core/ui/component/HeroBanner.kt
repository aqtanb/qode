package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Data class for hero banner items
 */
data class HeroBannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String? = null,
    val backgroundGradient: List<Color>,
    val promoCode: PromoCode? = null,
    val actionText: String = "View Details",
    val onActionClick: () -> Unit = {}
)

/**
 * HeroBanner component for featured content carousel
 *
 * @param items List of banner items to display
 * @param onItemClick Called when a banner item is clicked
 * @param modifier Modifier to be applied to the banner
 * @param autoScroll Whether to automatically scroll through items
 * @param autoScrollDelay Delay between auto-scroll transitions in milliseconds
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    items: List<HeroBannerItem>,
    onItemClick: (HeroBannerItem) -> Unit,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = true,
    autoScrollDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll effect
    LaunchedEffect(pagerState.currentPage, autoScroll) {
        if (autoScroll && items.size > 1) {
            delay(autoScrollDelay)
            val nextPage = (pagerState.currentPage + 1) % items.size
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = modifier) {
        // Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = QodeSpacing.md),
            pageSpacing = QodeSpacing.sm,
            pageSize = PageSize.Fill,
            modifier = Modifier.height(200.dp),
        ) { page ->
            val item = items[page]
            HeroBannerCard(
                item = item,
                onClick = { onItemClick(item) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Page indicators
        if (items.size > 1) {
            Spacer(modifier = Modifier.height(QodeSpacing.sm))
            PageIndicators(
                pageCount = items.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/**
 * Individual hero banner card
 */
@Composable
private fun HeroBannerCard(
    item: HeroBannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = item.backgroundGradient,
                    ),
                )
                .padding(QodeSpacing.md),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Header
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(QodeSpacing.xs))

                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    item.description?.let { desc ->
                        Spacer(modifier = Modifier.height(QodeSpacing.xs))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Action button and promo info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    item.promoCode?.let { promo ->
                        Column {
                            QodeBadge(
                                text = promo.code,
                                containerColor = Color.White.copy(alpha = 0.9f),
                                contentColor = item.backgroundGradient.first(),
                            )
                            if (promo.isVerified) {
                                Spacer(modifier = Modifier.height(QodeSpacing.xs))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = QodeStatusIcons.Verified,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                                    Text(
                                        text = "Verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    QodeButton(
                        onClick = item.onActionClick,
                        text = item.actionText,
                        variant = QodeButtonVariant.Primary,
                        size = QodeButtonSize.Small,
                    )
                }
            }
        }
    }
}

/**
 * Page indicators for the banner
 */
@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(QodeSpacing.xs),
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.4f,
                animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
                label = "indicator_alpha",
            )

            Surface(
                modifier = Modifier
                    .size(
                        width = if (isSelected) 20.dp else 8.dp,
                        height = 8.dp,
                    )
                    .alpha(alpha),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {}
        }
    }
}

/**
 * Compact hero banner for smaller spaces
 */
@Composable
fun CompactHeroBanner(
    item: HeroBannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier.height(120.dp),
        variant = QodeCardVariant.Elevated,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = item.backgroundGradient,
                    ),
                )
                .padding(QodeSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                item.promoCode?.let { promo ->
                    QodeBadge(
                        text = promo.code,
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = item.backgroundGradient.first(),
                    )
                }
            }
        }
    }
}

/**
 * Statistics banner for showcasing app metrics
 */
@Composable
fun StatsBanner(
    totalCodes: Int,
    activeUsers: Int,
    totalSavings: String,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Filled,
    ) {
        Column {
            Text(
                text = "Community Impact",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(QodeSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    value = totalCodes.toString(),
                    label = "Promo Codes",
                    icon = QodeCommerceIcons.PromoCode,
                )

                StatItem(
                    value = "${activeUsers}K",
                    label = "Active Users",
                    icon = QodeCommerceIcons.Store,
                )

                StatItem(
                    value = totalSavings,
                    label = "Total Savings",
                    icon = QodeCommerceIcons.Dollar,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
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
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.height(QodeSpacing.xs))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Sample data for preview
private fun getSampleBannerItems(): List<HeroBannerItem> {
    val sampleStore = Store(
        id = "kaspi",
        name = "Kaspi Bank",
        category = StoreCategory.Electronics,
    )

    val sampleCategory = Category(
        id = "electronics",
        name = "Electronics",
    )

    return listOf(
        HeroBannerItem(
            id = "1",
            title = "Weekend Special",
            subtitle = "Up to 50% off electronics",
            description = "Don't miss out on amazing deals this weekend",
            backgroundGradient = listOf(
                Color(0xFF6366F1),
                Color(0xFF8B5CF6),
            ),
            promoCode = PromoCode(
                id = "weekend50",
                code = "WEEKEND50",
                title = "Weekend Special",
                description = "50% off electronics",
                store = sampleStore,
                category = sampleCategory,
                discountPercentage = 50,
                isVerified = true,
                createdAt = LocalDateTime.now(),
                expiryDate = LocalDate.now().plusDays(2),
            ),
        ),
        HeroBannerItem(
            id = "2",
            title = "New User Bonus",
            subtitle = "20,000 KZT off your first order",
            description = "Welcome to Qode! Start saving immediately",
            backgroundGradient = listOf(
                Color(0xFFEC4899),
                Color(0xFFF97316),
            ),
            actionText = "Get Code",
        ),
        HeroBannerItem(
            id = "3",
            title = "Flash Sale",
            subtitle = "Limited time offers",
            description = "Hurry! These deals won't last long",
            backgroundGradient = listOf(
                Color(0xFF059669),
                Color(0xFF10B981),
            ),
        ),
    )
}

// Preview
@Preview(name = "HeroBanner", showBackground = true)
@Composable
private fun HeroBannerPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(vertical = QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.lg),
        ) {
            HeroBanner(
                items = getSampleBannerItems(),
                onItemClick = {},
            )

            CompactHeroBanner(
                item = getSampleBannerItems().first(),
                onClick = {},
                modifier = Modifier.padding(horizontal = QodeSpacing.md),
            )

            StatsBanner(
                totalCodes = 1250,
                activeUsers = 25,
                totalSavings = "2.5M ₸",
                modifier = Modifier.padding(horizontal = QodeSpacing.md),
            )
        }
    }
}
