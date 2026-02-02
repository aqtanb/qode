package com.qodein.feature.promocode.submission

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.UiText
import com.qodein.core.ui.util.ImageCompressor
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.common.permission.NotificationPermissionChecker
import com.qodein.shared.common.permission.NotificationPermissionState
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.promocode.EnqueuePromocodeSubmissionUseCase
import com.qodein.shared.domain.usecase.service.GetServiceLogoUrlUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
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
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getServicesByIdsUseCase: GetServicesByIdsUseCase,
    private val getServiceLogoUrlUseCase: GetServiceLogoUrlUseCase,
    private val notificationPermissionChecker: NotificationPermissionChecker
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PromocodeSubmissionUiState())
    val uiState: StateFlow<PromocodeSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated
    private var pendingSubmission: Boolean = false

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
            is PromocodeSubmissionAction.UpdateServiceUrl -> updateServiceUrl(action.serviceUrl)
            is PromocodeSubmissionAction.UpdatePromocodeType -> updateWizardData { it.copy(promocodeType = action.type) }
            is PromocodeSubmissionAction.UpdatePromocode -> updatePromocode(action.promocode)
            is PromocodeSubmissionAction.UpdateDiscountPercentage -> updateDiscountPercentage(action.percentage)
            is PromocodeSubmissionAction.UpdateDiscountAmount -> updateDiscountAmount(action.amount)
            is PromocodeSubmissionAction.UpdateFreeItemDescription -> updateWizardData { it.copy(freeItemDescription = action.description) }
            is PromocodeSubmissionAction.UpdateMinimumOrderAmount -> updateMinimumOrderAmount(action.amount)
            is PromocodeSubmissionAction.UpdateDescription -> updateDescription(action.description)
            is PromocodeSubmissionAction.UpdateStartDate -> updateWizardData { it.copy(startDate = action.date) }
            is PromocodeSubmissionAction.UpdateEndDate -> updateWizardData { it.copy(endDate = action.date) }

            is PromocodeSubmissionAction.PickImages -> pickImages()
            is PromocodeSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)
            is PromocodeSubmissionAction.RemoveImage -> removeImage(action.index)
            PromocodeSubmissionAction.SubmitPromoCode -> submitPromoCode()
        }
    }

    private fun updateServiceUrl(serviceUrl: String) {
        val sanitizedUrl = Service.sanitizeUrl(serviceUrl)
        updateWizardData { it.copy(serviceUrl = sanitizedUrl) }
    }

    internal fun applyServiceSelection(serviceId: ServiceId) {
        viewModelScope.launch {
            val result = getServicesByIdsUseCase(setOf(serviceId)).firstOrNull() ?: return@launch
            updateWizardData { it.copy(selectedService = result) }
            Logger.d { "Service selected: ${result.name}" }
        }
    }

    private fun showServiceSelector() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentServiceId = currentState.wizardData.selectedService?.id
            _events.emit(PromocodeSubmissionEvent.ShowServiceSelection(currentServiceId))
            Logger.i { "Service selection dialog shown" }
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

    private fun validateServiceStep(): Result<Unit, PromocodeError.CreationFailure> {
        val data = _uiState.value.wizardData
        return when {
            !data.isManualServiceEntry && data.selectedService == null ->
                Result.Error(PromocodeError.CreationFailure.ServiceNotSelected)
            data.isManualServiceEntry && data.serviceName.isBlank() ->
                Result.Error(PromocodeError.CreationFailure.EmptyServiceName)
            data.isManualServiceEntry && data.serviceUrl.isBlank() ->
                Result.Error(PromocodeError.CreationFailure.EmptyServiceUrl)
            else -> Result.Success(Unit)
        }
    }

    private fun validatePromocodeStep(): Result<Unit, PromocodeError.CreationFailure> {
        val data = _uiState.value.wizardData
        return when {
            data.promocode.isBlank() ->
                Result.Error(PromocodeError.CreationFailure.EmptyCode)
            data.promocodeType == null ->
                Result.Error(PromocodeError.CreationFailure.PromocodeTypeNotSelected)
            else -> Promocode.validateCode(data.promocode)
        }
    }

    private fun validateDiscountValueStep(): Result<Unit, PromocodeError.CreationFailure> {
        val data = _uiState.value.wizardData

        val sanitizedMinimum = Promocode.sanitizeMonetaryValue(data.minimumOrderAmount)
        if (sanitizedMinimum.isBlank()) {
            return Result.Error(PromocodeError.CreationFailure.EmptyMinimumOrderAmount)
        }
        val minimumOrder = sanitizedMinimum.toDoubleOrNull()
            ?: return Result.Error(PromocodeError.CreationFailure.InvalidMinimumAmount)

        when (val result = Promocode.validateMinimumOrderAmount(minimumOrder)) {
            is Result.Error -> return result
            is Result.Success -> Unit
        }

        val discount = when (data.promocodeType) {
            PromocodeType.PERCENTAGE -> {
                val sanitized = Promocode.sanitizeMonetaryValue(data.discountPercentage)
                if (sanitized.isBlank()) {
                    return Result.Error(PromocodeError.CreationFailure.EmptyDiscountPercentage)
                }
                val percentage = sanitized.toDoubleOrNull()
                    ?: return Result.Error(PromocodeError.CreationFailure.InvalidPercentageDiscount)
                Discount.Percentage(percentage)
            }
            PromocodeType.FIXED_AMOUNT -> {
                val sanitized = Promocode.sanitizeMonetaryValue(data.discountAmount)
                if (sanitized.isBlank()) {
                    return Result.Error(PromocodeError.CreationFailure.EmptyDiscountAmount)
                }
                val amount = sanitized.toDoubleOrNull()
                    ?: return Result.Error(PromocodeError.CreationFailure.InvalidFixedAmountDiscount)
                Discount.FixedAmount(amount)
            }
            PromocodeType.FREE_ITEM -> {
                if (data.freeItemDescription.isBlank()) {
                    return Result.Error(PromocodeError.CreationFailure.EmptyFreeItemDescription)
                }
                Discount.FreeItem(data.freeItemDescription)
            }
            null -> return Result.Error(PromocodeError.CreationFailure.PromocodeTypeNotSelected)
        }

        return discount.validate(minimumOrder)
    }

    private fun validateDatesStep(): Result<Unit, PromocodeError.CreationFailure> {
        val data = _uiState.value.wizardData
        if (data.endDate == null) {
            return Result.Error(PromocodeError.CreationFailure.InvalidDateRange)
        }
        return Promocode.validateDateRange(data.startDate.toInstant(), data.endDate.toInstant())
    }

    private fun goToNextProgressiveStep() {
        val currentState = _uiState.value

        // Validate current step
        val validationResult = when (currentState.currentStep) {
            PromocodeWizardStep.SERVICE -> validateServiceStep()
            PromocodeWizardStep.PROMOCODE -> validatePromocodeStep()
            PromocodeWizardStep.DISCOUNT_VALUE -> validateDiscountValueStep()
            PromocodeWizardStep.DATES -> validateDatesStep()
            PromocodeWizardStep.DESCRIPTION -> Promocode.validateDescription(currentState.wizardData.description.ifBlank { null })
        }

        if (validationResult is Result.Error) {
            viewModelScope.launch {
                _events.emit(PromocodeSubmissionEvent.ShowSnackbar(validationResult.error.toUiText()))
            }
            return
        }

        // Existing logic for service logo validation
        if (currentState.currentStep == PromocodeWizardStep.SERVICE &&
            currentState.wizardData.isManualServiceEntry
        ) {
            validateServiceLogoAndProceed()
            return
        }

        // Proceed to next step
        _uiState.update { state ->
            state.currentStep.next()?.let { nextStep ->
                state.copy(currentStep = nextStep)
            } ?: state
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
                    _events.emit(PromocodeSubmissionEvent.ShowSnackbar(result.error.toUiText()))
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
        _uiState.update { currentState -> currentState.copy(currentStep = targetStep) }
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
                _events.emit(
                    PromocodeSubmissionEvent.ShowSnackbar(
                        UiText.StringResource(R.string.promocode_image_limit_reached),
                    ),
                )
                return@launch
            }

            val urisToCompress = uris.take(availableSlots).map { it.toUri() }
            if (uris.size > availableSlots) {
                _events.emit(
                    PromocodeSubmissionEvent.ShowSnackbar(
                        UiText.StringResource(R.string.promocode_images_partially_added),
                    ),
                )
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
                    _events.emit(PromocodeSubmissionEvent.ShowSnackbar(result.error.toUiText()))
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

    private fun updatePromocode(rawCode: String) {
        val sanitizedPromocode = Promocode.sanitizeCode(rawCode)
        updateWizardData { it.copy(promocode = sanitizedPromocode) }
    }

    private fun updateDiscountPercentage(rawPercentage: String) {
        val sanitized = Promocode.sanitizeMonetaryValue(rawPercentage)
        updateWizardData { it.copy(discountPercentage = sanitized) }
    }

    private fun updateDiscountAmount(rawAmount: String) {
        val sanitized = Promocode.sanitizeMonetaryValue(rawAmount)
        updateWizardData { it.copy(discountAmount = sanitized) }
    }

    private fun updateMinimumOrderAmount(rawAmount: String) {
        val sanitized = Promocode.sanitizeMonetaryValue(rawAmount)
        updateWizardData { it.copy(minimumOrderAmount = sanitized) }
    }

    private fun updateDescription(rawDescription: String) {
        val sanitized = Promocode.sanitizeDescription(rawDescription)
        updateWizardData { it.copy(description = sanitized) }
    }

    private fun submitPromoCode() {
        when (notificationPermissionChecker.checkPermission()) {
            NotificationPermissionState.Denied -> {
                viewModelScope.launch {
                    _events.emit(PromocodeSubmissionEvent.RequestNotificationPermission)
                }
                return
            }
            NotificationPermissionState.Granted,
            NotificationPermissionState.NotRequired -> {
                proceedWithSubmission()
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted || !notificationPermissionChecker.isPermissionRequired()) {
            proceedWithSubmission()
        } else {
            viewModelScope.launch {
                _events.emit(
                    PromocodeSubmissionEvent.ShowSnackbar(
                        UiText.StringResource(com.qodein.core.ui.R.string.notification_permission_denied_message),
                    ),
                )
            }
            proceedWithSubmission()
        }
    }

    private fun proceedWithSubmission() {
        Logger.i { "submitPromoCode() called - using authenticated user" }

        val userId = (currentAuthState as? AuthState.Authenticated)?.userId
        if (userId == null) {
            Logger.w { "Cannot submit: user is not authenticated" }
            return
        }

        viewModelScope.launch {
            when (val userResult = getUserByIdUseCase(userId.value)) {
                is Result.Success -> performSubmission(userResult.data)
                is Result.Error -> {
                    Logger.w { "Cannot submit: failed to load user profile: ${userResult.error}" }
                    _events.emit(PromocodeSubmissionEvent.ShowSnackbar(userResult.error.toUiText()))
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
            Logger.w { "Cannot submit: validation failed" }
            return
        }

        val wizardData = currentState.wizardData
        Logger.d { "Submitting: service='${wizardData.effectiveServiceName}', code='${wizardData.promocode}'" }

        viewModelScope.launch {
            val serviceRef = wizardData.selectedService?.id?.let { ServiceRef.ById(it) }
                ?: ServiceRef.ByName(wizardData.effectiveServiceName, wizardData.effectiveServiceUrl)

            val discount = when (wizardData.promocodeType) {
                PromocodeType.PERCENTAGE -> Discount.Percentage(wizardData.discountPercentage.toDoubleOrNull() ?: 0.0)
                PromocodeType.FIXED_AMOUNT -> Discount.FixedAmount(wizardData.discountAmount.toDoubleOrNull() ?: 0.0)
                PromocodeType.FREE_ITEM -> Discount.FreeItem(wizardData.freeItemDescription)
                null -> {
                    _events.emit(PromocodeSubmissionEvent.ShowSnackbar(PromocodeError.CreationFailure.InvalidPromocodeId.toUiText()))
                    return@launch
                }
            }

            val minimumOrder = wizardData.minimumOrderAmount.toDoubleOrNull()
            val endDate = wizardData.endDate

            if (minimumOrder == null || endDate == null) {
                _events.emit(PromocodeSubmissionEvent.ShowSnackbar(PromocodeError.CreationFailure.InvalidPromocodeId.toUiText()))
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
                    _events.emit(PromocodeSubmissionEvent.ShowSnackbar(result.error.toUiText()))
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
