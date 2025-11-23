package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeOutlinedButton
import com.qodein.core.designsystem.component.QodeinCard
import com.qodein.core.designsystem.component.QodeinFilterChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.error.asUiText
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.service.selection.PopularServices
import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

/**
 * Centralized service selector bottom sheet component.
 * Uses unified state and action pattern for consistent behavior.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceSelectorBottomSheet(
    state: ServiceSelectionUiState,
    sheetState: SheetState,
    onAction: (ServiceSelectionAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
        ) {
            ServiceSelectorContent(
                state = state,
                onAction = onAction,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceSelectorContent(
    state: ServiceSelectionUiState,
    onAction: (ServiceSelectionAction) -> Unit
) {
    val searchQuery = state.domainState.search.query

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        QodeinTextField(
            value = searchQuery,
            onValueChange = { query -> onAction(ServiceSelectionAction.UpdateQuery(query)) },
            placeholder = stringResource(R.string.search_services_placeholder),
            leadingIcon = QodeNavigationIcons.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        if (searchQuery.isEmpty()) {
            PopularServicesSection(
                popularStatus = state.domainState.popular.status,
                popularServices = state.popularServices,
                selectedServices = state.selectedServices,
                onAction = { action -> onAction(action) },
            )
        } else {
            SearchResultsSection(
                searchStatus = state.domainState.search.status,
                displayServices = state.displayServices,
                selectedServices = state.selectedServices,
                onServiceClick = { service -> onAction(ServiceSelectionAction.ToggleService(service)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopularServicesSection(
    popularStatus: PopularStatus,
    popularServices: List<Service>,
    selectedServices: List<Service>,
    onAction: (ServiceSelectionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = stringResource(R.string.popular_services_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (popularStatus) {
            PopularStatus.Loading -> {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    repeat(20) {
                        ShimmerBox(
                            width = 100.dp,
                            height = 32.dp,
                            shape = RoundedCornerShape(16.dp),
                        )
                    }
                }
            }
            is PopularStatus.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.xl),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = QodeUIIcons.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                    )
                    Text(
                        text = popularStatus.error.asUiText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Spacer(modifier = Modifier.size(SpacingTokens.sm))

                    QodeOutlinedButton(
                        onClick = { onAction(ServiceSelectionAction.UpdateQuery("")) },
                        text = stringResource(R.string.action_retry),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            PopularStatus.Success -> {
                if (popularServices.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        popularServices.take(20).forEach { service ->
                            QodeinFilterChip(
                                label = service.name,
                                leadingIcon = {
                                    CircularImage(
                                        imageUrl = service.logoUrl,
                                        fallbackText = service.name,
                                        fallbackIcon = QodeCommerceIcons.Store,
                                        size = SizeTokens.Icon.sizeSmall,
                                        backgroundColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = service.name,
                                    )
                                },
                                selected = selectedServices.any { it.id == service.id },
                                onClick = { onAction(ServiceSelectionAction.ToggleService(service.id)) },
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.lg),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = QodeUIIcons.Empty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                        )
                        Text(
                            text = stringResource(R.string.no_services_found_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchStatus: SearchStatus,
    displayServices: List<Service>,
    selectedServices: List<Service>,
    onServiceClick: (ServiceId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        modifier = modifier,
    ) {
        when (searchStatus) {
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
                if (displayServices.isNotEmpty()) {
                    items(displayServices) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = selectedServices.any { it.id == service.id },
                            onClick = { onServiceClick(service.id) },
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
            modifier = Modifier.padding(SpacingTokens.md),
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

@ThemePreviews
@Composable
private fun ServiceSelectorContentPreview() {
    QodeTheme {
        ServiceSelectorContent(
            state = ServiceSelectionUiState(
                domainState = ServiceSelectionState(
                    popular = PopularServices(
                        status = PopularStatus.Success,
                    ),
                ),
            ),
            onAction = { },
        )
    }
}

@ThemePreviews
@Composable
private fun PopularServicesSectionLoadingPreview() {
    QodeTheme {
        PopularServicesSection(
            popularStatus = PopularStatus.Loading,
            popularServices = emptyList(),
            selectedServices = emptyList(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PopularServicesSectionIdlePreview() {
    QodeTheme {
        PopularServicesSection(
            popularStatus = PopularStatus.Success,
            popularServices = ServicePreviewData.allSamples,
            selectedServices = listOf(ServicePreviewData.yandex),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PopularServicesSectionErrorPreview() {
    QodeTheme {
        PopularServicesSection(
            popularStatus = PopularStatus.Error(SystemError.Unknown),
            popularServices = emptyList(),
            selectedServices = emptyList(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ServiceItemPreview() {
    QodeTheme {
        ServiceItem(
            service = ServicePreviewData.yandex,
            isSelected = true,
            onClick = {},
        )
    }
}
