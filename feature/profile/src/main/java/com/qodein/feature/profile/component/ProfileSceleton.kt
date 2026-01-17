package com.qodein.feature.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.ShimmerCircle
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
internal fun ProfileSceleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.huge))

        ShimmerCircle(size = SizeTokens.Avatar.sizeXLarge)

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        ShimmerLine(width = 180.dp, height = 32.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                ShimmerLine(width = 40.dp, height = 28.dp)
                ShimmerLine(width = 60.dp, height = 14.dp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                ShimmerLine(width = 40.dp, height = 28.dp)
                ShimmerLine(width = 60.dp, height = 14.dp)
            }
        }
        ShimmerLine(width = 180.dp, height = 32.dp)
    }
}
