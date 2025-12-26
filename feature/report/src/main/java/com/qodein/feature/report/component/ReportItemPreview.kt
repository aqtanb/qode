package com.qodein.feature.report.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.report.R
import com.qodein.shared.model.ContentType

@Composable
internal fun ReportItemPreview(
    itemType: ContentType,
    itemTitle: String,
    itemAuthor: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                Text(
                    text = when (itemType) {
                        ContentType.POST -> stringResource(R.string.content_type_post)
                        ContentType.PROMO_CODE -> stringResource(R.string.content_type_promocode)
                        else -> itemType.name
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                itemAuthor?.let { author ->
                    Text(
                        text = stringResource(R.string.by_author, author),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
