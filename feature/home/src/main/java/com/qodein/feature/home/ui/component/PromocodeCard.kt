@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.qodein.feature.home.ui.component

import android.content.ClipData
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinOutlinedIconButton
import com.qodein.core.designsystem.component.ShimmerCircle
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.shape.CouponShape
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import com.qodein.core.ui.R as CoreUiR
import com.qodein.feature.home.R as HomeR

private object CouponTokens {
    val cardHeight = 140.dp
    val stubWidth = 90.dp
    val cutoutRadius = 12.dp
}

/**
 * Coupon-style promocode card that looks like a real coupon with a detachable stub.
 * Features Material 3 theming, circular cutouts, working copy functionality, and enhanced accessibility.
 */
@Composable
fun PromocodeCard(
    promocode: Promocode,
    onCardClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = tween(durationMillis = MotionTokens.Duration.FAST),
        label = "coupon_card_scale",
    )

    val discountText = when (val discount = promocode.discount) {
        is Discount.Percentage -> "${formatNumber(discount.value)} %"
        is Discount.FixedAmount -> "${formatNumber(discount.value)} ₸"
    }

    // Create custom coupon shape with actual cuts
    val density = LocalDensity.current
    val couponShape = remember {
        CouponShape(
            cornerRadius = with(density) { ShapeTokens.Corner.extraLarge.toPx() },
            cutoutRadius = with(density) { CouponTokens.cutoutRadius.toPx() },
            stubWidthPx = with(density) { CouponTokens.stubWidth.toPx() },
        )
    }

    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onCardClick()
            },
        shape = couponShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.medium,
            pressedElevation = ElevationTokens.small,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.small),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CouponTokens.cardHeight),
        ) {
            // Main coupon body with improved layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left section - Main content with optimized spacing
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(SpacingTokens.md),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Top section - Service name and category
                    CouponHeader(
                        promocode = promocode,
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.md))

                    // Clean promocode section (no ugly background)
                    PromoCodeRow(
                        code = promocode.code.value,
                        onCopyClick = {
                            // Copy to clipboard using coroutine
                            coroutineScope.launch {
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("promo_code", promocode.code.value)))
                            }
                            // Call the provided callback
                            onCopyCodeClick()
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .width(CouponTokens.stubWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                ) {
                    StubContent(
                        expiresAt = promocode.endDate,
                        discountText = discountText,
                        upvotes = promocode.upvotes,
                        downvotes = promocode.downvotes,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

// MARK: - Header Section
@Composable
private fun CouponHeader(
    promocode: Promocode,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularImage(
            imageUrl = promocode.serviceLogoUrl,
            fallbackIcon = QodeIcons.Service,
            contentDescription = stringResource(CoreUiR.string.cd_service_logo),
            size = SizeTokens.Icon.sizeMedium,
            modifier = Modifier.clip(CircleShape),
        )

        Spacer(modifier = Modifier.width(SpacingTokens.sm))

        Text(
            text = promocode.serviceName,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .alignByBaseline(),
        )

        Spacer(modifier = Modifier.width(SpacingTokens.sm))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            val isExpiringSoon = promocode.endDate < now.plus(3.days)
            if (isExpiringSoon) {
                Icon(
                    imageVector = UIIcons.Expiring,
                    contentDescription = stringResource(CoreUiR.string.cd_expiring_soon),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }

            if (promocode.isVerified) {
                Icon(
                    imageVector = PromocodeIcons.Verified,
                    contentDescription = stringResource(CoreUiR.string.cd_verified),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall),
                )
            }
        }
    }
}

@Composable
private fun PromoCodeRow(
    code: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(HomeR.string.promocode_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
            Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = modifier.width(SpacingTokens.md))

        QodeinOutlinedIconButton(
            onClick = onCopyClick,
            icon = QodeActionIcons.Copy,
            contentDescription = stringResource(CoreUiR.string.copy_code),
            size = ButtonSize.Small,
        )
    }
}

// MARK: - Stub Content
@Composable
private fun StubContent(
    discountText: String,
    expiresAt: Instant,
    upvotes: Int,
    downvotes: Int,
    modifier: Modifier = Modifier
) {
    val netRating = upvotes - downvotes
    Box(
        modifier = modifier.padding(SpacingTokens.xs),
    ) {
        Text(
            text = if (netRating > 0) "+$netRating" else "$netRating",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Text(
            text = discountText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )

        Text(
            text = rememberFormattedRelativeTime(expiresAt),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@ThemePreviews
@Composable
fun CouponPromoCodeCardPreview() {
    QodeTheme {
        Surface {
            PromocodeCard(
                promocode = PromocodePreviewData.percentagePromocode,
                onCardClick = {},
                onCopyCodeClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
fun PromocodeCardSkeleton(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val couponShape = remember {
        CouponShape(
            cornerRadius = with(density) { ShapeTokens.Corner.extraLarge.toPx() },
            cutoutRadius = with(density) { CouponTokens.cutoutRadius.toPx() },
            stubWidthPx = with(density) { CouponTokens.stubWidth.toPx() },
        )
    }

    Card(
        modifier = modifier,
        shape = couponShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationTokens.medium,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ElevationTokens.small),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CouponTokens.cardHeight),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(SpacingTokens.md),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        ShimmerLine(width = 120.dp, height = 16.dp)
                        Spacer(modifier = Modifier.height(SpacingTokens.xs))
                        ShimmerLine(width = 80.dp, height = 12.dp)
                    }

                    Spacer(modifier = Modifier.height(SpacingTokens.md))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        ShimmerLine(width = 100.dp, height = 14.dp)
                        ShimmerCircle(size = 20.dp)
                    }
                }

                Box(
                    modifier = Modifier
                        .width(CouponTokens.stubWidth)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = ShapeTokens.Corner.large,
                                bottomEnd = ShapeTokens.Corner.large,
                            ),
                        )
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ShimmerLine(width = 40.dp, height = 24.dp)
                        Spacer(modifier = Modifier.height(SpacingTokens.xs))
                        ShimmerLine(width = 30.dp, height = 10.dp)
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun CouponPromoCodeCardFixedAmountPreview() {
    QodeTheme {
        Surface {
            PromocodeCard(
                promocode = PromocodePreviewData.fixedAmountPromocode,
                onCardClick = {},
                onCopyCodeClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
fun CouponPromoCodeCardExpiringSoonPreview() {
    QodeTheme {
        Surface {
            PromocodeCard(
                promocode = PromocodePreviewData.expiringSoonPromocode,
                onCardClick = {},
                onCopyCodeClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
