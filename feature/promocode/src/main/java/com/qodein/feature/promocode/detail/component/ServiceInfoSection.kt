package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeLocationIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Composable
fun ServiceInfoSection(
    promoCode: PromoCode,
    isFollowingService: Boolean,
    isFollowingCategory: Boolean,
    onServiceClicked: () -> Unit,
    onFollowServiceClicked: () -> Unit,
    onFollowCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left side: Service name and category with follow icons
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            modifier = Modifier.weight(1f),
        ) {
            // Service name with follow icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Text(
                    text = promoCode.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                Surface(
                    color = if (isFollowingService) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50), // Circular
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onFollowServiceClicked() },
                ) {
                    Icon(
                        imageVector = if (isFollowingService) QodeActionIcons.Unfollow else QodeActionIcons.Follow,
                        contentDescription = if (isFollowingService) "Following" else "Follow",
                        modifier = Modifier
                            .size(12.dp)
                            .padding(4.dp),
                        tint = if (isFollowingService) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Category with follow icon (if exists)
            promoCode.category?.let { category ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Surface(
                        color = if (isFollowingCategory) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50), // Circular
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onFollowCategoryClicked() },
                    ) {
                        Icon(
                            imageVector = if (isFollowingCategory) QodeActionIcons.Unfollow else QodeActionIcons.Follow,
                            contentDescription = if (isFollowingCategory) "Following" else "Follow",
                            modifier = Modifier
                                .size(10.dp)
                                .padding(4.dp),
                            tint = if (isFollowingCategory) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }

        // Right side: Country flags and badges
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            // Target countries row
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                promoCode.targetCountries.take(2).forEach { countryCode ->
                    CircularImage(
                        imageUrl = "https://flagcdn.com/w80/${countryCode.lowercase()}.png",
                        fallbackText = countryCode,
                        fallbackIcon = QodeLocationIcons.Territory,
                        size = 28.dp,
                        contentDescription = "Flag of $countryCode",
                    )
                }

                if (promoCode.targetCountries.size > 2) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50), // Fully circular
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = QodeNavigationIcons.More,
                            contentDescription = "More countries",
                            modifier = Modifier
                                .size(14.dp)
                                .padding(2.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Status chip - shows promo code status
                val now = Clock.System.now()
                val statusInfo = getPromoCodeStatus(promoCode, now)

                Surface(
                    color = statusInfo.backgroundColor,
                    shape = RoundedCornerShape(50), // Fully circular pill
                    modifier = Modifier,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = statusInfo.text,
                            tint = statusInfo.contentColor,
                            modifier = Modifier.size(12.dp),
                        )

                        Text(
                            text = statusInfo.text,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = statusInfo.contentColor,
                        )
                    }
                }

                // Verified badge - green circular success card (only if verified)
                if (promoCode.isVerified) {
                    Surface(
                        color = MaterialTheme.extendedColorScheme.successContainer,
                        shape = RoundedCornerShape(50), // Fully circular pill
                        modifier = Modifier,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            Icon(
                                imageVector = QodeActionIcons.Check,
                                contentDescription = "Verified",
                                tint = MaterialTheme.extendedColorScheme.onSuccessContainer,
                                modifier = Modifier.size(12.dp),
                            )

                            Text(
                                text = "Verified",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.extendedColorScheme.onSuccessContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ServiceInfoSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCode.PercentagePromoCode(
            id = PromoCodeId("SAMPLE_ID"),
            code = "FALL60",
            serviceName = "Food Delivery Pro Service",
            category = "Food",
            title = "51% Off Food Orders",
            discountPercentage = 51.0,
            minimumOrderAmount = 5000.0,
            startDate = Clock.System.now(),
            endDate = Clock.System.now().plus(7.days),
            targetCountries = listOf("KZ", "US"),
            isVerified = true,
        )

        Surface {
            ServiceInfoSection(
                promoCode = samplePromoCode,
                isFollowingService = false,
                isFollowingCategory = false,
                onServiceClicked = {},
                onFollowServiceClicked = {},
                onFollowCategoryClicked = {},
            )
        }
    }
}

private data class StatusInfo(val text: String, val icon: ImageVector, val backgroundColor: Color, val contentColor: Color)

@Composable
private fun getPromoCodeStatus(
    promoCode: PromoCode,
    now: Instant
): StatusInfo {
    val threeDaysFromNow = now.plus(3.days)

    return when {
        now < promoCode.startDate -> StatusInfo(
            text = "Not Active",
            icon = QodeNavigationIcons.Calendar,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        now > promoCode.endDate -> StatusInfo(
            text = "Expired",
            icon = QodeActionIcons.Block,
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        promoCode.endDate < threeDaysFromNow -> StatusInfo(
            text = "Expiring Soon",
            icon = QodeNavigationIcons.Warning,
            backgroundColor = Color(0xFFFF8A00).copy(alpha = 0.1f), // Orange warning
            contentColor = Color(0xFFFF8A00),
        )

        else -> StatusInfo(
            text = "Active",
            icon = QodeActionIcons.Check,
            backgroundColor = MaterialTheme.extendedColorScheme.successContainer,
            contentColor = MaterialTheme.extendedColorScheme.onSuccessContainer,
        )
    }
}
