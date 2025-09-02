package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun HintCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                RoundedCornerShape(ShapeTokens.Corner.medium),
            )
            .border(
                width = ShapeTokens.Border.thin,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(ShapeTokens.Corner.medium),
            )
            .padding(SpacingTokens.md),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Info icon using QodeUIIcons
        Icon(
            imageVector = QodeUIIcons.Info,
            contentDescription = "Hint",
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(name = "Hint Card", showBackground = true)
@Composable
private fun HintCardPreview() {
    QodeTheme {
        HintCard(
            text = "Select the service for your promo code. Can't find it? Type it manually.",
        )
    }
}
