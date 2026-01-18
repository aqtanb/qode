package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.PromocodeSortBy

/**
 * Sort filter bottom sheet component
 * Allows users to select sort options in a mobile-friendly bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterBottomSheet(
    isVisible: Boolean,
    currentSortBy: PromocodeSortBy,
    onSortBySelected: (PromocodeSortBy) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                SortOption(
                    sortBy = PromocodeSortBy.POPULARITY,
                    text = stringResource(R.string.sort_popularity),
                    isSelected = currentSortBy == PromocodeSortBy.POPULARITY,
                    onClick = {
                        onSortBySelected(PromocodeSortBy.POPULARITY)
                        onDismiss()
                    },
                )

                // Newest sort option
                SortOption(
                    sortBy = PromocodeSortBy.NEWEST,
                    text = stringResource(R.string.sort_newest),
                    isSelected = currentSortBy == PromocodeSortBy.NEWEST,
                    onClick = {
                        onSortBySelected(PromocodeSortBy.NEWEST)
                        onDismiss()
                    },
                )

                // Expiring soon sort option
                SortOption(
                    sortBy = PromocodeSortBy.EXPIRING_SOON,
                    text = stringResource(R.string.sort_expiring_soon),
                    isSelected = currentSortBy == PromocodeSortBy.EXPIRING_SOON,
                    onClick = {
                        onSortBySelected(PromocodeSortBy.EXPIRING_SOON)
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun SortOption(
    sortBy: PromocodeSortBy,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            RadioButton(
                selected = isSelected,
                onClick = null, // Click is handled by the TextButton
            )
        }
    }
}
