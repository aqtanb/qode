package com.qodein.feature.promocode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.usecase.promocode.CreatePromoCodeUseCase
import com.qodein.core.model.PromoCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionViewModel @Inject constructor(private val createPromoCodeUseCase: CreatePromoCodeUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Loading)
    val uiState: StateFlow<SubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubmissionEvent>()
    val events = _events.asSharedFlow()

    init {
        initialize()
    }

    fun onAction(action: SubmissionAction) {
        when (action) {
            is SubmissionAction.UpdateServiceName -> updateServiceName(action.serviceName)
            is SubmissionAction.UpdatePromoCode -> updatePromoCode(action.promoCode)
            is SubmissionAction.UpdateDescription -> updateDescription(action.description)
            SubmissionAction.SubmitPromoCode -> submitPromoCode()
            SubmissionAction.RetryClicked -> initialize()
            SubmissionAction.ErrorDismissed -> {
                // Handle error dismissal if needed
            }
        }
    }

    private fun initialize() {
        _uiState.value = SubmissionUiState.Success()
    }

    private fun updateServiceName(serviceName: String) {
        val currentState = _uiState.value
        if (currentState is SubmissionUiState.Success) {
            _uiState.value = currentState.copy(serviceName = serviceName)
        }
    }

    private fun updatePromoCode(promoCode: String) {
        val currentState = _uiState.value
        if (currentState is SubmissionUiState.Success) {
            _uiState.value = currentState.copy(promoCode = promoCode)
        }
    }

    private fun updateDescription(description: String) {
        val currentState = _uiState.value
        if (currentState is SubmissionUiState.Success) {
            _uiState.value = currentState.copy(description = description)
        }
    }

    private fun submitPromoCode() {
        val currentState = _uiState.value
        if (currentState is SubmissionUiState.Success && currentState.canSubmit) {
            _uiState.value = currentState.copy(isSubmitting = true)

            viewModelScope.launch {
                // Create a percentage promo code for testing
                val promoCodeResult = PromoCode.createPercentage(
                    code = currentState.promoCode,
                    serviceName = currentState.serviceName,
                    discountPercentage = 10.0,
                    maximumDiscount = 1000.0,
                    description = currentState.description.takeIf { it.isNotBlank() },
                )

                promoCodeResult.fold(
                    onSuccess = { promoCode ->
                        createPromoCodeUseCase(promoCode)
                            .catch { exception ->
                                _uiState.value = SubmissionUiState.Error(exception)
                            }
                            .collect { result ->
                                result.fold(
                                    onSuccess = {
                                        _events.emit(SubmissionEvent.PromoCodeSubmitted)
                                        _events.emit(SubmissionEvent.NavigateBack)
                                    },
                                    onFailure = { exception ->
                                        _uiState.value = SubmissionUiState.Error(exception)
                                    },
                                )
                            }
                    },
                    onFailure = { exception ->
                        _uiState.value = SubmissionUiState.Error(exception)
                    },
                )
            }
        }
    }
}
