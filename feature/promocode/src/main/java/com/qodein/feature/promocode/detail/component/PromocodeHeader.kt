package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.feature.promocode.detail.PromocodeDetailScreen
import com.qodein.feature.promocode.detail.PromocodeDetailUiState
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeInteraction

@Composable
fun PromocodeHeader(
    promocode: Promocode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularImage(
            imageUrl = promocode.authorAvatarUrl,
            fallbackText = promocode.authorUsername ?: "",
            fallbackIcon = QodeNavigationIcons.Profile,
            size = SizeTokens.Avatar.sizeSmall,
            contentDescription = promocode.authorUsername ?: "",
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = promocode.authorUsername ?: "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = if (promocode.voteScore > 0) "+${promocode.voteScore}" else promocode.voteScore.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
            Text(
                text = rememberFormattedRelativeTime(promocode.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PromocodeHeaderPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promoCodeId = samplePromoCode.id,
                promocodeInteraction = PromocodeInteraction(
                    promocode = samplePromoCode,
                    userInteraction = null,
                ),
                isLoading = false,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
