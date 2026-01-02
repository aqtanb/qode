package com.qodein.qode.ui.state

import com.qodein.qode.navigation.NavigationActions

sealed interface AppUiEvents {
    data class Navigate(val action: NavigationActions) : AppUiEvents
}
