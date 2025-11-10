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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil.compose.AsyncImage
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.core.ui.component.BackdropBlurOverlay
import com.qodein.core.ui.component.rememberBackdropBlurState
import com.qodein.core.ui.error.asUiText
import com.qodein.core.ui.preview.BannerPreviewData
import com.qodein.feature.home.R
import com.qodein.feature.home.ui.state.BannerState
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.getTranslatedCtaDescription
import com.qodein.shared.model.getTranslatedCtaTitle
import dev.chrisbanes.haze.haze
import kotlin.time.Duration.Companion.seconds

// MARK: - Constants

private val BLUR_RADIUS = 16.dp
private const val BANNER_HEIGHT_PERCENTAGE = 0.5f
private const val TEXT_BACKGROUND_TOP_ALPHA = 0.6f
private const val TEXT_BACKGROUND_BOTTOM_ALPHA = 0.8f

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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                ctaTitle = stringResource(R.string.banner_loading_title),
                ctaDescription = stringResource(R.string.banner_loading_description),
                modifier = modifier,
            )
        }
        is BannerState.Success -> {
            if (bannerState.banners.isNotEmpty()) {
                BannerContent(
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
                            tint = MaterialTheme.colorScheme.onSurface,
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
                            imageVector = QodeUIIcons.Error,
                            contentDescription = null,
                            modifier = modifier.size(SizeTokens.Avatar.sizeLarge).padding(bottom = SpacingTokens.md),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )

                        QodeButton(
                            onClick = onRetryBanners,
                            text = stringResource(R.string.error_retry),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                ctaTitle = stringResource(R.string.banner_error_title),
                ctaDescription = bannerState.error.asUiText(),
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun BannerContent(
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
        key = { banner -> "${banner.id}-${userLanguage.code}" },
        modifier = modifier
            .fillMaxWidth()
            .height(screenHeight * BANNER_HEIGHT_PERCENTAGE),
    ) { banner, _ ->
        key(userLanguage) {
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
    Logger.d("BannerItem") { "Composing for banner ${banner.id} with language ${userLanguage.code}" }
    val hazeState = rememberBackdropBlurState()
    val ctaTitle = remember(userLanguage) {
        val title = banner.getTranslatedCtaTitle(userLanguage)
        Logger.d("BannerItem") { "Computed ctaTitle: $title for language ${userLanguage.code}" }
        title
    }
    val ctaDescription = remember(userLanguage) { banner.getTranslatedCtaDescription(userLanguage) }

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
                    contentDescription = ctaTitle,
                    modifier = modifier
                        .fillMaxSize()
                        .haze(hazeState),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        BackdropBlurOverlay(
            hazeState = hazeState,
            topAlpha = TEXT_BACKGROUND_TOP_ALPHA,
            bottomAlpha = TEXT_BACKGROUND_BOTTOM_ALPHA,
            blurRadius = BLUR_RADIUS,
            middleAreaWeight = CLEAR_IMAGE_WEIGHT,
            bottomAreaWeight = CTA_AREA_WEIGHT,
            overlayAlpha = 0f,
        )

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
                    ctaTitle = ctaTitle,
                    ctaDescription = ctaDescription,
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
            .background(MaterialTheme.colorScheme.surface),
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
                    ctaTitle = ctaTitle,
                    ctaDescription = ctaDescription,
                    currentPage = 0,
                    totalPages = 1,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun BannerCallToAction(
    ctaTitle: String,
    ctaDescription: String,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(SpacingTokens.sm),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (totalPages > 1) {
            PageIndicator(
                currentIndex = currentPage,
                totalPages = totalPages,
                inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = SpacingTokens.xs),
            )
        }

        Text(
            text = ctaTitle,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Text(
            text = ctaDescription,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// MARK: - Previews

@ThemePreviews
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

@ThemePreviews
@Composable
private fun BannerErrorStatePreview() {
    QodeTheme {
        HeroBannerSection(
            bannerState = BannerState.Error(
                error = FirestoreError.PermissionDenied,
            ),
            userLanguage = Language.ENGLISH,
            onBannerClick = { },
            onRetryBanners = { },
        )
    }
}

@ThemePreviews
@Composable
private fun BannerEmptyStatePreview() {
    QodeTheme {
        HeroBannerSection(
            bannerState = BannerState.Success(emptyList()),
            userLanguage = Language.ENGLISH,
            onBannerClick = { },
            onRetryBanners = { },
        )
    }
}

@ThemePreviews
@Composable
private fun BannerSuccessStatePreview() {
    QodeTheme {
        HeroBannerSection(
            bannerState = BannerState.Success(BannerPreviewData.allSamples),
            userLanguage = Language.ENGLISH,
            onBannerClick = { },
            onRetryBanners = { },
        )
    }
}
