package com.qodein.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.theme.QodeSpacing

@Composable
fun PromoCarousel(
    promos: List<String>,
    modifier: Modifier = Modifier
) {
    // val promoItems = viewModel.promoPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = modifier.padding(horizontal = QodeSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
    ) {
        promos.forEach { promo ->
            QodeCard(
                modifier = Modifier.fillMaxWidth(),
                variant = QodeCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(QodeSpacing.md)) {
                    Text(
                        text = "Promo Code: $promo",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use this code to save 20% on your next purchase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
