package com.qodein.feature.profile

sealed interface ProfileEvent {
    data object NavigateToEditProfile : ProfileEvent
    data object NavigateBack : ProfileEvent
    data object NavigateToSignOut : ProfileEvent
    data object NavigateToAchievements : ProfileEvent
    data object NavigateToUserJourney : ProfileEvent
}
