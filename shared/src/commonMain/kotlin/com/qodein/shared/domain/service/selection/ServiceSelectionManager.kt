package com.qodein.shared.domain.service.selection

import com.qodein.shared.model.ServiceId

/**
 * Pure domain manager for service selection business logic.
 * Contains no UI state, flows, or lifecycle management.
 * ViewModels coordinate this with other domain services.
 */
interface ServiceSelectionManager {
    /**
     * Apply a selection action to current state - pure function
     */
    fun applyAction(
        state: ServiceSelectionState,
        action: ServiceSelectionAction
    ): ServiceSelectionState

    /**
     * Validate a service selection according to business rules
     */
    fun validateSelection(selection: SelectionState): SelectionValidationResult

    /**
     * Get recommended services based on current selection
     */
    fun getRecommendedServices(selection: SelectionState): List<ServiceId>
}

/**
 * Result of selection validation
 */
sealed class SelectionValidationResult {
    data object Valid : SelectionValidationResult()
    data class Invalid(val reason: String) : SelectionValidationResult()
}
