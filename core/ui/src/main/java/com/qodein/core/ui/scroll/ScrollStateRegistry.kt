package com.qodein.core.ui.scroll

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

interface ScrollStateRegistry {

    fun registerScrollState(scrollableState: ScrollableState?)

    fun unregisterScrollState()
}

@Composable
fun ScrollStateRegistry.RegisterScrollState(scrollableState: ScrollableState?) {
    // Register immediately on every recomposition to handle tab switching
    DisposableEffect(scrollableState) {
        registerScrollState(scrollableState)
        onDispose {
            unregisterScrollState()
        }
    }
}
