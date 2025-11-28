package com.qodein.core.ui.state

import com.qodein.shared.model.User

/**
 * Lightweight UI-layer auth state shared across flows.
 * Mirrors domain auth state with an optional loading placeholder
 * and carries the authenticated user for convenience.
 */
sealed interface UiAuthState {
    data object Loading : UiAuthState
    data object Unauthenticated : UiAuthState
    data class Authenticated(val user: User) : UiAuthState
}
