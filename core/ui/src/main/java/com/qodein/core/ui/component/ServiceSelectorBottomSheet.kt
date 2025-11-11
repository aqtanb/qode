package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinCard
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.state.ServiceSelectionUiAction
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.model.Service
import kotlinx.coroutines.delay

/**
 * Centralized service selector bottom sheet component.
 * Uses unified state and action pattern for consistent behavior.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceSelectorBottomSheet(
    state: ServiceSelectionUiState,
    sheetState: SheetState,
    onAction: (ServiceSelectionUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isVisible) {
        SharedFilterBottomSheet(
            isVisible = state.isVisible,
            title = stringResource(R.string.select_service_title),
            onDismiss = { onAction(ServiceSelectionUiAction.Dismiss) },
            sheetState = sheetState,
            modifier = modifier,
        ) {
            ServiceSelectorContent(
                state = state,
                onAction = onAction,
                isSearchMode = state.shouldAutoExpand,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceSelectorContent(
    state: ServiceSelectionUiState,
    onAction: (ServiceSelectionUiAction) -> Unit,
    isSearchMode: Boolean
) {
    val searchQuery = state.domainState.search.query
    val isSearching = state.domainState.search.isSearching

    // Debounced search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(300)
            // Search is handled by the domain manager automatically
        }
    }

    Column(
        modifier = if (isSearchMode) Modifier.fillMaxWidth().padding(SpacingTokens.lg) else Modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // No extra header in search mode - keep it clean

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query -> onAction(ServiceSelectionUiAction.UpdateQuery(query)) },
            label = { Text(stringResource(R.string.search_services_label)) },
            placeholder = { Text(stringResource(R.string.search_services_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = QodeNavigationIcons.Search,
                    contentDescription = stringResource(R.string.search_icon_description),
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onAction(ServiceSelectionUiAction.ClearQuery) }) {
                        Icon(
                            imageVector = QodeActionIcons.Close,
                            contentDescription = stringResource(R.string.action_clear),
                        )
                    }
                }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    onAction(ServiceSelectionUiAction.SetSearchFocus(focusState.isFocused))
                },
            shape = RoundedCornerShape(28.dp),
        )

        // Popular services chips (when search query is empty)
        if (searchQuery.isEmpty()) {
            Text(
                text = stringResource(R.string.popular_services_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when (state.domainState.popular.status) {
                PopularStatus.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.lg),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(SpacingTokens.sm))
                        Text(
                            text = stringResource(R.string.loading_popular_services),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is PopularStatus.Error -> {
                    // Could show error state here
                }
                PopularStatus.Idle -> {
                    if (state.popularServices.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        ) {
                            state.popularServices.take(20).forEach { service ->
                                ServiceChip(
                                    service = service,
                                    isSelected = state.selectedServices.any { it.id == service.id },
                                    onClick = { onAction(ServiceSelectionUiAction.SelectService(service)) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search results (only when searching)
        if (isSearching) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.height(if (isSearchMode) 600.dp else 400.dp),
            ) {
                when (val searchStatus = state.domainState.search.status) {
                    SearchStatus.Loading -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(SpacingTokens.xl),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.size(SpacingTokens.md))
                                Text(
                                    text = stringResource(R.string.searching_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    is SearchStatus.Success -> {
                        if (state.displayServices.isNotEmpty()) {
                            items(state.displayServices) { service ->
                                ServiceItem(
                                    service = service,
                                    isSelected = state.selectedServices.any { it.id == service.id },
                                    onClick = { onAction(ServiceSelectionUiAction.SelectService(service)) },
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = stringResource(R.string.no_services_found_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(SpacingTokens.xl),
                                )
                            }
                        }
                    }
                    is SearchStatus.Error -> {
                        item {
                            Text(
                                text = stringResource(R.string.search_error_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(SpacingTokens.xl),
                            )
                        }
                    }
                    SearchStatus.Idle -> {
                        // No search results to show
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceItem(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = modifier.fillMaxWidth(),
        ) {
            CircularImage(
                imageUrl = service.logoUrl,
                fallbackText = service.name,
                fallbackIcon = QodeCommerceIcons.Store,
                size = SizeTokens.Icon.sizeMedium,
                backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                contentDescription = service.name,
            )

            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(3f),
            )

            Text(
                text = "${service.promoCodeCount} codes",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ServiceChip(
    service: Service,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = service.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
        },
        leadingIcon = if (service.logoUrl != null) {
            {
                CircularImage(
                    imageUrl = service.logoUrl,
                    fallbackText = service.name,
                    fallbackIcon = QodeCommerceIcons.Store,
                    size = SizeTokens.Icon.sizeSmall,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    contentDescription = service.name,
                )
            }
        } else {
            null
        },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
    )
}
