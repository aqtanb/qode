package com.qodein.shared.data.datasource

import com.qodein.shared.data.dto.BlockDto
import com.qodein.shared.data.dto.UserDto
import com.qodein.shared.data.dto.UserInsertDto
import com.qodein.shared.data.dto.UserInteractionDto
import com.qodein.shared.data.util.Page
import com.qodein.shared.data.util.PageCursor
import com.qodein.shared.data.util.startAfter
import com.qodein.shared.data.util.toPage
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

class SupabaseUserDataSource(private val postgrest: Postgrest, private val auth: Auth, private val functions: Functions) {
    suspend fun createUser(dto: UserInsertDto) {
        postgrest.from(UserDto.TABLE_NAME).insert(dto)
    }

    /** Other users' rows are private, so they come through the name-and-photo RPC. */
    suspend fun getUserById(userId: String): UserDto? =
        if (userId == auth.currentUserOrNull()?.id) {
            postgrest.from(UserDto.TABLE_NAME).select { filter { eq("id", userId) } }.decodeSingleOrNull()
        } else {
            postgrest.rpc(
                function = "get_user_profile",
                parameters = buildJsonObject { put("profile_id", userId) },
            ).decodeList<UserDto>().firstOrNull()
        }

    suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ) {
        postgrest.from(UserDto.TABLE_NAME).update(
            buildJsonObject {
                put("legal_policies_accepted_at", Instant.fromEpochMilliseconds(legalPoliciesAcceptedAt).toString())
            },
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ) {
        postgrest.from(BlockDto.TABLE_NAME).upsert(
            buildJsonObject {
                put("user_id", currentUserId)
                put("blocked_user_id", blockedUserId)
            },
        ) {
            ignoreDuplicates = true
        }
    }

    suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ) {
        postgrest.from(BlockDto.TABLE_NAME).delete {
            filter {
                eq("user_id", currentUserId)
                eq("blocked_user_id", blockedUserId)
            }
        }
    }

    suspend fun getBlockedUserIds(currentUserId: String): Set<String> =
        postgrest.from(BlockDto.TABLE_NAME).select {
            filter { eq("user_id", currentUserId) }
        }.decodeList<BlockDto>().map { it.blockedUserId }.toSet()

    suspend fun getBlockedUsers(
        currentUserId: String,
        limit: Int,
        cursor: PageCursor?
    ): Page<BlockDto> =
        postgrest.from(BlockDto.TABLE_NAME).select {
            filter {
                eq("user_id", currentUserId)
                cursor?.let { startAfter(it, "blocked_at", ascending = false, idColumn = "blocked_user_id") }
            }
            order("blocked_at", Order.DESCENDING)
            order("blocked_user_id", Order.ASCENDING)
            limit(limit + 1L)
        }.decodeList<BlockDto>().toPage(limit) { block ->
            PageCursor(Instant.parse(block.blockedAt.orEmpty()).toString(), block.blockedUserId)
        }

    suspend fun deleteUserAccount() {
        functions.invoke("delete-account")
        auth.clearSession()
    }

    suspend fun getUserInteraction(
        itemType: String,
        itemId: String,
        userId: String
    ): UserInteractionDto? =
        postgrest.from(UserInteractionDto.TABLE_NAME).select {
            filter {
                eq("user_id", userId)
                eq("item_type", itemType)
                eq("item_id", itemId)
            }
        }.decodeSingleOrNull()

    suspend fun saveUserInteraction(
        itemType: String,
        itemId: String,
        voteState: String,
        bookmarked: Boolean
    ) {
        postgrest.rpc(
            function = "save_interaction",
            parameters = buildJsonObject {
                put("target_type", itemType)
                put("target_id", itemId)
                put("new_vote_state", voteState)
                put("is_bookmarked", bookmarked)
            },
        )
    }

    suspend fun deleteUserInteraction(
        itemType: String,
        itemId: String,
        userId: String
    ) {
        postgrest.from(UserInteractionDto.TABLE_NAME).delete {
            filter {
                eq("user_id", userId)
                eq("item_type", itemType)
                eq("item_id", itemId)
            }
        }
    }
}
