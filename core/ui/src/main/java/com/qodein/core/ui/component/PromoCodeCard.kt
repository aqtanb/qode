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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeButton
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
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * PromoCodeCard component that displays a promo code with all its details
 *
 * @param promoCode The promo code data to display
 * @param onCardClick Called when the card is clicked
 * @param onUpvoteClick Called when upvote button is clicked
 * @param onFollowStoreClick Called when follow store button is clicked
 * @param onCopyCodeClick Called when copy code button is clicked
 * @param modifier Modifier to be applied to the card
 * @param isLoggedIn Whether the user is logged in (affects interaction availability)
 */
@Composable
fun PromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onUpvoteClick: () -> Unit,
    onFollowStoreClick: () -> Unit,
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

    val cardAlpha by animateFloatAsState(
        targetValue = if (promoCode.isUsed && promoCode.isSingleUse) 0.6f else 1f,
        animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
        label = "card_alpha",
    )

    QodeCard(
        modifier = modifier
            .alpha(cardAlpha)
            .clickable {
                isPressed = true
                onCardClick()
            },
        variant = QodeCardVariant.Elevated,
    ) {
        Column {
            // Header with store info and follow button
            StoreHeader(
                store = promoCode.store,
                onFollowClick = onFollowStoreClick,
                isLoggedIn = isLoggedIn,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Promo code section
            PromoCodeSection(
                code = promoCode.code,
                title = promoCode.title,
                onCopyClick = onCopyCodeClick,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Description
            if (promoCode.description.isNotBlank()) {
                Text(
                    text = promoCode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(SpacingTokens.sm))
            }

            // Discount info
            DiscountInfo(
                discountAmount = promoCode.discountAmount,
                discountPercentage = promoCode.discountPercentage,
                minimumOrderAmount = promoCode.minimumOrderAmount,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Badges row
            BadgesRow(
                isVerified = promoCode.isVerified,
                isFirstOrderOnly = promoCode.isFirstOrderOnly,
                isSingleUse = promoCode.isSingleUse,
                isUsed = promoCode.isUsed,
                expiryDate = promoCode.expiryDate,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpacingTokens.sm),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Footer with upvote and stats
            FooterSection(
                upvotes = promoCode.upvotes,
                isUpvoted = promoCode.isUpvoted,
                onUpvoteClick = onUpvoteClick,
                isLoggedIn = isLoggedIn,
                createdAt = promoCode.createdAt,
            )
        }
    }
}

@Composable
private fun StoreHeader(
    store: Store,
    onFollowClick: () -> Unit,
    isLoggedIn: Boolean
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
            // Store logo placeholder
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
                    text = store.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (store.followersCount > 0) {
                    Text(
                        text = "${store.followersCount} followers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (isLoggedIn) {
            QodeButton(
                onClick = onFollowClick,
                text = if (store.isFollowed) "Following" else "Follow",
                variant = if (store.isFollowed) QodeButtonVariant.Outlined else QodeButtonVariant.Primary,
                size = QodeButtonSize.Small,
            )
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
private fun DiscountInfo(
    discountAmount: Int?,
    discountPercentage: Int?,
    minimumOrderAmount: Int?
) {
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

        when {
            discountPercentage != null -> {
                Text(
                    text = "$discountPercentage% off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            discountAmount != null -> {
                Text(
                    text = "${formatCurrency(discountAmount)} off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            else -> {
                Text(
                    text = "Discount available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        minimumOrderAmount?.let { amount ->
            Text(
                text = "• Min ${formatCurrency(amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BadgesRow(
    isVerified: Boolean,
    isFirstOrderOnly: Boolean,
    isSingleUse: Boolean,
    isUsed: Boolean,
    expiryDate: LocalDate?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isVerified) {
            QodeBadge(
                text = "VERIFIED",
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
            )
        }

        if (isFirstOrderOnly) {
            QodeBadge(
                text = "FIRST ORDER",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        if (isSingleUse) {
            QodeBadge(
                text = if (isUsed) "USED" else "SINGLE USE",
                containerColor = if (isUsed) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                contentColor = if (isUsed) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
            )
        }

        expiryDate?.let { date ->
            val daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), date)
            when {
                daysUntilExpiry < 0 -> {
                    QodeBadge(
                        text = "EXPIRED",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                daysUntilExpiry <= 3 -> {
                    QodeBadge(
                        text = "EXPIRES SOON",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                daysUntilExpiry <= 30 -> {
                    QodeBadge(
                        text = "Expires ${date.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterSection(
    upvotes: Int,
    isUpvoted: Boolean,
    onUpvoteClick: () -> Unit,
    isLoggedIn: Boolean,
    createdAt: LocalDateTime
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
                        .background(
                            if (isUpvoted) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                        )
                        .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs)
                } else {
                    Modifier
                },
            ) {
                Icon(
                    imageVector = if (isUpvoted) QodeActionIcons.Thumbs else QodeActionIcons.Thumbs,
                    contentDescription = "Upvote",
                    tint = if (isUpvoted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp),
                )

                if (upvotes > 0) {
                    Spacer(modifier = Modifier.width(SpacingTokens.xs))
                    Text(
                        text = upvotes.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUpvoted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            // Time ago
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(SpacingTokens.xs))
                Text(
                    text = formatTimeAgo(createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Int): String = "${amount.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")} ₸"

private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        hours < 1 -> "just now"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> "${days / 30}mo ago"
    }
}

// Preview
@Preview(name = "PromoCodeCard", showBackground = true)
@Composable
private fun PromoCodeCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            val sampleStore = Store(
                id = "kaspi",
                name = "Kaspi Bank",
                category = StoreCategory.Electronics,
                followersCount = 1250,
                isFollowed = false,
            )

            val sampleCategory = Category(
                id = "electronics",
                name = "Electronics",
            )

            // Regular promo code
            PromoCodeCard(
                promoCode = PromoCode(
                    id = "1",
                    code = "SAVE20KZT",
                    title = "20% off all electronics",
                    description = "Get amazing discounts on laptops, phones, and more. Limited time offer!",
                    store = sampleStore,
                    category = sampleCategory,
                    discountPercentage = 20,
                    minimumOrderAmount = 50000,
                    upvotes = 15,
                    isVerified = true,
                    createdAt = LocalDateTime.now().minusHours(2),
                    expiryDate = LocalDate.now().plusDays(5),
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onFollowStoreClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )

            // Single-use promo code
            PromoCodeCard(
                promoCode = PromoCode(
                    id = "2",
                    code = "FIRST50",
                    title = "50% off first order",
                    description = "Special discount for new customers",
                    store = sampleStore.copy(isFollowed = true),
                    category = sampleCategory,
                    discountPercentage = 50,
                    isFirstOrderOnly = true,
                    isSingleUse = true,
                    upvotes = 8,
                    isUpvoted = true,
                    createdAt = LocalDateTime.now().minusDays(1),
                ),
                onCardClick = {},
                onUpvoteClick = {},
                onFollowStoreClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )
        }
    }
}
