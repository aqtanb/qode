package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.CategoryIconHelper
import com.qodein.core.ui.preview.PromoCodePreviewData
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode

@Composable
fun GradientBannerSection(
    promoCode: PromoCode,
    isCopying: Boolean,
    onCopyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get category icon from centralized helper
    val categoryIcon = CategoryIconHelper.getCategoryIcon(promoCode.category)

    // Calculate discount display text using proper localization
    val discountDisplay = when (promoCode.discount) {
        is Discount.Percentage -> "${promoCode.discount.value.toInt()}% OFF FROM ${promoCode.minimumOrderAmount.toInt()}₸"
        is Discount.FixedAmount -> "${promoCode.discount.value.toInt()}₸ OFF FROM ${promoCode.minimumOrderAmount.toInt()}₸"
    }

    // Banner with solid background color
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            modifier = Modifier.padding(SpacingTokens.xl),
        ) {
            // Category pill with proper circular background and spacing
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape,
                    )
                    .padding(SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm), // Proper spacing
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = promoCode.category,
                    tint = Color.White,
                    modifier = Modifier.size(SizeTokens.Icon.sizeSmall), // Proper size
                )

                Text(
                    text = promoCode.category?.replaceFirstChar { it.uppercase() } ?: "Food",
                    style = MaterialTheme.typography.labelSmall, // Bigger text
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier.padding(top = SpacingTokens.sm),
            ) {
                Text(
                    text = promoCode.code,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(
                        enabled = !isCopying,
                        onClick = onCopyClicked,
                    ),

                )
            }

            // Discount text - smaller, secondary
            Text(
                text = discountDisplay,
                style = MaterialTheme.typography.titleMedium, // Smaller than promo code
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = SpacingTokens.md),
            )

            // Description - clean and readable
            // TODO: Make description extensible
            promoCode.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun GradientBannerSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCodePreviewData.percentagePromoCode

        GradientBannerSection(
            promoCode = samplePromoCode,
            isCopying = false,
            onCopyClicked = {},
        )
    }
}
