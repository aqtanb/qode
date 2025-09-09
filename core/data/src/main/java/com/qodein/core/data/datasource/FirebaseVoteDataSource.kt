package com.qodein.core.data.datasource

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.qodein.core.data.mapper.PromoCodeMapper
import com.qodein.core.data.model.PromoCodeVoteDto
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class ContentType {
    PROMO_CODE,
    POST,
    COMMENT,
    PROMO
}

enum class VoteType {
    UPVOTE,
    DOWNVOTE,
    REMOVE
}

data class VoteResult(
    val success: Boolean,
    val voteId: String,
    val currentVote: VoteType?,
    val action: String,
    val newUpvotes: Int,
    val newDownvotes: Int
)

@Singleton
class FirebaseVoteDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val VOTES_COLLECTION = "votes"

        private fun sanitizeDocumentId(input: String): String =
            input
                .lowercase()
                .replace(Regex("[^a-z0-9_-]"), "_")
                .replace(Regex("_{2,}"), "_")
                .replace(Regex("^_+|_+$"), "")

        private fun generateVoteId(
            itemId: String,
            userId: String
        ): String {
            val sanitizedItemId = sanitizeDocumentId(itemId)
            val sanitizedUserId = sanitizeDocumentId(userId)
            return "${sanitizedItemId}_$sanitizedUserId"
        }
    }

    private val functions: FirebaseFunctions = Firebase.functions

    /**
     * Generic voting method for all content types using 3-state voting
     */
    suspend fun voteOnContent(
        itemId: String,
        contentType: ContentType,
        voteType: VoteType,
        userId: UserId
    ): VoteResult {
        val data = hashMapOf(
            "itemId" to itemId,
            "itemType" to contentType.name,
            "voteType" to voteType.name,
        )

        val result = functions
            .getHttpsCallable("handleContentVote")
            .call(data)
            .await()

        val resultData = result.data as Map<String, Any>

        return VoteResult(
            success = resultData["success"] as Boolean,
            voteId = resultData["voteId"] as String,
            currentVote = (resultData["currentVote"] as String?)?.let { VoteType.valueOf(it) },
            action = resultData["action"] as String,
            newUpvotes = (resultData["newUpvotes"] as Long).toInt(),
            newDownvotes = (resultData["newDownvotes"] as Long).toInt(),
        )
    }

    /**
     * Specific method for PromoCode voting (for backwards compatibility with existing UI)
     */
    suspend fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        voteType: VoteType
    ): PromoCodeVote? {
        val result = voteOnContent(
            itemId = promoCodeId.value,
            contentType = ContentType.PROMO_CODE,
            voteType = voteType,
            userId = userId,
        )

        return if (result.currentVote != null) {
            PromoCodeVote(
                id = result.voteId,
                promoCodeId = promoCodeId,
                userId = userId,
                isUpvote = result.currentVote == VoteType.UPVOTE,
            )
        } else {
            null
        }
    }

    /**
     * Get current vote for any content type
     */
    suspend fun getUserVote(
        itemId: String,
        userId: UserId
    ): PromoCodeVote? {
        val voteId = generateVoteId(itemId, userId.value)

        val document = firestore.collection(VOTES_COLLECTION)
            .document(voteId)
            .get()
            .await()

        return document.toObject<PromoCodeVoteDto>()?.let { dto ->
            PromoCodeMapper.voteToDomain(dto)
        }
    }
}
