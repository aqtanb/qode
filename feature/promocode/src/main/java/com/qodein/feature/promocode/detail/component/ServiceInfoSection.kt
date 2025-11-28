package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinAssistChip
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.shared.model.Promocode
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

// MARK: - Main Component

@Composable
fun ServiceInfoSection(
    promoCode: Promocode,
    isFollowingService: Boolean,
    onServiceClicked: () -> Unit,
    onFollowServiceClicked: () -> Unit,
    modifier: Modifier = Modifier
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
                    fallbackIcon = QodeIcons.Store,
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
            }

            // Status chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val now = Clock.System.now()

                if (promoCode.isFirstUseOnly) {
                    QodeinAssistChip(
                        label = stringResource(R.string.cd_first_user_only),
                        onClick = {},
                        leadingIcon = PromocodeIcons.FirstUseOnly,
                        enabled = false,
                    )
                }

                if (promoCode.isOneTimeUseOnly) {
                    QodeinAssistChip(
                        label = stringResource(R.string.cd_one_time_use),
                        onClick = {},
                        leadingIcon = PromocodeIcons.OneTimeUse,
                        enabled = false,
                    )
                }

                val isExpiringSoon = promoCode.endDate < now.plus(3.days)
                if (isExpiringSoon) {
                    QodeinAssistChip(
                        label = stringResource(R.string.cd_expiring_soon),
                        onClick = {},
                        leadingIcon = UIIcons.Expiring,
                        enabled = false,
                    )
                }

                if (promoCode.isVerified) {
                    QodeinAssistChip(
                        label = stringResource(R.string.cd_verified),
                        onClick = {},
                        leadingIcon = PromocodeIcons.Verified,
                        enabled = false,
                    )
                }
            }
        }
    }
}
