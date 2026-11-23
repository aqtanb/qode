package com.qodein.shared.ios

import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.usecase.post.GetPostsByUserUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesByUserUseCase
import com.qodein.shared.domain.usecase.user.DeleteUserAccountUseCase
import com.qodein.shared.domain.usecase.user.GetBlockedUsersUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.UnblockUserUseCase
import com.qodein.shared.domain.usecase.user.UpdateDisplayNameUseCase
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProfileItem(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val promocodeCount: Int,
    val postCount: Int
)

class BlockedUserItem(val id: String, val displayName: String?, val photoUrl: String?)

/** The signed-in user's profile, own content, blocks and account deletion. Paging state lives here. */
class ProfileApi : KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val getUserById: GetUserByIdUseCase by inject()
    private val getPromocodesByUser: GetPromocodesByUserUseCase by inject()
    private val getPostsByUser: GetPostsByUserUseCase by inject()
    private val getBlockedUsers: GetBlockedUsersUseCase by inject()
    private val unblockUserUseCase: UnblockUserUseCase by inject()
    private val deleteUserAccount: DeleteUserAccountUseCase by inject()
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase by inject()

    private var promocodeCursor: PaginationCursor<*>? = null
    private var postCursor: PaginationCursor<*>? = null
    private var blockedCursor: PaginationCursor<*>? = null

    val hasMorePromocodes: Boolean get() = promocodeCursor != null
    val hasMorePosts: Boolean get() = postCursor != null
    val hasMoreBlocked: Boolean get() = blockedCursor != null

    /** Null when signed out or not registered yet. */
    @Throws(Exception::class)
    suspend fun profile(): ProfileItem? {
        val authUser = authRepository.observeAuthState().first() ?: return null
        val user = call { getUserById(authUser.uid) }
        return ProfileItem(
            id = user.id.value,
            displayName = user.displayName,
            email = authUser.email,
            photoUrl = user.profile.photoUrl,
            promocodeCount = user.stats.submittedPromocodesCount,
            postCount = user.stats.submittedPostsCount,
        )
    }

    @Throws(Exception::class)
    suspend fun myPromocodes(nextPage: Boolean): List<PromocodeItem> {
        val cursor = if (nextPage) promocodeCursor ?: return emptyList() else null
        val page = call { getPromocodesByUser(requireUserId(), cursor) }
        promocodeCursor = page.nextCursor
        return page.data.map { it.toItem() }
    }

    @Throws(Exception::class)
    suspend fun myPosts(nextPage: Boolean): List<PostItem> {
        val cursor = if (nextPage) postCursor ?: return emptyList() else null
        val page = call { getPostsByUser(requireUserId(), cursor) }
        postCursor = page.nextCursor
        return page.data.map { it.toItem() }
    }

    @Throws(Exception::class)
    suspend fun blockedUsers(nextPage: Boolean): List<BlockedUserItem> {
        val cursor = if (nextPage) blockedCursor ?: return emptyList() else null
        val page = call { getBlockedUsers(cursor) }
        blockedCursor = page.nextCursor
        return page.data.map { BlockedUserItem(it.id.value, it.displayName, it.profile.photoUrl) }
    }

    /** Returns the saved name, or null when the blank input cleared it. */
    @Throws(Exception::class)
    suspend fun updateDisplayName(name: String): String? = call { updateDisplayNameUseCase(name) }

    @Throws(Exception::class)
    suspend fun unblock(userId: String) = call { unblockUserUseCase(userId) }

    /** Deletes the account and all its content on the server; the session ends with it. */
    @Throws(Exception::class)
    suspend fun deleteAccount() = call { deleteUserAccount() }

    private suspend fun requireUserId(): UserId =
        authRepository.observeAuthState().first()?.let { UserId(it.uid) }
            ?: throw QodeException("NoCredentialsAvailable", "Not signed in")
}
