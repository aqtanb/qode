package com.qodein.feature.promocode.submission

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.util.ImageCompressor
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.promocode.EnqueuePromocodeSubmissionUseCase
import com.qodein.shared.domain.usecase.service.GetServiceLogoUrlUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeCode
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Instant

class PromocodeSubmissionViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val enqueuePromocodeSubmissionUseCase: EnqueuePromocodeSubmissionUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getServicesByIdsUseCase: GetServicesByIdsUseCase,
    private val getServiceLogoUrlUseCase: GetServiceLogoUrlUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PromocodeSubmissionUiState())
    val uiState: StateFlow<PromocodeSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated
    private var pendingSubmission: Boolean = false

    companion object {
        private const val TAG = "PromocodeSubmissionViewModel"
        private const val EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION = "progressive_step_navigation"
        private const val PARAM_STEP_FROM = "step_from"
        private const val PARAM_STEP_TO = "step_to"
        private const val PARAM_DIRECTION = "direction"
        private const val DIRECTION_NEXT = "next"
        private const val DIRECTION_PREVIOUS = "previous"
    }

    init {
        setupAuthStateMonitoring()
        observeAuthResult()
    }

    fun onAction(action: PromocodeSubmissionAction) {
        when (action) {
            PromocodeSubmissionAction.NextProgressiveStep -> goToNextProgressiveStep()
            PromocodeSubmissionAction.PreviousProgressiveStep -> goToPreviousProgressiveStep()
            is PromocodeSubmissionAction.NavigateToStep -> navigateToStep(action.step)

            PromocodeSubmissionAction.ShowServiceSelector -> showServiceSelector()
            PromocodeSubmissionAction.ToggleManualEntry -> toggleManualEntry()
            PromocodeSubmissionAction.ConfirmServiceLogo -> confirmServiceLogo()
            PromocodeSubmissionAction.DismissServiceConfirmation -> dismissServiceConfirmation()

            is PromocodeSubmissionAction.UpdateServiceName -> updateWizardData { it.copy(serviceName = action.serviceName) }
            is PromocodeSubmissionAction.UpdateServiceUrl -> updateWizardData { it.copy(serviceUrl = action.serviceUrl) }
            is PromocodeSubmissionAction.UpdatePromocodeType -> updateWizardData { it.copy(promocodeType = action.type) }
            is PromocodeSubmissionAction.UpdatePromocode -> updatePromocode(action.promocode)
            is PromocodeSubmissionAction.UpdateDiscountPercentage -> updateWizardData { it.copy(discountPercentage = action.percentage) }
            is PromocodeSubmissionAction.UpdateDiscountAmount -> updateWizardData { it.copy(discountAmount = action.amount) }
            is PromocodeSubmissionAction.UpdateFreeItemDescription -> updateWizardData { it.copy(freeItemDescription = action.description) }
            is PromocodeSubmissionAction.UpdateMinimumOrderAmount -> updateWizardData { it.copy(minimumOrderAmount = action.amount) }
            is PromocodeSubmissionAction.UpdateDescription -> updateWizardData { it.copy(description = action.description) }
            is PromocodeSubmissionAction.UpdateStartDate -> updateWizardData { it.copy(startDate = action.date) }
            is PromocodeSubmissionAction.UpdateEndDate -> updateWizardData { it.copy(endDate = action.date) }

            is PromocodeSubmissionAction.PickImages -> pickImages()
            is PromocodeSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)
            is PromocodeSubmissionAction.RemoveImage -> removeImage(action.index)
            PromocodeSubmissionAction.SubmitPromoCode -> submitPromoCode()
        }
    }

    internal fun applyServiceSelection(serviceId: ServiceId) {
        viewModelScope.launch {
            val result = getServicesByIdsUseCase(setOf(serviceId)).firstOrNull() ?: return@launch
            updateWizardData { it.copy(selectedService = result) }
            Logger.d(TAG) { "Service selected: ${result.name}" }
        }
    }

    private fun showServiceSelector() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentServiceId = currentState.wizardData.selectedService?.id
            _events.emit(PromocodeSubmissionEvent.ShowServiceSelection(currentServiceId))
            Logger.i(TAG) { "Service selection dialog shown" }
        }
    }

    private fun toggleManualEntry() {
        updateWizardData { data ->
            data.copy(
                isManualServiceEntry = !data.isManualServiceEntry,
                // Clear the other service selection method when toggling
                selectedService = if (!data.isManualServiceEntry) null else data.selectedService,
                serviceName = if (data.isManualServiceEntry) "" else data.serviceName,
                serviceUrl = if (data.isManualServiceEntry) "" else data.serviceUrl,
            )
        }
    }

    private fun goToNextProgressiveStep() {
        val currentState = _uiState.value

        if (currentState.currentStep == PromocodeWizardStep.SERVICE &&
            currentState.wizardData.isManualServiceEntry &&
            currentState.canGoNext
        ) {
            validateServiceLogoAndProceed()
            return
        }

        _uiState.update { state ->
            if (state.canGoNext) {
                val currentStep = state.currentStep
                val nextStep = currentStep.next()
                if (nextStep != null) {
                    state.copy(currentStep = nextStep)
                } else {
                    state
                }
            } else {
                state
            }
        }
    }

    private fun validateServiceLogoAndProceed() {
        val currentState = _uiState.value
        val rawDomain = currentState.wizardData.serviceUrl

        val sanitized = Service.sanitizeUrl(rawDomain)
        updateWizardData { it.copy(serviceUrl = sanitized) }

        viewModelScope.launch {
            when (val result = getServiceLogoUrlUseCase(sanitized)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            serviceConfirmationDialog = ServiceConfirmationDialogState(
                                serviceName = state.wizardData.serviceName,
                                serviceUrl = sanitized,
                                logoUrl = result.data,
                            ),
                        )
                    }
                }
                is Result.Error -> {
                    _events.emit(PromocodeSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun goToPreviousProgressiveStep() {
        _uiState.update { currentState ->
            if (currentState.canGoPrevious) {
                val currentStep = currentState.currentStep
                val previousStep = currentStep.previous()
                if (previousStep != null) {
                    // Track progressive step navigation
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                            extras = listOf(
                                AnalyticsEvent.Param(PARAM_STEP_FROM, currentStep.name),
                                AnalyticsEvent.Param(PARAM_STEP_TO, previousStep.name),
                                AnalyticsEvent.Param(PARAM_DIRECTION, DIRECTION_PREVIOUS),
                            ),
                        ),
                    )
                    currentState.copy(currentStep = previousStep)
                } else {
                    currentState
                }
            } else {
                currentState
            }
        }
    }

    private fun navigateToStep(targetStep: PromocodeWizardStep) {
        _uiState.update { currentState ->
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = EVENT_TYPE_PROGRESSIVE_STEP_NAVIGATION,
                    extras = listOf(
                        AnalyticsEvent.Param(PARAM_STEP_FROM, currentState.currentStep.name),
                        AnalyticsEvent.Param(PARAM_STEP_TO, targetStep.name),
                        AnalyticsEvent.Param(PARAM_DIRECTION, "DIRECT"),
                    ),
                ),
            )
            currentState.copy(currentStep = targetStep)
        }
    }

    private fun setupAuthStateMonitoring() {
        viewModelScope.launch {
            getAuthStateUseCase()
                .collect { authState ->
                    currentAuthState = authState
                }
        }
    }

    private fun observeAuthResult() {
        viewModelScope.launch {
            savedStateHandle.getStateFlow("auth_result", "")
                .collect { result ->
                    if (result == "success") {
                        // Execute pending submission if auth was successful
                        if (pendingSubmission) {
                            submitPromoCode()
                            pendingSubmission = false
                        }
                        // Reset the flag so it doesn't trigger again
                        savedStateHandle["auth_result"] = ""
                    }
                }
        }
    }

    private fun confirmServiceLogo() {
        _uiState.update { state ->
            val nextStep = state.currentStep.next()
            state.copy(
                serviceConfirmationDialog = null,
                currentStep = nextStep ?: state.currentStep,
            )
        }
    }

    private fun dismissServiceConfirmation() {
        _uiState.update { it.copy(serviceConfirmationDialog = null) }
    }

    private fun pickImages() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.wizardData.imageUris.size >= Promocode.MAX_IMAGES) {
                return@launch
            }
            _events.emit(PromocodeSubmissionEvent.PickImagesRequested)
        }
    }

    private fun updateImageUris(uris: List<String>) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentImages = currentState.wizardData.imageUris
            val availableSlots = Promocode.MAX_IMAGES - currentImages.size

            if (availableSlots == 0) {
                _events.emit(PromocodeSubmissionEvent.ImageLimitReached)
                return@launch
            }

            val urisToCompress = uris.take(availableSlots).map { it.toUri() }
            if (uris.size > availableSlots) {
                _events.emit(PromocodeSubmissionEvent.ImagesPartiallyAdded(urisToCompress.size))
            }

            if (urisToCompress.isEmpty()) return@launch

            _uiState.update { it.copy(isCompressing = true) }

            val result = ImageCompressor.compressImages(
                context = getApplication(),
                uris = urisToCompress,
            )

            when (result) {
                is Result.Success -> {
                    val compressedUriStrings = result.data.map { it.toString() }
                    updateWizardData { data ->
                        data.copy(imageUris = currentImages + compressedUriStrings)
                    }
                }
                is Result.Error -> {
                    _events.emit(PromocodeSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }

            _uiState.update { it.copy(isCompressing = false) }
        }
    }

    private fun removeImage(index: Int) {
        updateWizardData { data ->
            data.copy(imageUris = data.imageUris.filterIndexed { i, _ -> i != index })
        }
    }

    // MARK: - Submission Logic

    /**
     * Submit promocode with explicit user (for external calls)
     */
    private fun submitPromoCode(user: User) {
        Logger.i(TAG) { "submitPromoCode() called with user: ${user.id.value}" }
        performSubmission(user)
    }

    private fun updatePromocode(rawCode: String) {
        val validationResult = PromocodeCode.create(rawCode)
        updateWizardData { it.copy(promocode = rawCode) }
    }

    /**
     * Submit promocode using authenticated user from current state
     */
    private fun submitPromoCode() {
        Logger.i(TAG) { "submitPromoCode() called - using authenticated user" }

        val userId = (currentAuthState as? AuthState.Authenticated)?.userId
        if (userId == null) {
            Logger.w(TAG) { "Cannot submit: user is not authenticated" }
            return
        }

        viewModelScope.launch {
            when (val userResult = getUserByIdUseCase(userId.value)) {
                is Result.Success -> performSubmission(userResult.data)
                is Result.Error -> {
                    Logger.w(TAG) { "Cannot submit: failed to load user profile: ${userResult.error}" }
                    _events.emit(PromocodeSubmissionEvent.ShowError(userResult.error.toUiText()))
                }
            }
        }
    }

    /**
     * Core submission logic - creates Promocode and delegates to use case
     */
    private fun performSubmission(user: User) {
        val currentState = _uiState.value

        if (!currentState.canSubmit) {
            Logger.w(TAG) { "Cannot submit: validation failed" }
            return
        }

        val wizardData = currentState.wizardData
        Logger.d(TAG) { "Submitting: service='${wizardData.effectiveServiceName}', code='${wizardData.promocode}'" }

        viewModelScope.launch {
            val serviceRef = wizardData.selectedService?.id?.let { ServiceRef.ById(it) }
                ?: ServiceRef.ByName(wizardData.effectiveServiceName, wizardData.effectiveServiceUrl)

            val discount = when (wizardData.promocodeType) {
                PromocodeType.PERCENTAGE -> Discount.Percentage(wizardData.discountPercentage.toDoubleOrNull() ?: 0.0)
                PromocodeType.FIXED_AMOUNT -> Discount.FixedAmount(wizardData.discountAmount.toDoubleOrNull() ?: 0.0)
                PromocodeType.FREE_ITEM -> Discount.FreeItem(wizardData.freeItemDescription)
                null -> {
                    _events.emit(PromocodeSubmissionEvent.ShowError(PromocodeError.CreationFailure.InvalidPromocodeId.toUiText()))
                    return@launch
                }
            }

            val minimumOrder = wizardData.minimumOrderAmount.toDoubleOrNull()
            val endDate = wizardData.endDate

            if (minimumOrder == null || endDate == null) {
                _events.emit(PromocodeSubmissionEvent.ShowError(PromocodeError.CreationFailure.InvalidPromocodeId.toUiText()))
                return@launch
            }

            when (
                val result = enqueuePromocodeSubmissionUseCase(
                    code = wizardData.promocode,
                    service = serviceRef,
                    userId = user.id,
                    discount = discount,
                    minimumOrderAmount = minimumOrder,
                    startDate = wizardData.startDate.toInstant(),
                    endDate = endDate.toInstant(),
                    description = wizardData.description.takeIf { it.isNotBlank() },
                    imageUris = wizardData.imageUris,
                    isFirstUserOnly = false,
                    isOneTimeUseOnly = false,
                    isVerified = false,
                )
            ) {
                is Result.Success -> {
                    _events.emit(PromocodeSubmissionEvent.PromocodeSubmitted)
                }
                is Result.Error -> {
                    _events.emit(PromocodeSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun LocalDate.toInstant(): Instant =
        Instant.fromEpochMilliseconds(
            atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )

    private fun updateWizardData(update: (SubmissionWizardData) -> SubmissionWizardData) {
        _uiState.update { state ->
            state.copy(wizardData = update(state.wizardData))
        }
    }
}
