package com.qodein.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AutoScrollingBanner
import com.qodein.core.ui.component.EmptyState
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.rememberBackdropBlurState
import com.qodein.core.ui.preview.BannerPreviewData
import com.qodein.feature.home.BannerState
import com.qodein.feature.home.R
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Language
import com.qodein.shared.model.getTranslatedCtaTitle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

private val BLUR_RADIUS = 16.dp
private const val BANNER_HEIGHT_PERCENTAGE = 0.3f

@Composable
fun HeroBannerSection(
    bannerState: BannerState,
    userLanguage: Language,
    onBannerClick: (Banner) -> Unit,
    onRetryBanners: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val bannerHeight = screenHeight * BANNER_HEIGHT_PERCENTAGE

    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            when (bannerState) {
                is BannerState.Error -> {
                    QodeErrorCard(
                        error = bannerState.error,
                        onRetry = onRetryBanners,
                        modifier = Modifier.padding(SpacingTokens.xs),
                    )
                }
                BannerState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(SpacingTokens.xs))
                }
                is BannerState.Success -> {
                    if (bannerState.banners.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.banner_empty_title),
                            description = stringResource(R.string.home_banner_empty_description),
                            modifier = Modifier.padding(SpacingTokens.xs),
                        )
                    } else {
                        BannerContent(
                            banners = bannerState.banners,
                            onBannerClick = onBannerClick,
                            userLanguage = userLanguage,
                            onPageChange = { page, total ->
                                currentPage = page
                                totalPages = total
                            },
                        )
                    }
                }
            }
        }

        if (totalPages > 1) {
            PageIndicator(
                currentIndex = currentPage,
                totalPages = totalPages,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = SpacingTokens.xs),
            )
        }
    }
}

@Composable
private fun BannerContent(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    userLanguage: Language,
    onPageChange: (page: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AutoScrollingBanner(
        items = banners,
        onPagerStateChange = { page, total ->
            if (total > 0 && total == banners.size) {
                onPageChange(page, total)
            }
        },
        key = { banner -> "${banner.id}-${userLanguage.code}" },
        modifier = modifier.fillMaxSize(),
    ) { banner, _ ->
        key(userLanguage) {
            BannerItem(
                banner = banner,
                onBannerClick = onBannerClick,
                userLanguage = userLanguage,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun BannerItem(
    banner: Banner,
    onBannerClick: (Banner) -> Unit,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    val hazeState = rememberBackdropBlurState()
    val ctaTitle = remember(userLanguage) { banner.getTranslatedCtaTitle(userLanguage) }

    Box(modifier = modifier) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = ctaTitle,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.7f to MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    ),
                ),
        )

        BannerCallToAction(
            ctaTitle = ctaTitle,
            onBannerClick = { onBannerClick(banner) },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SpacingTokens.md),
        )
    }
}

@Composable
private fun BannerCallToAction(
    ctaTitle: String,
    onBannerClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(ShapeTokens.Corner.full))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    tint = HazeTint(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    blurRadius = BLUR_RADIUS,
                ),
            )
            .clickable { onBannerClick() }
            .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ctaTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.width(SpacingTokens.xs))

        Icon(
            imageVector = NavigationIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
    }
}

@PreviewLightDark
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

@PreviewLightDark
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

@PreviewLightDark
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

@PreviewLightDark
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
