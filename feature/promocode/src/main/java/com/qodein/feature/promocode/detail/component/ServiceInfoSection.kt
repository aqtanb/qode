package com.qodein.feature.promocode.detail.component

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinAssistChip
import com.qodein.core.designsystem.icon.PromocodeStatusIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeLocationIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.feature.promocode.R
import com.qodein.shared.model.PromoCode
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

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
                CircularImage(
                    imageUrl = promoCode.serviceLogoUrl,
                    fallbackText = promoCode.serviceName,
                    fallbackIcon = QodeCommerceIcons.Store,
                    size = SizeTokens.Icon.sizeLarge,
                    contentDescription = "Logo of ${promoCode.serviceName}",
                )

                Text(
                    text = promoCode.serviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                /*Surface(
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
                }*/
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

        // Status chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val now = Clock.System.now()

            if (promoCode.isFirstUserOnly) {
                QodeinAssistChip(
                    label = stringResource(R.string.cd_first_user_only),
                    onClick = {},
                    leadingIcon = PromocodeStatusIcons.FirstTimeUsers,
                    enabled = false,
                )
            }

            if (promoCode.isOneTimeUseOnly) {
                QodeinAssistChip(
                    label = stringResource(R.string.cd_one_time_use),
                    onClick = {},
                    leadingIcon = PromocodeStatusIcons.OneTimeUse,
                    enabled = false,
                )
            }

            val isExpiringSoon = promoCode.endDate < now.plus(3.days)
            if (isExpiringSoon) {
                QodeinAssistChip(
                    label = stringResource(R.string.cd_expiring_soon),
                    onClick = {},
                    leadingIcon = PromocodeStatusIcons.ExpiringSoon,
                    enabled = false,
                )
            }

            if (promoCode.isVerified) {
                QodeinAssistChip(
                    label = stringResource(R.string.cd_verified),
                    onClick = {},
                    leadingIcon = PromocodeStatusIcons.Verified,
                    enabled = false,
                )
            }
        }
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ServiceInfoSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromoCode

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
