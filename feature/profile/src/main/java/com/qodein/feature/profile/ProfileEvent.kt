package com.qodein.feature.profile

sealed interface ProfileEvent {
    data object EditProfileRequested : ProfileEvent
    data object SignedOut : ProfileEvent
    data object LeaderboardRequested : ProfileEvent
}
