package com.qodein.feature.profile.component

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeinCard
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.feature.profile.R
import com.qodein.feature.profile.SectionTitle
import com.qodein.shared.model.UserStats

@Composable
internal fun StatsSection(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(SpacingTokens.md),
    ) {
        SectionTitle(
            title = stringResource(R.string.profile_stats_title),
            modifier = Modifier.padding(start = SpacingTokens.xs, bottom = SpacingTokens.lg),
        )
        UserStatsRow(userStats = userStats)
    }
}

@Composable
private fun UserStatsRow(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        StatCard(
            value = userStats.submittedPromocodesCount,
            label = stringResource(R.string.profile_promocodes_label),
            icon = PromocodeIcons.Promocode,
            modifier = Modifier.weight(1f),
        )

        StatCard(
            value = userStats.submittedPostsCount,
            label = stringResource(R.string.posts),
            icon = PostIcons.Post,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    QodeinCard(
        modifier = modifier,
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        variant = QodeCardVariant.Filled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(SizeTokens.Icon.sizeXLarge),
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                textAlign = TextAlign.Center,
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StatsSectionPreview() {
    QodeTheme {
        Surface {
            StatsSection(
                userStats = UserPreviewData.powerUser.stats,
            )
        }
    }
}
