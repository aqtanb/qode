package com.qodein.core.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    key: ((item: T) -> Any)? = null,
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
                key = if (key != null) {
                    { page ->
                        val actualIndex = if (enableInfiniteScroll && itemCount > 1) {
                            page % itemCount
                        } else {
                            page.coerceIn(0, itemCount - 1)
                        }
                        key(items[actualIndex])
                    }
                } else {
                    null
                },
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
