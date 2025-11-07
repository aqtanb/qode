package com.qodein.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.core.ui.component.BackdropBlurOverlay
import com.qodein.core.ui.component.rememberBackdropBlurState
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.home.R
import com.qodein.feature.home.ui.state.BannerState
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.getTranslatedCtaDescription
import com.qodein.shared.model.getTranslatedCtaTitle
import dev.chrisbanes.haze.haze
import kotlin.time.Duration.Companion.seconds

// MARK: - Constants

private val LOADING_STROKE_WIDTH = 3.dp
private val BLUR_RADIUS = 16.dp
private const val ERROR_ICON_ALPHA = 0.6f
private const val EMPTY_ICON_ALPHA = 0.6f
private const val BANNER_HEIGHT_PERCENTAGE = 0.5f
private const val TEXT_BACKGROUND_TOP_ALPHA = 0.6f
private const val TEXT_BACKGROUND_BOTTOM_ALPHA = 0.8f
private const val INDICATOR_INACTIVE_ALPHA = 0.3f

// MARK: - Layout Proportions
private const val CLEAR_IMAGE_WEIGHT = 0.8f
private const val CTA_AREA_WEIGHT = 0.2f

// MARK: - Main Component

/**
 * Hero Banner Section with state-based rendering
 * Maintains consistent banner structure across Loading, Success, Error, and Empty states
 */
@Composable
fun HeroBannerSection(
    bannerState: BannerState,
    userLanguage: Language,
    onBannerClick: (Banner) -> Unit,
    onRetryBanners: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (bannerState) {
        BannerState.Loading -> {
            BannerStructure(
                imageContent = {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = LOADING_STROKE_WIDTH,
                    )
                },
                ctaTitle = stringResource(R.string.banner_loading_title),
                ctaDescription = stringResource(R.string.banner_loading_description),
                modifier = modifier,
            )
        }
        is BannerState.Success -> {
            if (bannerState.banners.isNotEmpty()) {
                SuccessBannerContent(
                    banners = bannerState.banners,
                    onBannerClick = onBannerClick,
                    userLanguage = userLanguage,
                    modifier = modifier,
                )
            } else {
                BannerStructure(
                    imageContent = {
                        Icon(
                            imageVector = QodeBusinessIcons.Asset,
                            contentDescription = null,
                            modifier = modifier.size(SizeTokens.Avatar.sizeLarge),
                            tint = Color.White.copy(alpha = EMPTY_ICON_ALPHA),
                        )
                    },
                    ctaTitle = stringResource(R.string.banner_empty_title),
                    ctaDescription = stringResource(R.string.banner_empty_description),
                    modifier = modifier,
                )
            }
        }
        is BannerState.Error -> {
            BannerStructure(
                imageContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        Icon(
                            imageVector = QodeBusinessIcons.Asset,
                            contentDescription = null,
                            modifier = modifier.size(SizeTokens.Avatar.sizeLarge),
                            tint = Color.White.copy(alpha = ERROR_ICON_ALPHA),
                        )

                        if (bannerState.isRetryable) {
                            QodeButton(
                                onClick = onRetryBanners,
                                text = stringResource(R.string.error_retry),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                },
                ctaTitle = stringResource(R.string.banner_error_title),
                ctaDescription = bannerState.errorType.asUiText(),
                modifier = modifier,
            )
        }
    }
}

// MARK: - Success State Components

@Composable
private fun SuccessBannerContent(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember(banners.size) { mutableIntStateOf(banners.size) }

    val screenHeight = LocalWindowInfo.current.let { windowInfo ->
        with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }
    }

    AutoScrollingBanner(
        items = banners,
        autoScrollDelay = 4.seconds,
        onPagerStateChange = { page, total ->
            currentPage = page
            if (total > 0 && total == banners.size) {
                totalPages = total
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(screenHeight * BANNER_HEIGHT_PERCENTAGE),
    ) { banner, _ ->
        BannerItem(
            banner = banner,
            onBannerClick = onBannerClick,
            currentPage = currentPage,
            totalPages = totalPages,
            userLanguage = userLanguage,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun BannerItem(
    banner: Banner,
    onBannerClick: (Banner) -> Unit,
    currentPage: Int,
    totalPages: Int,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    val hazeState = rememberBackdropBlurState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onBannerClick(banner) },
    ) {
        // Layer 1: The Image with Haze blur source
        Box(
            modifier = modifier
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
            } else {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.getTranslatedCtaTitle(userLanguage),
                    modifier = modifier
                        .fillMaxSize()
                        .haze(hazeState),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // Layer 2: Removed separate black overlay - using component-level backgrounds instead

        // Layer 3: Enterprise-level Backdrop Blur Overlay (no black overlay)
        BackdropBlurOverlay(
            hazeState = hazeState,
            topAlpha = TEXT_BACKGROUND_TOP_ALPHA,
            bottomAlpha = TEXT_BACKGROUND_BOTTOM_ALPHA,
            blurRadius = BLUR_RADIUS,
            middleAreaWeight = CLEAR_IMAGE_WEIGHT,
            bottomAreaWeight = CTA_AREA_WEIGHT,
            overlayAlpha = 0f, // No black overlay here since we added it separately
        )

        // Layer 4: The Content with matching proportions
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.weight(CLEAR_IMAGE_WEIGHT))
            Box(
                modifier = Modifier
                    .weight(CTA_AREA_WEIGHT)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                BannerCallToAction(
                    ctaTitle = banner.getTranslatedCtaTitle(userLanguage),
                    ctaDescription = banner.getTranslatedCtaDescription(userLanguage),
                    currentPage = currentPage,
                    totalPages = totalPages,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// MARK: - State Structure Component

@Composable
private fun BannerStructure(
    imageContent: @Composable () -> Unit,
    ctaTitle: String,
    ctaDescription: String,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalWindowInfo.current.let { windowInfo ->
        with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(screenHeight * BANNER_HEIGHT_PERCENTAGE)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        // Center image content
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            imageContent()
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            BannerCallToAction(
                ctaTitle = ctaTitle,
                ctaDescription = ctaDescription,
                currentPage = 0,
                totalPages = 1,
            )
        }
    }
}

@Composable
private fun BannerCallToAction(
    ctaTitle: String,
    ctaDescription: String,
    currentPage: Int,
    totalPages: Int,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(SpacingTokens.sm),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (totalPages > 1) {
            PageIndicator(
                currentIndex = currentPage,
                totalPages = totalPages,
                inactiveColor = textColor.copy(alpha = INDICATOR_INACTIVE_ALPHA),
                activeColor = textColor,
                modifier = Modifier.padding(bottom = SpacingTokens.xs),
            )
        }

        Text(
            text = ctaTitle,
            style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.5.sp),
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Text(
            text = ctaDescription,
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.25.sp),
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// MARK: - Previews

@Preview(name = "Hero Banner Loading", showBackground = true, heightDp = 400)
@Composable
private fun HeroBannerLoadingPreview() {
    QodeTheme {
        HeroBannerSection(
            bannerState = BannerState.Loading,
            userLanguage = Language.ENGLISH,
            onBannerClick = { },
            onRetryBanners = { },
        )
    }
}
