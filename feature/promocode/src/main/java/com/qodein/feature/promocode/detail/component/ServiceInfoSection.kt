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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeLocationIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeSecurityIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.core.ui.component.getPromoCodeStatus
import com.qodein.core.ui.preview.PromoCodePreviewData
import com.qodein.shared.model.PromoCode
import kotlin.time.Clock

// MARK: - Main Component

@Composable
fun ServiceInfoSection(
    promoCode: PromoCode,
    isFollowingService: Boolean,
    onServiceClicked: () -> Unit,
    onFollowServiceClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    // Follow service action - auth checking handled at parent level
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Top row: Service name + countries
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Service name with follow button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Text(
                    text = promoCode.serviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Surface(
                    color = if (isFollowingService) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .size(SizeTokens.Icon.sizeLarge)
                        .clickable { onFollowServiceClicked() }, // Auth checking handled at parent level
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

            // Right: Target countries
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
                        shape = RoundedCornerShape(50),
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
        }

        // Status chips row - full width, centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // First User Only chip
            Surface(
                color = if (promoCode.isFirstUserOnly) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Icon(
                        imageVector = if (promoCode.isFirstUserOnly) QodeNavigationIcons.Popular else QodeNavigationIcons.Team,
                        contentDescription = if (promoCode.isFirstUserOnly) "First user only" else "All users",
                        tint = if (promoCode.isFirstUserOnly) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(12.dp),
                    )

                    Text(
                        text = if (promoCode.isFirstUserOnly) "First User Only" else "All Users",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (promoCode.isFirstUserOnly) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            // Status chip
            val now = Clock.System.now()
            val statusInfo = getPromoCodeStatus(promoCode, now)
            val statusIcon = when {
                statusInfo.text == "Active" -> QodeActionIcons.Play
                statusInfo.text == "Expiring Soon" -> QodeNavigationIcons.Warning
                statusInfo.text == "Expired" -> QodeActionIcons.Block
                statusInfo.text == "Not Active" -> QodeNavigationIcons.Calendar
                else -> QodeNavigationIcons.Help
            }

            Surface(
                color = statusInfo.backgroundColor,
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Icon(
                        imageVector = statusIcon,
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

            // Verified badge
            Surface(
                color = if (promoCode.isVerified) {
                    MaterialTheme.extendedColorScheme.successContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Icon(
                        imageVector = if (promoCode.isVerified) QodeActionIcons.Check else QodeSecurityIcons.Secure,
                        contentDescription = if (promoCode.isVerified) "Verified" else "Unverified",
                        tint = if (promoCode.isVerified) {
                            MaterialTheme.extendedColorScheme.onSuccessContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(12.dp),
                    )

                    Text(
                        text = if (promoCode.isVerified) "Verified" else "Unverified",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (promoCode.isVerified) {
                            MaterialTheme.extendedColorScheme.onSuccessContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ServiceInfoSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCodePreviewData.percentagePromoCode

        Surface {
            ServiceInfoSection(
                promoCode = samplePromoCode,
                isFollowingService = false,
                onServiceClicked = {},
                onFollowServiceClicked = {},
                isDarkTheme = false,
            )
        }
    }
}
