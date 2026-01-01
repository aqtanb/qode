package com.qodein.feature.service.selection

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeOutlinedButton
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.QodeinFilterChip
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.error.asUiText
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServiceSelectionBottomSheet(
    viewModel: ServiceSelectionViewModel,
    onDismiss: (Set<ServiceId>) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ServiceSelectionEvent.ServiceSelected -> {
                    onDismiss(event.selectedServiceIds)
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            Logger.d("ServiceSelectionBottomSheet") { "onDismissRequest called with ${uiState.selectedServiceIds.size} services" }
            onDismiss(uiState.selectedServiceIds)
        },
        sheetState = sheetState,
        modifier = modifier,
    ) {
        ServiceSelectionContent(
            uiState = uiState,
            onAction = viewModel::onAction,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceSelectionContent(
    uiState: ServiceSelectionUiState,
    onAction: (ServiceSelectionAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        QodeinTextField(
            value = uiState.searchText,
            onValueChange = { query -> onAction(ServiceSelectionAction.UpdateQuery(query)) },
            placeholder = stringResource(R.string.search_services_placeholder),
            leadingIcon = QodeNavigationIcons.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.searchText.isEmpty()) {
            PopularServicesSection(
                popularStatus = uiState.popularStatus,
                selectedServiceIds = uiState.selectedServiceIds,
                onAction = onAction,
            )
        } else {
            SearchResultsSection(
                searchStatus = uiState.searchStatus,
                selectedServiceIds = uiState.selectedServiceIds,
                onServiceClick = { serviceId -> onAction(ServiceSelectionAction.ToggleService(serviceId)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopularServicesSection(
    popularStatus: PopularStatus,
    selectedServiceIds: Set<ServiceId>,
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
                    repeat(GetPopularServicesUseCase.DEFAULT_LIMIT.toInt()) {
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
                        imageVector = UIIcons.Error,
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
                        onClick = { onAction(ServiceSelectionAction.RetryLoadServices) },
                        text = stringResource(R.string.action_retry),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            is PopularStatus.Success -> {
                val popularServices = popularStatus.services
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
                                        fallbackIcon = QodeIcons.Store,
                                        size = SizeTokens.Icon.sizeSmall,
                                        backgroundColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = service.name,
                                    )
                                },
                                selected = service.id in selectedServiceIds,
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
                            imageVector = UIIcons.Empty,
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
    searchStatus: SearchUiState,
    selectedServiceIds: Set<ServiceId>,
    onServiceClick: (ServiceId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        modifier = modifier,
    ) {
        when (searchStatus) {
            SearchUiState.Loading -> {
                items(1) {
                    ServiceItemPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            is SearchUiState.Success -> {
                val searchResults = searchStatus.services
                if (searchResults.isNotEmpty()) {
                    items(searchResults) { service ->
                        ServiceItem(
                            service = service,
                            isSelected = service.id in selectedServiceIds,
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
            is SearchUiState.Error -> {
                item {
                    Text(
                        text = stringResource(R.string.search_error_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(SpacingTokens.xl),
                    )
                }
            }

            SearchUiState.Idle -> {
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
    QodeinElevatedCard(
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
                fallbackIcon = QodeIcons.Store,
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
                text = "${service.promocodeCount}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ServiceItemPlaceholder(modifier: Modifier = Modifier) {
    QodeinElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            ShimmerBox(
                width = SizeTokens.Icon.sizeMedium,
                height = SizeTokens.Icon.sizeMedium,
                shape = RoundedCornerShape(ShapeTokens.Corner.full),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                modifier = Modifier.weight(3f),
            ) {
                ShimmerBox(
                    width = 160.dp,
                    height = 16.dp,
                    shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                )
            }
            ShimmerBox(
                width = 50.dp,
                height = 14.dp,
                shape = RoundedCornerShape(ShapeTokens.Corner.small),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ServiceSelectionContentPreview() {
    QodeTheme {
        ServiceSelectionContent(
            uiState = ServiceSelectionUiState(
                popularStatus = PopularStatus.Success(ServicePreviewData.allSamples),
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
            selectedServiceIds = emptySet(),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun PopularServicesSectionIdlePreview() {
    QodeTheme {
        PopularServicesSection(
            popularStatus = PopularStatus.Success(ServicePreviewData.allSamples),
            selectedServiceIds = setOf(ServicePreviewData.yandex.id),
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
            selectedServiceIds = emptySet(),
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

@ThemePreviews
@Composable
private fun SearchResultsSectionLoadingPreview() {
    QodeTheme {
        SearchResultsSection(
            searchStatus = SearchUiState.Loading,
            selectedServiceIds = emptySet(),
            onServiceClick = {},
        )
    }
}
