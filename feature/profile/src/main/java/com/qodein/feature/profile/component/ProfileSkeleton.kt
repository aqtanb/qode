package com.qodein.feature.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qodein.core.designsystem.component.ShimmerCircle
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.PromocodeCardSkeleton

@Composable
internal fun ProfileSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        contentPadding = PaddingValues(vertical = SpacingTokens.lg),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                ShimmerCircle(size = SizeTokens.Avatar.sizeXLarge)
                ShimmerLine(width = SizeTokens.Avatar.sizeXLarge, height = SpacingTokens.lg)
            }
        }

        item {
            ShimmerLine(
                width = SizeTokens.Avatar.sizeXLarge,
                height = SpacingTokens.xxl,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        items(5) {
            PromocodeCardSkeleton()
        }
    }
}
