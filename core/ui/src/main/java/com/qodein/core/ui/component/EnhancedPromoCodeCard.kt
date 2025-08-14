package com.qodein.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.PromoCode
import com.qodein.core.model.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Enhanced PromoCodeCard that works with our domain PromoCode sealed class
 */
@Composable
fun EnhancedPromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.FAST),
        label = "card_scale",
    )

    QodeCard(
        modifier = modifier
            .clickable {
                isPressed = true
                onCardClick()
            },
        variant = QodeCardVariant.Elevated,
    ) {
        Column {
            // Header with service info and category
            ServiceHeader(
                serviceName = promoCode.serviceName,
                category = promoCode.category,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Promo code section
            PromoCodeSection(
                code = promoCode.code,
                title = promoCode.title ?: "Discount Available",
                onCopyClick = onCopyCodeClick,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Description
            promoCode.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.sm))
                }
            }

            // Discount info
            DiscountInfo(promoCode = promoCode)

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Badges row
            BadgesRow(promoCode = promoCode)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpacingTokens.sm),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Footer with upvote and stats
            FooterSection(
                upvotes = promoCode.upvotes.toInt(),
                onUpvoteClick = onUpvoteClick,
                isLoggedIn = isLoggedIn,
                createdAt = promoCode.createdAt,
                views = promoCode.views.toInt(),
            )
        }
    }
}

@Composable
private fun ServiceHeader(
    serviceName: String,
    category: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            // Service logo placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(QodeCorners.sm),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = QodeCommerceIcons.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.sm))

            Column {
                Text(
                    text = serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoCodeSection(
    code: String,
    title: String,
    onCopyClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            QodeIconButton(
                onClick = onCopyClick,
                icon = QodeActionIcons.Copy,
                contentDescription = "Copy promo code",
                variant = QodeButtonVariant.Outlined,
                size = QodeButtonSize.Small,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(QodeCorners.sm),
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Text(
                text = code,
                modifier = Modifier.padding(SpacingTokens.sm),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun DiscountInfo(promoCode: PromoCode) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Icon(
            imageVector = QodeCommerceIcons.Discount,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        when (promoCode) {
            is PromoCode.PercentagePromoCode -> {
                Text(
                    text = "${promoCode.discountPercentage.toInt()}% off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (promoCode.maximumDiscount > 0) {
                    Text(
                        text = "• Max ${formatCurrency(promoCode.maximumDiscount, "KZT")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is PromoCode.FixedAmountPromoCode -> {
                Text(
                    text = "${formatCurrency(promoCode.discountAmount, "KZT")} off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is PromoCode.PromoPromoCode -> {
                Text(
                    text = promoCode.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BadgesRow(promoCode: PromoCode) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Community validated badge (based on upvotes)
        if (promoCode.upvotes >= 10) {
            QodeBadge(
                text = "VERIFIED",
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
            )
        }

        // Expiry badge
        promoCode.endDate?.let { expiryDate ->
            val now = Clock.System.now()
            val timeUntilExpiry = expiryDate.toKotlinInstant() - now

            when {
                timeUntilExpiry.isNegative() -> {
                    QodeBadge(
                        text = "EXPIRED",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                timeUntilExpiry < 3.hours -> {
                    QodeBadge(
                        text = "EXPIRES SOON",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        // Popular badge
        if (promoCode.views >= 100) {
            QodeBadge(
                text = "POPULAR",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun FooterSection(
    upvotes: Int,
    onUpvoteClick: () -> Unit,
    isLoggedIn: Boolean,
    createdAt: java.time.Instant,
    views: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Upvote button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (isLoggedIn) {
                    Modifier.clickable { onUpvoteClick() }
                        .clip(RoundedCornerShape(QodeCorners.sm))
                        .background(Color.Transparent)
                        .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs)
                } else {
                    Modifier
                },
            ) {
                Icon(
                    imageVector = QodeActionIcons.Thumbs,
                    contentDescription = "Upvote",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                if (upvotes > 0) {
                    Spacer(modifier = Modifier.width(SpacingTokens.xs))
                    Text(
                        text = upvotes.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Views count
            if (views > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = QodeActionIcons.Preview,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.xs))
                    Text(
                        text = "$views views",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Time ago
        Text(
            text = formatTimeAgo(createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Helper functions
private fun formatCurrency(
    amount: Double,
    currency: String
): String =
    when (currency.uppercase()) {
        "KZT" -> "${amount.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")} ₸"
        "USD" -> "$${amount.toInt()}"
        "EUR" -> "€${amount.toInt()}"
        else -> "${amount.toInt()} $currency"
    }

private fun formatTimeAgo(instant: java.time.Instant): String {
    val now = Clock.System.now()
    val kotlinInstant = instant.toKotlinInstant()
    val duration = now.minus(kotlinInstant)

    return when {
        duration < 1.minutes -> "just now"
        duration < 1.hours -> "${duration.inWholeMinutes}m ago"
        duration < 24.hours -> "${duration.inWholeHours}h ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
        duration.inWholeDays < 30 -> "${duration.inWholeDays / 7}w ago"
        else -> "${duration.inWholeDays / 30}mo ago"
    }
}

// Preview
@Preview(name = "EnhancedPromoCodeCard", showBackground = true)
@Composable
private fun EnhancedPromoCodeCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
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
                    description = "Get 30% discount on your first 3 months of Netflix Premium subscription",
                    createdBy = UserId("user1"),
                ).getOrThrow().copy(
                    createdAt = now.minus(2.hours).toJavaInstant(),
                    endDate = now.plus(30.days).toJavaInstant(),
                    upvotes = 45,
                    downvotes = 3,
                    views = 234,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )

            // Fixed Amount PromoCode
            EnhancedPromoCodeCard(
                promoCode = PromoCode.createFixedAmount(
                    code = "KASPI500",
                    serviceName = "Kaspi",
                    discountAmount = 500.0,
                    category = "Shopping",
                    title = "500 KZT off Kaspi Shopping",
                    description = "Get 500 KZT discount on orders above 5000 KZT",
                    createdBy = UserId("user2"),
                ).getOrThrow().copy(
                    createdAt = now.minus(1.days).toJavaInstant(),
                    endDate = now.plus(14.days).toJavaInstant(),
                    upvotes = 156,
                    downvotes = 12,
                    views = 892,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )

            // Promo PromoCode
            EnhancedPromoCodeCard(
                promoCode = PromoCode.createPromo(
                    code = "GLOVOFREE",
                    serviceName = "Glovo",
                    description = "Get Glovo Prime membership free for 30 days with unlimited free delivery",
                    category = "Food Delivery",
                    title = "Free Glovo Prime for 1 Month",
                    createdBy = UserId("user3"),
                ).getOrThrow().copy(
                    createdAt = now.minus(3.days).toJavaInstant(),
                    endDate = now.plus(10.days).toJavaInstant(),
                    upvotes = 124,
                    downvotes = 9,
                    views = 678,
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )
        }
    }
}
