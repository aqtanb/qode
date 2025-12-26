package com.qodein.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.ReportReason
import com.qodein.shared.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Clock

class ReportViewModel(
    private val reportedItemId: String,
    private val reportedItemType: ContentType,
    private val reportRepository: ReportRepository,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Input())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated

    companion object {
        private const val TAG = "ReportViewModel"
        private const val MAX_ADDITIONAL_DETAILS_LENGTH = 500
    }

    init {
        observeAuthState()
    }

    fun onAction(action: ReportAction) {
        when (action) {
            is ReportAction.SelectReason -> selectReason(action.reason)
            is ReportAction.UpdateAdditionalDetails -> updateAdditionalDetails(action.text)
            ReportAction.Submit -> submitReport()
            ReportAction.NavigateBack -> {}
            ReportAction.DismissError -> dismissError()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                currentAuthState = authState
            }
        }
    }

    private fun selectReason(reason: ReportReason) {
        _uiState.update { state ->
            if (state is ReportUiState.Input) {
                state.copy(
                    selectedReason = reason,
                    validationErrorResId = null,
                )
            } else {
                state
            }
        }
    }

    private fun updateAdditionalDetails(text: String) {
        _uiState.update { state ->
            if (state is ReportUiState.Input) {
                val trimmedText = text.take(MAX_ADDITIONAL_DETAILS_LENGTH)
                state.copy(
                    additionalDetails = trimmedText,
                    validationErrorResId = null,
                )
            } else {
                state
            }
        }
    }

    private fun submitReport() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ReportUiState.Input) return@launch

            val validationErrorResId = validateInput(currentState)
            if (validationErrorResId != null) {
                _uiState.update {
                    (it as ReportUiState.Input).copy(validationErrorResId = validationErrorResId)
                }
                return@launch
            }

            // Check auth
            val userId = (currentAuthState as? AuthState.Authenticated)?.userId
            if (userId == null) {
                Logger.w(TAG) { "Cannot submit: user is not authenticated" }
                _uiState.value = ReportUiState.Error(SystemError.Unknown)
                return@launch
            }

            // Update to submitting state
            _uiState.update {
                (it as ReportUiState.Input).copy(isSubmitting = true)
            }

            // Create report
            val report = ContentReport(
                id = UUID.randomUUID().toString(),
                reportedItemId = reportedItemId,
                reportedItemType = reportedItemType,
                reporterId = userId.value,
                reason = currentState.selectedReason!!,
                additionalDetails = currentState.additionalDetails.ifBlank { null },
                status = ReportStatus.PENDING,
                createdAt = Clock.System.now(),
            )

            // Submit
            when (val result = reportRepository.submitReport(report)) {
                is Result.Success -> {
                    Logger.i(TAG) { "Report submitted successfully" }
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = "report_submitted",
                            extras = listOf(
                                AnalyticsEvent.Param("item_type", reportedItemType.name),
                                AnalyticsEvent.Param("reason", currentState.selectedReason!!.name),
                            ),
                        ),
                    )
                    _uiState.value = ReportUiState.Success
                }
                is Result.Error -> {
                    Logger.e(TAG) { "Report submission failed: ${result.error}" }
                    _uiState.value = ReportUiState.Error(result.error)
                }
            }
        }
    }

    private fun validateInput(state: ReportUiState.Input): Int? {
        if (state.selectedReason == null) {
            return R.string.error_reason_required
        }

        if (state.selectedReason == ReportReason.OTHER && state.additionalDetails.isBlank()) {
            return R.string.error_details_required
        }

        return null
    }

    private fun dismissError() {
        _uiState.value = ReportUiState.Input()
    }
}
