package com.example.home.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BannerCarousel(
    modifier: Modifier = Modifier,
    banners: List<String>,
    onBannerClick: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp), // slightly larger for better design
    ) {
        val bannerWidth = maxWidth * 0.4f // each banner takes 80% of screen width
        val spacing = 12.dp

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items(banners.size) { index ->
                BannerItem(
                    banner = banners[index],
                    width = bannerWidth,
                    onClick = { onBannerClick(banners[index]) },
                )
            }
        }
    }
}

@Composable
fun BannerItem(
    banner: String,
    width: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        // replaceable by an image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceTint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = banner,
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BannerCarouselPreview() {
    BannerCarousel(
        banners = listOf("Sale - 50%", "Promo Code: SAVE20", "Get Premium Free"),
        onBannerClick = {},
    )
}
