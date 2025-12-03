package com.qodein.core.ui.state

import com.qodein.shared.model.UserId

/**
 * Lightweight UI-layer auth state shared across flows.
 * Mirrors domain auth state while distinguishing:
 * - Uninitialized: initial probe; avoid showing UI yet
 * - SigningIn: user-triggered sign-in in progress
 * - Unauthenticated/Authenticated: steady states
 */
sealed interface UiAuthState {
    data object Uninitialized : UiAuthState
    data object SigningIn : UiAuthState
    data object Unauthenticated : UiAuthState
    data class Authenticated(val userId: UserId) : UiAuthState
}

fun UiAuthState.shouldShowAuthSheet(): Boolean =
    when (this) {
        is UiAuthState.Authenticated -> false
        UiAuthState.Unauthenticated, UiAuthState.SigningIn -> true
        UiAuthState.Uninitialized -> false
    }
