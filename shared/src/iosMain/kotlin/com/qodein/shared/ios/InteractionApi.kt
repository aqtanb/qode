package com.qodein.shared.ios

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.user.BlockUserUseCase
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.ReportReason
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSUUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Votes, reports and blocks for the signed-in user. Calls without a session throw code "NoCredentialsAvailable". */
class InteractionApi : KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val getUserInteraction: GetUserInteractionUseCase by inject()
    private val toggleVoteUseCase: ToggleVoteUseCase by inject()
    private val reportRepository: ReportRepository by inject()
    private val blockUserUseCase: BlockUserUseCase by inject()

    /** The user's current vote, or NONE when signed out. */
    @Throws(Exception::class)
    suspend fun voteState(
        itemId: String,
        isPost: Boolean
    ): VoteState {
        val userId = currentUserId() ?: return VoteState.NONE
        return call { getUserInteraction(itemId, contentType(isPost), userId) }?.voteState ?: VoteState.NONE
    }

    /** Applies [target] the way the vote buttons toggle and returns the saved vote. */
    @Throws(Exception::class)
    suspend fun vote(
        itemId: String,
        isPost: Boolean,
        current: VoteState,
        target: VoteState
    ): VoteState {
        val userId = requireUserId()
        return call {
            toggleVoteUseCase(
                itemId = itemId,
                itemType = contentType(isPost),
                userId = userId,
                currentVoteState = current,
                isBookmarked = false,
                targetVoteState = target,
            )
        }.voteState
    }

    @OptIn(ExperimentalTime::class)
    @Throws(Exception::class)
    suspend fun report(
        itemId: String,
        isPost: Boolean,
        reason: ReportReason,
        details: String?
    ) {
        val userId = requireUserId()
        call {
            reportRepository.submitReport(
                ContentReport(
                    id = NSUUID().UUIDString.lowercase(),
                    reportedItemId = itemId,
                    reportedItemType = contentType(isPost),
                    reporterId = userId.value,
                    reason = reason,
                    additionalDetails = details?.trim()?.ifBlank { null },
                    createdAt = Clock.System.now(),
                ),
            )
        }
    }

    @Throws(Exception::class)
    suspend fun block(authorId: String) = call { blockUserUseCase(UserId(authorId)) }

    private suspend fun currentUserId(): UserId? = authRepository.observeAuthState().first()?.let { UserId(it.uid) }

    private suspend fun requireUserId(): UserId =
        currentUserId() ?: call { Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable) }

    private fun contentType(isPost: Boolean) = if (isPost) ContentType.POST else ContentType.PROMOCODE
}
