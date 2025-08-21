package com.qodein.core.ui.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.qodein.core.ui.R
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days

// Coupon-specific design tokens
private object CouponTokens {
    val cardHeight = 140.dp
    val stubWidth = 90.dp // Slightly wider for more content
    val dividerWidth = 4.dp
    val iconSize = SizeTokens.Icon.sizeMedium
    val dashInterval = floatArrayOf(6f, 4f)

    // Coupon cutout dimensions
    val cutoutRadius = 8.dp
    val cutoutDiameter = cutoutRadius * 2

    // Vibrant colors for better UX
    val percentageColor = Color(0xFF16A34A) // Success green
    val fixedAmountColor = Color(0xFFEA580C) // Warning orange
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

            // Top edge to perforation
            lineTo(perforationX - cutoutRadius, 0f)

            // Top triangle cutout at perforation line - taller
            lineTo(perforationX, cutoutRadius * 2)
            lineTo(perforationX + cutoutRadius, 0f)

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

            // Bottom edge to perforation
            lineTo(perforationX + cutoutRadius, height)

            // Bottom triangle cutout at perforation line - taller
            lineTo(perforationX, height - cutoutRadius * 2)
            lineTo(perforationX - cutoutRadius, height)

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
 * Features improved layout, visible perforated divider, and vibrant color scheme.
 */
@Composable
fun CouponPromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Vibrant color scheme for better visual appeal
    val (stubColor, stubGradient) = when (promoCode) {
        is PromoCode.PercentagePromoCode ->
            CouponTokens.percentageColor to
                Brush.verticalGradient(listOf(CouponTokens.percentageColor, CouponTokens.percentageColor.copy(alpha = 0.8f)))
        is PromoCode.FixedAmountPromoCode ->
            CouponTokens.fixedAmountColor to
                Brush.verticalGradient(listOf(CouponTokens.fixedAmountColor, CouponTokens.fixedAmountColor.copy(alpha = 0.8f)))
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
                    CouponHeaderSection(
                        serviceName = promoCode.serviceName,
                        category = promoCode.category,
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.md))

                    // Clean promo code section (no ugly background)
                    CleanCodeSection(
                        code = promoCode.code,
                        onCopyClick = onCopyCodeClick,
                    )
                }

                // Enhanced perforated divider aligned with cutouts
                EnhancedPerforatedDivider(
                    modifier = Modifier
                        .width(CouponTokens.dividerWidth)
                        .fillMaxHeight()
                        .padding(vertical = CouponTokens.cutoutDiameter)
                        .offset(x = 3.dp), // Move slightly right to align with cutouts
                )

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
                        createdAt = promoCode.createdAt,
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
private fun CouponHeaderSection(
    serviceName: String,
    category: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = serviceName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        category?.let { categoryText ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(ShapeTokens.Corner.small),
                modifier = Modifier.padding(start = SpacingTokens.sm),
            ) {
                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.sm,
                        vertical = SpacingTokens.xs,
                    ),
                )
            }
        }
    }
}

// MARK: - Clean Code Section (no background)
@Composable
private fun CleanCodeSection(
    code: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "PROMO CODE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
            Text(
                text = code,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        QodeIconButton(
            onClick = onCopyClick,
            icon = QodeActionIcons.Copy,
            contentDescription = stringResource(R.string.copy_code),
            variant = QodeButtonVariant.Primary,
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
            Icon(
                imageVector = QodeCommerceIcons.Coupon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(CouponTokens.iconSize),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xs))

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

// MARK: - Enhanced Perforated Divider
@Composable
private fun EnhancedPerforatedDivider(modifier: Modifier = Modifier) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)

    Canvas(modifier = modifier) {
        drawPerforatedLine(
            color = dividerColor,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = CouponTokens.dividerWidth.toPx(),
        )
    }
}

private fun DrawScope.drawPerforatedLine(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    val pathEffect = PathEffect.dashPathEffect(
        intervals = CouponTokens.dashInterval,
        phase = 0f,
    )

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = pathEffect,
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
                category = "Restaurant",
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
