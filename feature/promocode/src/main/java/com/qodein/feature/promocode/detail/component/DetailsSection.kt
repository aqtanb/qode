package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromoCodePreviewData
import com.qodein.shared.model.PromoCode
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
fun DetailsSection(
    promoCode: PromoCode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        // Section Title - smaller, cleaner
        Text(
            text = "Promocode Details",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        // Details without card - flat design like reference
        Column(
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Minimum Order
            when (promoCode) {
                is Discount.Percentage -> {
                    DetailRow(
                        icon = QodeCommerceIcons.Cost,
                        label = "Minimum Order",
                        value = "₸${formatAmount(promoCode.minimumOrderAmount)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                    )
                }
                is Discount.FixedAmount -> {
                    DetailRow(
                        icon = QodeCommerceIcons.Cost,
                        label = "Minimum Order",
                        value = "₸${formatAmount(promoCode.minimumOrderAmount)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp,
            )

            // Valid From
            DetailRow(
                icon = QodeActionIcons.Play,
                label = "Valid From",
                value = formatDate(promoCode.startDate),
                valueColor = if (promoCode.isNotStarted) {
                    MaterialTheme.colorScheme.tertiary // Orange for not yet started
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp,
            )

            // Valid Until
            DetailRow(
                icon = QodeActionIcons.Stop,
                label = "Valid Until",
                value = formatDate(promoCode.endDate),
                valueColor = if (promoCode.isExpired) {
                    MaterialTheme.colorScheme.error // Red for expired
                } else if (isExpiringWithinWeek(promoCode.endDate)) {
                    MaterialTheme.colorScheme.tertiary // Orange for expiring soon
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            // Created date
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

// Helper functions
private fun formatAmount(amount: Double): String =
    when {
        amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
        amount >= 1_000 -> "${(amount / 1_000).toInt()}K"
        else -> amount.toInt().toString()
    }

private fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val date = instant.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return formatter.format(date)
}

private fun formatDateWithTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")
    val dateTime = instant.toJavaInstant().atZone(ZoneId.systemDefault())
    return formatter.format(dateTime)
}

private fun isExpiringWithinWeek(endDate: Instant): Boolean {
    val now = Clock.System.now()
    val oneWeekFromNow = now.plus(7.days)
    return endDate > now && endDate <= oneWeekFromNow
}

@Preview
@Composable
private fun DetailsSectionPreview() {
    QodeTheme {
        Surface {
            DetailsSection(promoCode = PromoCodePreviewData.percentagePromoCode)
        }
    }
}
