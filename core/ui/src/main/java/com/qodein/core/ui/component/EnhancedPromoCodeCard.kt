package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeGradientStyle
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.PromoCode
import com.qodein.core.model.UserId
import com.qodein.core.ui.R
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days

/**
 * Beautiful PromoCode card designed like an actual promotional card
 */
@Composable
fun EnhancedPromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "card_scale",
    )

    // Dynamic gradient style based on promo type using design system
    val gradientStyle = when (promoCode) {
        is PromoCode.PercentagePromoCode -> QodeGradientStyle.Primary
        is PromoCode.FixedAmountPromoCode -> QodeGradientStyle.Secondary
        is PromoCode.PromoPromoCode -> QodeGradientStyle.Tertiary
    }

    val gradientColors = when (gradientStyle) {
        QodeGradientStyle.Primary -> listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )
        QodeGradientStyle.Secondary -> listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
        )
        QodeGradientStyle.Tertiary -> listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
        )
        QodeGradientStyle.Hero -> listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        )
    }

    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onCardClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
        ) {
            // Top section: Service name and discount badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = promoCode.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                // Enhanced discount badge with minimum order info
                Text(
                    text = when (promoCode) {
                        is PromoCode.PercentagePromoCode -> {
                            val minOrder = promoCode.minimumOrderAmount?.toInt()
                            if (minOrder != null && minOrder > 0) {
                                stringResource(
                                    R.string.percentage_off_order,
                                    promoCode.discountPercentage.toInt(),
                                    minOrder,
                                )
                            } else {
                                stringResource(
                                    R.string.percentage_discount,
                                    promoCode.discountPercentage.toInt(),
                                )
                            }
                        }
                        is PromoCode.FixedAmountPromoCode -> {
                            val minOrder = promoCode.minimumOrderAmount?.toInt()
                            if (minOrder != null && minOrder > 0) {
                                stringResource(
                                    R.string.amount_off_order,
                                    promoCode.discountAmount.toInt(),
                                    minOrder,
                                )
                            } else {
                                stringResource(
                                    R.string.fixed_amount_discount,
                                    promoCode.discountAmount.toInt(),
                                )
                            }
                        }
                        is PromoCode.PromoPromoCode -> stringResource(R.string.promo_type)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Middle section: Promo code with dashed border (coupon style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp),
                    )
                    .padding(SpacingTokens.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "CODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )

                    Text(
                        text = promoCode.code,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                QodeIconButton(
                    onClick = onCopyCodeClick,
                    icon = QodeActionIcons.Copy,
                    contentDescription = stringResource(R.string.copy_code),
                    variant = QodeButtonVariant.Primary,
                    size = QodeButtonSize.Small,
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Bottom section: Votes and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatLastUpdated(promoCode.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                VotingSection(
                    upvotes = promoCode.upvotes,
                    downvotes = promoCode.downvotes,
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick,
                )
            }
        }
    }
}

/**
 * Voting section with upvote and downvote buttons
 */
@Composable
private fun VotingSection(
    upvotes: Int,
    downvotes: Int,
    onUpvoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Upvote button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onUpvoteClick() }
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = QodeActionIcons.Thumbs,
                contentDescription = stringResource(R.string.upvotes),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = upvotes.toString(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Downvote button - always show if there are downvotes
        if (downvotes > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDownvoteClick() }
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = QodeActionIcons.ThumbsDown,
                    contentDescription = stringResource(R.string.downvotes),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(14.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = downvotes.toString(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun formatLastUpdated(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return formatter.format(date)
}

// Preview
@Preview(name = "Beautiful PromoCode Cards", showBackground = true)
@Composable
private fun EnhancedPromoCodeCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val now = Clock.System.now()

            // Percentage PromoCode
            EnhancedPromoCodeCard(
                promoCode = PromoCode.createPercentage(
                    code = "NETFLIX30",
                    serviceName = "Netflix",
                    discountPercentage = 30.0,
                    maximumDiscount = 15.0,
                    category = "Streaming",
                    title = "30% off Netflix Premium",
                    description = "Get 30% discount on your first 3 months",
                    createdBy = UserId("user1"),
                ).getOrThrow().copy(
                    createdAt = now.minus(2.days).toJavaInstant(),
                    updatedAt = now.minus(1.days).toJavaInstant(),
                    upvotes = 45,
                    downvotes = 3,
                    views = 234,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onDownvoteClick = {},
                onCopyCodeClick = {},
            )

            // Fixed Amount PromoCode
            EnhancedPromoCodeCard(
                promoCode = PromoCode.createFixedAmount(
                    code = "KASPI500",
                    serviceName = "Kaspi",
                    discountAmount = 500.0,
                    category = "Shopping",
                    title = "500 KZT off",
                    description = "Get 500 KZT discount on orders above 5000 KZT",
                    createdBy = UserId("user2"),
                ).getOrThrow().copy(
                    createdAt = now.minus(1.days).toJavaInstant(),
                    updatedAt = now.toJavaInstant(),
                    upvotes = 156,
                    downvotes = 12,
                    views = 892,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onDownvoteClick = {},
                onCopyCodeClick = {},
            )

            // Promo PromoCode
            EnhancedPromoCodeCard(
                promoCode = PromoCode.createPromo(
                    code = "GLOVOFREE",
                    serviceName = "Glovo",
                    description = "Free Glovo Prime for 1 month",
                    category = "Food Delivery",
                    title = "Free Glovo Prime",
                    createdBy = UserId("user3"),
                ).getOrThrow().copy(
                    createdAt = now.minus(3.days).toJavaInstant(),
                    updatedAt = now.minus(2.days).toJavaInstant(),
                    upvotes = 124,
                    downvotes = 9,
                    views = 678,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onDownvoteClick = {},
                onCopyCodeClick = {},
            )
        }
    }
}
