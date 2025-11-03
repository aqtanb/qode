package com.qodein.core.data.coordinator

import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.model.VoteState

class VotingCoordinator(
    private val toggleVoteUseCase: ToggleVoteUseCase,

    private val authStateManager: AuthStateManager,
    private val analyticsHelper: AnalyticsHelper
) {
    suspend fun handleVote(voteState: VoteState) {
        toggleVoteUseCase
    }
}
