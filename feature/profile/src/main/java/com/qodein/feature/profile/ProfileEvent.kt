package com.qodein.feature.profile

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
    data object NavigateToAuth : ProfileEvent
}
