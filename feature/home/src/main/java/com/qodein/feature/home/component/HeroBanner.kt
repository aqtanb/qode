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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.ModernPageIndicator
import com.qodein.core.designsystem.component.QodeDivider
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.BannerListPreviewParameterProvider
import com.qodein.core.ui.BannerPreviewParameterProvider
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.core.ui.component.ComingSoonDialog
import com.qodein.feature.home.R
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.getTranslatedCtaDescription
import com.qodein.shared.model.getTranslatedCtaTitle
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

/**
 * Section 1: Hero Banner Section
 * Contains country picker, promotional banner carousel, and call-to-action
 */
@Composable
fun HeroBannerSection(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    // Track pager state for page indicator - stabilize with banners.size
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember(banners.size) { mutableIntStateOf(banners.size) }

    // Debug banner recomposition
    val bannerKeys = banners.map {
        "${it.id.value}-${it.getTranslatedCtaTitle(userLanguage)}-${it.getTranslatedCtaDescription(userLanguage)}"
    }
    Timber.tag("HeroBannerSection").d("Recomposing with ${banners.size} banners, currentPage: $currentPage, totalPages: $totalPages")
    Timber.tag("HeroBannerSection").d("Banner keys: $bannerKeys")

    // Only use real banners from server, no fallback
    val bannerItems = banners

    // Get screen height for banner sizing
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }

    if (bannerItems.isNotEmpty()) {
        AutoScrollingBanner(
            items = bannerItems,
            autoScrollDelay = 4.seconds,
            onPagerStateChange = { page, total ->
                currentPage = page
                // Only update totalPages if it's a valid count and matches our banners
                if (total > 0 && total == banners.size) {
                    totalPages = total
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(screenHeight * 0.7f), // Banner takes 70% of screen height
        ) { banner, index ->
            HeroBannerContent(
                banner = banner,
                onBannerClick = onBannerClick,
                currentPage = currentPage,
                totalPages = totalPages,
                userLanguage = userLanguage,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        // Show placeholder banner that maintains same layout as real banners
        PlaceholderBannerState(
            modifier = modifier
                .fillMaxWidth()
                .height(screenHeight * 0.7f),
        )
    }
}

@Composable
private fun HeroBannerContent(
    banner: Banner,
    onBannerClick: (Banner) -> Unit,
    currentPage: Int,
    totalPages: Int,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    Timber.tag("HeroBannerContent").d("Recomposing banner content - ID: ${banner.id.value}, Page: $currentPage")

    // Use default brand color since gradientColors was removed
    val primaryGradientColor = Color(0xFF6366F1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onBannerClick(banner) }, // Make entire banner clickable
    ) {
        // Background: Always show image with proper error handling
        // Background layer - handle missing/failed images
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            if (banner.imageUrl.isBlank()) {
                Text(
                    text = stringResource(R.string.error_image_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Image layer - shows promotional banner content if available
        if (banner.imageUrl.isNotBlank()) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.getTranslatedCtaTitle(userLanguage),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Edge vignette - always show for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f), // Top edge
                            Color.Transparent, // Center stays clear
                            Color.Transparent, // More center clear
                            Color.Black.copy(alpha = 0.5f), // Bottom edge
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    ),
                ),
        )

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
                currentPage = currentPage,
                totalPages = totalPages,
                userLanguage = userLanguage,
            )
        }
    }
}

@Composable
private fun BannerCountryPicker(modifier: Modifier = Modifier) {
    var showComingSoonDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.country_kazakhstan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            showComingSoonDialog = true
                        }
                        .padding(horizontal = SpacingTokens.md),
                )
                QodeDivider(modifier = Modifier.weight(1f))
            }
        }
    }

    // Show dialog conditionally
    if (showComingSoonDialog) {
        ComingSoonDialog(
            title = stringResource(R.string.coming_soon_title),
            description = stringResource(R.string.coming_soon_country_description),
            onDismiss = { showComingSoonDialog = false },
            onTelegramClick = { showComingSoonDialog = false },
        )
    }
}

@Composable
private fun BannerCallToAction(
    banner: Banner,
    primaryColor: Color,
    onBannerClick: (Banner) -> Unit,
    currentPage: Int,
    totalPages: Int,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    val ctaTitle = banner.getTranslatedCtaTitle(userLanguage)
    val ctaDescription = banner.getTranslatedCtaDescription(userLanguage)

    Timber.tag("BannerCallToAction").d("Recomposing CTA - Title: $ctaTitle, Page: $currentPage/$totalPages")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = ctaTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                letterSpacing = 0.5.sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = ctaDescription,
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.25.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        // Modern page indicator - sexy 2024 style
        if (totalPages > 1) {
            ModernPageIndicator(
                currentPage = currentPage,
                totalPages = totalPages,
                modifier = Modifier.padding(top = SpacingTokens.xs),
                inactiveColor = Color.White.copy(alpha = 0.3f),
                activeColor = Color.White,
            )
        }
    }
}

@Composable
private fun PlaceholderBannerState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            ),
    ) {
        // Edge vignette - same as real banners for consistency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f), // Top edge
                            Color.Transparent, // Center stays clear
                            Color.Transparent, // More center clear
                            Color.Black.copy(alpha = 0.5f), // Bottom edge
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    ),
                ),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Country picker section - same position as real banners
            BannerCountryPicker()

            // Placeholder content where CTA section would be
            PlaceholderBannerCallToAction()
        }
    }
}

@Composable
private fun PlaceholderBannerCallToAction(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.empty_no_banners_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                letterSpacing = 0.5.sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.empty_no_banners_description),
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
private fun HeroBannerSectionPreview(@PreviewParameter(BannerListPreviewParameterProvider::class) banners: List<Banner>) {
    QodeTheme {
        HeroBannerSection(
            banners = banners,
            onBannerClick = { },
            userLanguage = Language.ENGLISH,
        )
    }
}

@Preview(name = "Hero Banner Section - Empty", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerSectionEmptyPreview() {
    QodeTheme {
        HeroBannerSection(
            banners = emptyList(),
            onBannerClick = { },
            userLanguage = Language.ENGLISH,
        )
    }
}

@Preview(name = "Placeholder Banner State", showBackground = true, heightDp = 400)
@Composable
private fun PlaceholderBannerStatePreview() {
    QodeTheme {
        PlaceholderBannerState()
    }
}

@Preview(name = "Hero Banner Content - Various Types", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerContentPreview(@PreviewParameter(BannerPreviewParameterProvider::class) banner: Banner) {
    QodeTheme {
        HeroBannerContent(
            banner = banner,
            onBannerClick = { },
            currentPage = 0,
            totalPages = 1,
            userLanguage = Language.ENGLISH,
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
private fun BannerCallToActionPreview(@PreviewParameter(BannerPreviewParameterProvider::class) banner: Banner) {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
        ) {
            BannerCallToAction(
                banner = banner,
                primaryColor = Color(0xFFDC2626),
                onBannerClick = { },
                currentPage = 2,
                totalPages = 4,
                userLanguage = Language.ENGLISH,
            )
        }
    }
}

@Preview(name = "Hero Banner - Dark Theme", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerDarkThemePreview(@PreviewParameter(BannerPreviewParameterProvider::class) banner: Banner) {
    QodeTheme(darkTheme = true) {
        HeroBannerContent(
            banner = banner,
            onBannerClick = { },
            currentPage = 3,
            totalPages = 5,
            userLanguage = Language.ENGLISH,
        )
    }
}
