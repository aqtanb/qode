package com.qodein.core.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.theme.QodeTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Auto-scrolling banner component with infinite scroll and bi-directional swiping
 *
 * @param items List of items to display in the banner
 * @param modifier Modifier to be applied to the component
 * @param autoScrollDelay Delay between auto-scroll transitions
 * @param enableAutoScroll Whether auto-scroll is enabled
 * @param enableInfiniteScroll Whether infinite scroll is enabled
 * @param pageIndicatorEnabled Whether to show page indicators
 * @param content Composable content for each item
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AutoScrollingBanner(
    items: List<T>,
    modifier: Modifier = Modifier,
    autoScrollDelay: Duration = 3.seconds,
    enableAutoScroll: Boolean = true,
    enableInfiniteScroll: Boolean = true,
    pageIndicatorEnabled: Boolean = true,
    onPagerStateChange: ((currentPage: Int, totalPages: Int) -> Unit)? = null,
    content: @Composable (item: T, index: Int) -> Unit
) {
    // Handle edge cases
    if (items.isEmpty()) {
        return
    }

    val itemCount = items.size
    val pageCount = if (enableInfiniteScroll && itemCount > 1) {
        Int.MAX_VALUE
    } else {
        itemCount
    }

    // Start from middle of virtual range for smooth bi-directional swiping
    val initialPage = if (enableInfiniteScroll && itemCount > 1) {
        Int.MAX_VALUE / 2
    } else {
        0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount },
    )

    // Track user interaction state
    var userIsInteracting by remember { mutableStateOf(false) }

    // Monitor drag interactions to pause auto-scroll
    LaunchedEffect(pagerState) {
        pagerState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start -> userIsInteracting = true
                is DragInteraction.Stop, is DragInteraction.Cancel -> userIsInteracting = false
            }
        }
    }

    // Report pager state changes
    LaunchedEffect(pagerState.currentPage, itemCount) {
        if (itemCount > 0) {
            val actualPage = if (enableInfiniteScroll && itemCount > 1) {
                pagerState.currentPage % itemCount
            } else {
                pagerState.currentPage.coerceIn(0, itemCount - 1)
            }
            onPagerStateChange?.invoke(actualPage, itemCount)
        }
    }

    // Auto-scroll logic
    LaunchedEffect(enableAutoScroll, userIsInteracting, itemCount) {
        if (enableAutoScroll && !userIsInteracting && itemCount > 1) {
            while (true) {
                delay(autoScrollDelay)
                if (!userIsInteracting) {
                    val nextPage = pagerState.currentPage + 1
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(durationMillis = 1000),
                    )
                }
            }
        }
    }

    Column(modifier = modifier) {
        // Banner content
        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val actualIndex = if (enableInfiniteScroll && itemCount > 1) {
                    page % itemCount
                } else {
                    page.coerceIn(0, itemCount - 1)
                }
                content(items[actualIndex], actualIndex)
            }
        }
    }
}

// MARK: - Banner Configuration

/**
 * Banner configuration data class for home banners
 */
data class BannerConfig(
    val id: String,
    val brandName: String,
    val gradientScheme: QodeColorScheme,
    val discountPercent: Int,
    val primaryColor: Color
)

/**
 * Default banner configurations used in HomeScreen
 */
fun getDefaultBannerConfigs(): List<BannerConfig> =
    listOf(
        BannerConfig(
            id = "kaspi",
            brandName = "Kaspi Gold",
            gradientScheme = QodeColorScheme.BannerIndigo,
            discountPercent = 15,
            primaryColor = Color(0xFF6366F1),
        ),
        BannerConfig(
            id = "wildberries",
            brandName = "Wildberries",
            gradientScheme = QodeColorScheme.BannerPink,
            discountPercent = 30,
            primaryColor = Color(0xFFEC4899),
        ),
        BannerConfig(
            id = "lamoda",
            brandName = "Lamoda",
            gradientScheme = QodeColorScheme.BannerGreen,
            discountPercent = 45,
            primaryColor = Color(0xFF10B981),
        ),
        BannerConfig(
            id = "technodom",
            brandName = "Technodom",
            gradientScheme = QodeColorScheme.BannerOrange,
            discountPercent = 60,
            primaryColor = Color(0xFFF59E0B),
        ),
        BannerConfig(
            id = "sulpak",
            brandName = "Sulpak",
            gradientScheme = QodeColorScheme.BannerPurple,
            discountPercent = 75,
            primaryColor = Color(0xFF8B5CF6),
        ),
        BannerConfig(
            id = "magnum",
            brandName = "Magnum",
            gradientScheme = QodeColorScheme.BannerCyan,
            discountPercent = 90,
            primaryColor = Color(0xFF06B6D4),
        ),
    )

// MARK: - Preview Data

/**
 * Sample banner configuration for previews
 */
private data class SampleBannerItem(val id: String, val title: String, val backgroundColor: Color)

private val sampleBannerItems = listOf(
    SampleBannerItem("1", "Banner 1", Color(0xFF6366F1)),
    SampleBannerItem("2", "Banner 2", Color(0xFFEC4899)),
    SampleBannerItem("3", "Banner 3", Color(0xFF10B981)),
    SampleBannerItem("4", "Banner 4", Color(0xFFF59E0B)),
    SampleBannerItem("5", "Banner 5", Color(0xFF8B5CF6)),
)

/**
 * Sample banner content for previews
 */
@Composable
private fun SampleBannerContent(
    item: SampleBannerItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(item.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
    }
}

// MARK: - Previews

@Preview(
    name = "Auto-Scrolling Banner",
    showBackground = true,
    heightDp = 200,
)
@Composable
private fun AutoScrollingBannerPreview() {
    QodeTheme {
        AutoScrollingBanner(
            items = sampleBannerItems,
            autoScrollDelay = 2.seconds,
            modifier = Modifier.fillMaxWidth(),
        ) { item, _ ->
            SampleBannerContent(
                item = item,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(
    name = "Banner with No Auto-Scroll",
    showBackground = true,
    heightDp = 200,
)
@Composable
private fun ManualBannerPreview() {
    QodeTheme {
        AutoScrollingBanner(
            items = sampleBannerItems,
            enableAutoScroll = false,
            modifier = Modifier.fillMaxWidth(),
        ) { item, _ ->
            SampleBannerContent(
                item = item,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(
    name = "Single Item Banner",
    showBackground = true,
    heightDp = 200,
)
@Composable
private fun SingleItemBannerPreview() {
    QodeTheme {
        AutoScrollingBanner(
            items = listOf(sampleBannerItems.first()),
            modifier = Modifier.fillMaxWidth(),
        ) { item, _ ->
            SampleBannerContent(
                item = item,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(
    name = "Banner without Indicators",
    showBackground = true,
    heightDp = 200,
)
@Composable
private fun BannerWithoutIndicatorsPreview() {
    QodeTheme {
        AutoScrollingBanner(
            items = sampleBannerItems,
            pageIndicatorEnabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) { item, _ ->
            SampleBannerContent(
                item = item,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
