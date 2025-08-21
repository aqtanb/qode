package com.qodein.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Simple placeholder SearchScreen - Coming Soon implementation
 */
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    TrackScreenViewEvent(screenName = "Search")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        contentAlignment = Alignment.Center,
    ) {
        QodeEmptyState(
            icon = Icons.Default.Search,
            title = "Search Feature",
            description = "Advanced search functionality is coming soon. Stay tuned for powerful promo code discovery tools!",
        )
    }
}

@Preview(name = "Search Screen Placeholder", showBackground = true)
@Composable
private fun SearchScreenPreview() {
    QodeTheme {
        SearchScreen()
    }
}

@Preview(name = "Search Screen Dark", showBackground = true)
@Composable
private fun SearchScreenDarkPreview() {
    QodeTheme(darkTheme = true) {
        SearchScreen()
    }
}
