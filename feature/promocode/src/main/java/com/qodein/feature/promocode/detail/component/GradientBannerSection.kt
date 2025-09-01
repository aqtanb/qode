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
import com.qodein.core.designsystem.component.QodeBannerGradient
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.CategoryIconHelper
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun GradientBannerSection(
    promoCode: PromoCode,
    isCopying: Boolean,
    onCopyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get PromoCode type-specific gradient scheme (purple for fixed amount, orange for percentage)
    val gradientScheme = CategoryIconHelper.getPromoCodeGradient(promoCode)

    // Get category icon from centralized helper
    val categoryIcon = CategoryIconHelper.getCategoryIcon(promoCode.category)

    // Calculate discount display text using proper localization
    val discountDisplay = when (promoCode) {
        is PromoCode.PercentagePromoCode -> "${promoCode.discountPercentage.toInt()}% OFF FROM ${promoCode.minimumOrderAmount.toInt()}₸"
        is PromoCode.FixedAmountPromoCode -> "${promoCode.discountAmount.toInt()}₸ OFF FROM ${promoCode.minimumOrderAmount.toInt()}₸"
    }

    // Banner with proper height and centering like reference
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp), // Much taller like reference
        contentAlignment = Alignment.Center, // Center all content
    ) {
        // Background gradient using centralized gradient system
        QodeBannerGradient(
            colors = gradientScheme,
            height = 280.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = Modifier.padding(SpacingTokens.xl),
        ) {
            // Category pill with proper circular background and spacing
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape, // Circular like reference
                    )
                    .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm), // Proper spacing
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = promoCode.category,
                    tint = Color.White,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium), // Proper size
                )

                Text(
                    text = promoCode.category?.replaceFirstChar { it.uppercase() } ?: "Food",
                    style = MaterialTheme.typography.labelSmall, // Bigger text
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            // PROMO CODE - THE MOST IMPORTANT, BIGGEST TEXT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier.padding(top = SpacingTokens.md),
            ) {
                Text(
                    text = promoCode.code,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )

                Icon(
                    imageVector = QodeActionIcons.Copy,
                    contentDescription = "Copy promo code",
                    tint = Color.White,
                    modifier = Modifier
                        .size(SizeTokens.Icon.sizeLarge)
                        .alpha(if (isCopying) 0.7f else 1f)
                        .clickable { onCopyClicked() },
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
            promoCode.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 4,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Preview
@Composable
private fun GradientBannerSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCode.PercentagePromoCode(
            id = PromoCodeId("SAMPLE_ID"),
            code = "FALL60",
            serviceName = "Food Delivery Pro",
            category = "Food",
            title = "51% Off Food Orders",
            description = "Қазақстандағы ең жақсы бағалар",
            discountPercentage = 51.0,
            minimumOrderAmount = 5000.0,
            startDate = Clock.System.now(),
            endDate = Clock.System.now().plus(7.days),
            upvotes = 331,
            downvotes = 28,
        )

        GradientBannerSection(
            promoCode = samplePromoCode,
            isCopying = false,
            onCopyClicked = {},
        )
    }
}
