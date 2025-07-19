package com.qodein.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.component.HeroBanner
import com.qodein.core.ui.component.PromoCodeCard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .padding(),
    ) {
        item {
            Spacer(
                modifier = Modifier
                    .height(QodeSpacing.sm),
            )
        }

        item {
            HeroBanner(
                items = getSampleBannerItems(),
                onItemClick = {},
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(QodeSpacing.md),
            )
        }

        val samplePromoCodes = getSamplePromoCodes()

        items(samplePromoCodes, key = { it.id }) { promoCode ->
            PromoCodeCard(
                promoCode = promoCode,
                onCardClick = {},
                onUpvoteClick = {},
                onFollowStoreClick = {},
                onCopyCodeClick = {},
                isLoggedIn = true,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun HomeScreenPreview() {
    QodeTheme {
        HomeScreen()
    }
}
