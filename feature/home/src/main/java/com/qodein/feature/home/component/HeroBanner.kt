package com.qodein.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.QodeDivider
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Banner
import com.qodein.core.model.BannerId
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.feature.home.R
import kotlin.time.Duration.Companion.seconds

/**
 * Section 1: Hero Banner Section
 * Contains country picker, promotional banner carousel, and call-to-action
 */
@Composable
fun HeroBannerSection(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val bannerHeight = screenHeight * 0.7f

    // Only use real banners from server, no fallback
    val bannerItems = banners

    // Only show banner section if there are real banners
    if (bannerItems.isNotEmpty()) {
        AutoScrollingBanner(
            items = bannerItems,
            autoScrollDelay = 4.seconds,
            modifier = modifier
                .fillMaxWidth()
                .height(bannerHeight),
        ) { banner, index ->
            HeroBannerContent(
                banner = banner,
                onBannerClick = onBannerClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HeroBannerContent(
    banner: Banner,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse gradient colors for text styling
    val gradientColors = try {
        banner.gradientColors.map { hex ->
            Color(hex.toColorInt())
        }
    } catch (_: Exception) {
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    }

    val primaryGradientColor = gradientColors.firstOrNull() ?: Color(0xFF6366F1)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Background: Image with gradient overlay for text readability
        if (banner.imageUrl.isNotBlank()) {
            // Image layer
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Edge vignette - darkens only the edges, keeps center clear
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f), // Top edge
                                Color.Transparent, // Center stays clear
                                Color.Transparent, // Center stays clear
                                Color.Transparent, // More center clear
                                Color.Black.copy(alpha = 0.5f), // Bottom edge
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY,
                        ),
                    ),
            )
        } else {
            // Error state: No image URL or failed to load
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Icon(
                        imageVector = QodeUIIcons.Error,
                        contentDescription = "Image not found",
                        tint = Color.White,
                        modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                    )
                    Text(
                        text = "Image not available",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Country picker section - edge to edge including status bar
            BannerCountryPicker()

            // Brand information and CTA section - edge to edge
            BannerCallToAction(
                banner = banner,
                primaryColor = primaryGradientColor,
                onBannerClick = onBannerClick,
            )
        }
    }
}

@Composable
private fun BannerCountryPicker(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.01f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Text(
                text = stringResource(R.string.banner_country_picker_title),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
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
                        .padding(horizontal = SpacingTokens.md),
                )
                QodeDivider(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BannerCallToAction(
    banner: Banner,
    primaryColor: Color,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = banner.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                letterSpacing = 0.5.sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = banner.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.25.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

// MARK: - Preview Functions

@Preview(name = "Hero Banner Section - With Banners", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerSectionPreview() {
    QodeTheme {
        HeroBannerSection(
            banners = listOf(
                Banner(
                    id = BannerId("1"),
                    title = "Summer Sale 2024",
                    description = "Get up to 70% off on all summer collections",
                    imageUrl = "https://example.com/banner1.jpg",
                    targetCountries = listOf("KZ", "US"),
                    brandName = "Fashion Store",
                    ctaText = "Shop Now",
                    ctaUrl = "https://example.com/summer-sale",
                    gradientColors = listOf("#6366F1", "#8B5CF6"),
                    isActive = true,
                    priority = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
                Banner(
                    id = BannerId("2"),
                    title = "Flash Sale",
                    description = "Limited time offer - 24 hours only!",
                    imageUrl = "https://example.com/banner2.jpg",
                    targetCountries = listOf("KZ"),
                    brandName = "Electronics Hub",
                    ctaText = "Grab Now",
                    ctaUrl = "https://example.com/flash-sale",
                    gradientColors = listOf("#F59E0B", "#EF4444"),
                    isActive = true,
                    priority = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            ),
            onBannerClick = { },
        )
    }
}

@Preview(name = "Hero Banner Section - Empty", showBackground = true, heightDp = 200)
@Composable
private fun HeroBannerSectionEmptyPreview() {
    QodeTheme {
        HeroBannerSection(
            banners = emptyList(),
            onBannerClick = { },
        )
    }
}

@Preview(name = "Hero Banner Content - With Image", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerContentPreview() {
    QodeTheme {
        HeroBannerContent(
            banner = Banner(
                id = BannerId("1"),
                title = "Winter Collection",
                description = "Discover the latest trends in winter fashion",
                imageUrl = "https://example.com/winter-banner.jpg",
                targetCountries = listOf("KZ", "RU"),
                brandName = "Winter Fashion",
                ctaText = "Explore Collection",
                ctaUrl = "https://example.com/winter",
                gradientColors = listOf("#3B82F6", "#1E40AF"),
                isActive = true,
                priority = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
            onBannerClick = { },
        )
    }
}

@Preview(name = "Hero Banner Content - No Image", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerContentNoImagePreview() {
    QodeTheme {
        HeroBannerContent(
            banner = Banner(
                id = BannerId("1"),
                title = "Special Offer",
                description = "Don't miss out on this amazing deal",
                imageUrl = "", // No image URL
                targetCountries = listOf("KZ", "RU"),
                brandName = "Winter Fashion",
                ctaText = "Explore Collection",
                ctaUrl = "https://example.com/winter",
                gradientColors = listOf("#3B82F6", "#1E40AF"),
                isActive = true,
                priority = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
            onBannerClick = { },
        )
    }
}

@Preview(name = "Banner Country Picker", showBackground = true)
@Composable
private fun BannerCountryPickerPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
        ) {
            BannerCountryPicker()
        }
    }
}

@Preview(name = "Banner Call To Action", showBackground = true)
@Composable
private fun BannerCallToActionPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
        ) {
            BannerCallToAction(
                banner = Banner(
                    id = BannerId("1"),
                    title = "Black Friday Sale",
                    description = "Huge discounts on everything! Limited time only.",
                    imageUrl = "https://example.com/blackfriday.jpg",
                    targetCountries = listOf("KZ", "RU"),
                    brandName = "Winter Fashion",
                    ctaText = "Explore Collection",
                    ctaUrl = "https://example.com/winter",
                    gradientColors = listOf("#3B82F6", "#1E40AF"),
                    isActive = true,
                    priority = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
                primaryColor = Color(0xFFDC2626),
                onBannerClick = { },
            )
        }
    }
}

@Preview(name = "Hero Banner - Dark Theme", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerDarkThemePreview() {
    QodeTheme(darkTheme = true) {
        HeroBannerContent(
            banner = Banner(
                id = BannerId("1"),
                title = "Midnight Sale",
                description = "Exclusive dark mode deals just for you",
                imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755436194/main-sample.png",
                targetCountries = listOf("KZ", "RU"),
                brandName = "Winter Fashion",
                ctaText = "Explore Collection",
                ctaUrl = "https://example.com/winter",
                gradientColors = listOf("#3B82F6", "#1E40AF"),
                isActive = true,
                priority = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
            onBannerClick = { },
        )
    }
}
