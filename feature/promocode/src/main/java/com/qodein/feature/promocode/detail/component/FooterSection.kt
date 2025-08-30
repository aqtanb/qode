package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeStatusIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import java.text.NumberFormat
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun FooterSection(
    views: Int,
    createdAt: Instant,
    modifier: Modifier = Modifier
) {
    val timeAgo = remember(createdAt) {
        formatTimeAgo(createdAt)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = SpacingTokens.lg, start = SpacingTokens.md, end = SpacingTokens.md, bottom = SpacingTokens.md),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Views
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeStatusIcons.Review,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "${formatViewCount(views)} views",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Divider
        Text(
            text = "•",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        // Created time
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = QodeStatusIcons.Limited,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "$timeAgo ago",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatViewCount(count: Int): String =
    when {
        count < 1000 -> NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
        count < 1000000 -> {
            val thousands = count / 1000.0
            String.format(Locale.getDefault(), "%.1fK", thousands).replace(".0K", "K")
        }
        else -> {
            val millions = count / 1000000.0
            String.format(Locale.getDefault(), "%.1fM", millions).replace(".0M", "M")
        }
    }

private fun formatTimeAgo(createdAt: Instant): String {
    val now = Clock.System.now()
    val duration = now - createdAt

    return when {
        duration.inWholeDays > 0 -> "${duration.inWholeDays}d"
        duration.inWholeHours > 0 -> "${duration.inWholeHours}h"
        duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}m"
        else -> "now"
    }
}

@Preview(showBackground = true)
@Composable
private fun FooterPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text(
                "Footer Section Examples",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            FooterSection(
                views = 1250,
                createdAt = Clock.System.now().minus(2.days),
            )

            FooterSection(
                views = 52000,
                createdAt = Clock.System.now().minus(1.days + 3.hours),
            )

            FooterSection(
                views = 2500000,
                createdAt = Clock.System.now().minus(7.days),
            )
        }
    }
}
