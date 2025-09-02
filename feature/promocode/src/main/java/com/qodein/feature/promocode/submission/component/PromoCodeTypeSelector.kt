package com.qodein.feature.promocode.submission.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType

@Composable
fun PromoCodeTypeSelector(
    selectedType: PromoCodeType?,
    onTypeSelected: (PromoCodeType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        TypeToggleButton(
            type = PromoCodeType.PERCENTAGE,
            title = "Percentage",
            subtitle = "Discount %",
            icon = QodeCommerceIcons.Sale,
            isSelected = selectedType == PromoCodeType.PERCENTAGE,
            onClick = { onTypeSelected(PromoCodeType.PERCENTAGE) },
            modifier = Modifier.weight(1f),
        )

        TypeToggleButton(
            type = PromoCodeType.FIXED_AMOUNT,
            title = "Fixed Amount",
            subtitle = "Exact ₸",
            icon = QodeCommerceIcons.Dollar,
            isSelected = selectedType == PromoCodeType.FIXED_AMOUNT,
            onClick = { onTypeSelected(PromoCodeType.FIXED_AMOUNT) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TypeToggleButton(
    type: PromoCodeType,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = AnimationTokens.Spec.emphasized,
        label = "scale",
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = "backgroundColor",
    )

    Surface(
        modifier = modifier
            .scale(animatedScale)
            .height(SizeTokens.Button.heightLarge + SpacingTokens.md)
            .clip(RoundedCornerShape(ShapeTokens.Corner.full))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        color = animatedBackgroundColor,
        shape = RoundedCornerShape(ShapeTokens.Corner.full),
        tonalElevation = if (isSelected) ElevationTokens.small else ElevationTokens.none,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(SizeTokens.Icon.sizeXLarge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Preview(name = "Promo Type Card - None Selected", showBackground = true)
@Composable
private fun PromoTypeCardNonePreview() {
    QodeTheme {
        PromoCodeTypeSelector(
            selectedType = null,
            onTypeSelected = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Promo Type Card - Percentage Selected", showBackground = true)
@Composable
private fun PromoTypeCardPercentagePreview() {
    QodeTheme {
        PromoCodeTypeSelector(
            selectedType = PromoCodeType.PERCENTAGE,
            onTypeSelected = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Promo Type Card - Fixed Amount Selected", showBackground = true)
@Composable
private fun PromoTypeCardFixedAmountPreview() {
    QodeTheme {
        PromoCodeTypeSelector(
            selectedType = PromoCodeType.FIXED_AMOUNT,
            onTypeSelected = {},
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
