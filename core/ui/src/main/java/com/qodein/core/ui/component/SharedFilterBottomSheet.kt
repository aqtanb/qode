package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeinOutlinedCard
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R

/**
 * Shared base bottom sheet component for all filter types
 * Provides consistent styling and behavior across the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedFilterBottomSheet(
    isVisible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SpacingTokens.lg),
            ) {
                // Main content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = SpacingTokens.lg), // Space for close button
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                ) {
                    // Header
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Content provided by caller
                    content()
                }

                // Close button positioned at bottom - outside scrollable content
                QodeinOutlinedCard(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Text(
                        text = stringResource(R.string.action_close),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
