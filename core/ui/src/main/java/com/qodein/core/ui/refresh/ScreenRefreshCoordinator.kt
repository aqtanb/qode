package com.qodein.core.ui.refresh

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centralized coordinator for triggering screen refreshes across the app.
 * This provides a reactive way to signal screens to refresh their data without
 * depending on navigation state or SavedStateHandle timing issues.
 *
 * Usage:
 * - Emit a refresh signal when data changes: `refreshCoordinator.triggerRefresh(RefreshTarget.HOME)`
 * - Observe signals in ViewModels to react to refresh events
 */
class ScreenRefreshCoordinator {

    private val _refreshSignals = MutableSharedFlow<RefreshTarget>(
        extraBufferCapacity = 1,
        // DROP_OLDEST ensures we don't block if no one is collecting
        // and the buffer is full
    )

    /**
     * Flow of refresh signals. Screens should collect this and react accordingly.
     */
    val refreshSignals: SharedFlow<RefreshTarget> = _refreshSignals.asSharedFlow()

    /**
     * Trigger a refresh for a specific screen/target.
     * This is a suspend function to ensure the signal is emitted.
     *
     * @param target The screen/target that should refresh
     */
    suspend fun triggerRefresh(target: RefreshTarget) {
        _refreshSignals.emit(target)
    }

    /**
     * Try to trigger a refresh without suspending.
     * Returns true if the signal was emitted, false if the buffer was full.
     *
     * @param target The screen/target that should refresh
     * @return true if signal was emitted successfully
     */
    fun tryTriggerRefresh(target: RefreshTarget): Boolean = _refreshSignals.tryEmit(target)
}

/**
 * Defines which screen/target should be refreshed
 */
enum class RefreshTarget {
    /**
     * Refresh the home screen (promocodes list)
     */
    HOME,

    /**
     * Refresh the feed screen (posts list)
     */
    FEED,

    /**
     * Refresh the profile screen
     */
    PROFILE
}
