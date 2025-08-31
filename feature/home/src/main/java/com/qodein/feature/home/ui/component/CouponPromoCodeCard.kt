@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.qodein.feature.home.ui.component

import android.content.ClipData
import android.content.res.Configuration
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.core.ui.R
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.time.toJavaInstant

// Coupon-specific design tokens
private object CouponTokens {
    val cardHeight = 140.dp
    val stubWidth = 90.dp // Slightly wider for more content
    val iconSize = SizeTokens.Icon.sizeMedium

    // Coupon cutout dimensions
    val cutoutRadius = 12.dp
    val cutoutDiameter = cutoutRadius * 2

    // Colors will be determined from theme at runtime
}

// Custom coupon shape with actual cuts at perforation line
private class CouponShape(private val cornerRadius: Float, private val cutoutRadius: Float, private val stubWidthPx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val perforationX = width - stubWidthPx

            // Start from top-left
            moveTo(0f, cornerRadius)

            // Top-left corner
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Top edge to cutout start
            lineTo(perforationX - cutoutRadius, 0f)

            // TOP circular cutout (using cubicTo for precise control)
            cubicTo(
                x1 = perforationX - cutoutRadius / 2,
                y1 = cutoutRadius,
                x2 = perforationX + cutoutRadius / 2,
                y2 = cutoutRadius,
                x3 = perforationX + cutoutRadius,
                y3 = 0f,
            )

            // Continue top edge
            lineTo(width - cornerRadius, 0f)

            // Top-right corner
            arcTo(
                rect = Rect(width - 2 * cornerRadius, 0f, width, 2 * cornerRadius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Right edge
            lineTo(width, height - cornerRadius)

            // Bottom-right corner
            arcTo(
                rect = Rect(width - 2 * cornerRadius, height - 2 * cornerRadius, width, height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Bottom edge to cutout start
            lineTo(perforationX + cutoutRadius, height)

            // BOTTOM circular cutout (using cubicTo for precise control)
            cubicTo(
                x1 = perforationX + cutoutRadius / 2,
                y1 = height - cutoutRadius,
                x2 = perforationX - cutoutRadius / 2,
                y2 = height - cutoutRadius,
                x3 = perforationX - cutoutRadius,
                y3 = height,
            )

            // Continue bottom edge
            lineTo(cornerRadius, height)

            // Bottom-left corner
            arcTo(
                rect = Rect(0f, height - 2 * cornerRadius, 2 * cornerRadius, height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            // Left edge back to start
            lineTo(0f, cornerRadius)

            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * Coupon-style promo code card that looks like a real coupon with a detachable stub.
 * Features Material 3 theming, circular cutouts, working copy functionality, and enhanced accessibility.
 */
@Composable
fun CouponPromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.Scale.PRESSED else 1f,
        animationSpec = tween(durationMillis = MotionTokens.Duration.FAST),
        label = "coupon_card_scale",
    )

    // Determine discount display for the stub
    val discountText = when (promoCode) {
        is PromoCode.PercentagePromoCode -> "${promoCode.discountPercentage.toInt()}%\nOFF"
        is PromoCode.FixedAmountPromoCode -> "${promoCode.discountAmount.toInt()}\nKZT\nOFF"
    }

    // Theme-aware color scheme for better visual appeal
    val extendedColors = MaterialTheme.extendedColorScheme
    val (stubColor, stubGradient) = when (promoCode) {
        is PromoCode.PercentagePromoCode -> {
            val baseColor = MaterialTheme.colorScheme.primary
            baseColor to Brush.verticalGradient(
                listOf(baseColor, baseColor.copy(alpha = 0.8f)),
            )
        }
        is PromoCode.FixedAmountPromoCode -> {
            val baseColor = extendedColors.complementary
            baseColor to Brush.verticalGradient(
                listOf(baseColor, baseColor.copy(alpha = 0.8f)),
            )
        }
    }

    // Create custom coupon shape with actual cuts
    val density = LocalDensity.current
    val couponShape = remember {
        CouponShape(
            cornerRadius = with(density) { ShapeTokens.Corner.large.toPx() },
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
            containerColor = MaterialTheme.colorScheme.surface,
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
                        serviceName = promoCode.serviceName,
                        category = promoCode.category,
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.md))

                    // Clean promo code section (no ugly background)
                    PromoCodeRow(
                        code = promoCode.code,
                        onCopyClick = {
                            // Copy to clipboard using coroutine
                            coroutineScope.launch {
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("promo_code", promoCode.code)))
                            }
                            // Call the provided callback
                            onCopyCodeClick()
                        },
                    )
                }

                // Right section - Enhanced coupon stub with gradient
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
                        .background(brush = stubGradient),
                ) {
                    EnhancedStubContent(
                        discountText = discountText,
                        createdAt = promoCode.endDate,
                        upvotes = promoCode.upvotes,
                        downvotes = promoCode.downvotes,
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
    serviceName: String,
    category: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = serviceName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(SpacingTokens.sm))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Icon(
                imageVector = QodeCommerceIcons.Store,
                contentDescription = "active",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(SizeTokens.Icon.sizeSmall),
            )
            Icon(
                imageVector = QodeCommerceIcons.Store,
                contentDescription = "first user only",
                modifier = modifier.size(SizeTokens.Icon.sizeSmall),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = QodeCommerceIcons.Store,
                contentDescription = "verified",
                modifier = modifier.size(SizeTokens.Icon.sizeSmall),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                text = "PROMO CODE",
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

        QodeIconButton(
            onClick = onCopyClick,
            icon = QodeActionIcons.Copy,
            contentDescription = stringResource(R.string.copy_code),
            variant = QodeButtonVariant.Outlined,
            size = QodeButtonSize.Small,
        )
    }
}

// MARK: - Enhanced Stub Content with Rating and Date
@Composable
private fun EnhancedStubContent(
    discountText: String,
    createdAt: Instant,
    upvotes: Int,
    downvotes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(SpacingTokens.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top: Rating
        StubRating(
            upvotes = upvotes,
            downvotes = downvotes,
        )

        // Center: Discount with icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = discountText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
        }

        // Bottom: Date
        Text(
            text = formatLastUpdated(createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }
}

// MARK: - Simple Numerical Rating for Stub
@Composable
private fun StubRating(
    upvotes: Int,
    downvotes: Int,
    modifier: Modifier = Modifier
) {
    val netRating = upvotes - downvotes

    Text(
        text = if (netRating > 0) "+$netRating" else "$netRating",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

// MARK: - Utility Functions

@Composable
private fun formatLastUpdated(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    val date = instant.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return formatter.format(date)
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CouponPromoCodeCardPreview() {
    QodeTheme {
        Surface {
            val samplePercentagePromo = PromoCode.PercentagePromoCode(
                id = PromoCodeId("SAMPLE_PERCENTAGE"),
                code = "SAVE25",
                serviceName = "Food Delivery",
                category = "Marketplace",
                title = "25% Off Your Order",
                discountPercentage = 25.0,
                minimumOrderAmount = 5000.0,
                startDate = Clock.System.now(),
                endDate = Clock.System.now().plus(7.days),
                upvotes = 125,
                downvotes = 12,
                createdAt = Clock.System.now().minus(2.days),
            )

            CouponPromoCodeCard(
                promoCode = samplePercentagePromo,
                onCardClick = {},
                onCopyCodeClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
