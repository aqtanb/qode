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

@Singleton
class FirebaseVoteDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val VOTES_COLLECTION = "votes"
    }

    private val functions: FirebaseFunctions = Firebase.functions

    suspend fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): PromoCodeVote? {
        val data = hashMapOf(
            "promoCodeId" to promoCodeId.value,
            "isUpvote" to isUpvote,
        )

        val result = functions
            .getHttpsCallable("handlePromoCodeVote")
            .call(data)
            .await()

        val resultData = result.data as Map<String, Any>
        val action = resultData["action"] as String

        return when (action) {
            "removed" -> null
            "added", "switched" -> {
                val voteId = resultData["voteId"] as String
                val finalIsUpvote = resultData["isUpvote"] as Boolean

                PromoCodeVote(
                    id = voteId,
                    promoCodeId = promoCodeId,
                    userId = userId,
                    isUpvote = finalIsUpvote,
                )
            }
            else -> throw IllegalStateException("Unknown action: $action")
        }
    }

    suspend fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): PromoCodeVote? {
        val voteId = "${promoCodeId.value}_${userId.value}"

        val document = firestore.collection(VOTES_COLLECTION)
            .document(voteId)
            .get()
            .await()

        return document.toObject<PromoCodeVoteDto>()?.let { dto ->
            PromoCodeMapper.voteToDomain(dto)
        }
    }
}
