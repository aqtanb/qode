package com.example.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.home.components.BannerCarousel
import com.example.home.components.PromoCarousel
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(
            modifier = Modifier
                .height(QodeSpacing.sm),
        )

        BannerCarousel(
            banners = listOf("advertisement", "advertisement", "advertisement"),
            onBannerClick = { },
        )

        Spacer(
            modifier = Modifier
                .height(QodeSpacing.md),
        )

        PromoCarousel(
            promos = listOf("1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"),
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    QodeTheme {
        HomeScreen()
    }
}
