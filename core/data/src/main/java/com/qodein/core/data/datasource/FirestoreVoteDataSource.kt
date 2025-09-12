package com.qodein.core.data.datasource

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.qodein.core.data.mapper.VoteMapper
import com.qodein.core.data.model.UserVoteDto
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import com.qodein.shared.model.VoteType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// TODO: Improve error handling

@Singleton
class FirestoreVoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions = Firebase.functions
) {
    companion object {
        private const val VOTES_COLLECTION = "votes"

        private fun sanitizeDocumentId(input: String): String = input.replace(Regex("[^a-zA-Z0-9_-]"), "_")

        private fun generateVoteId(
            itemId: String,
            userId: String
        ): String {
            val sanitizedItemId = sanitizeDocumentId(itemId)
            val sanitizedUserId = sanitizeDocumentId(userId)
            return "${sanitizedItemId}_$sanitizedUserId"
        }
    }

    /**
     * Vote on any content type with 3-state voting support.
     * Uses Firebase Cloud Functions for atomic transactions.
     */
    suspend fun voteOnContent(
        itemId: String,
        itemType: VoteType,
        userId: UserId,
        targetVoteState: VoteState
    ): Vote? {
        try {
            val data = hashMapOf(
                "itemId" to itemId,
                "itemType" to itemType.name,
                "userId" to userId.value,
                "voteState" to targetVoteState.name,
            )

            val result = functions
                .getHttpsCallable("handleVote")
                .call(data)
                .await()

            val resultData = result.data as? Map<String, Any>
                ?: throw IOException("Invalid Cloud Function response")

            if (resultData["success"] != true) {
                throw IOException("Vote operation failed: ${resultData["error"]}")
            }

            // Return updated vote or null if removed
            val voteState = resultData["voteState"] as? String
            return if (voteState == "NONE" || voteState == null) {
                null
            } else {
                Vote.create(
                    userId = userId,
                    itemId = itemId,
                    itemType = itemType,
                    voteState = VoteState.valueOf(voteState),
                )
            }
        } catch (e: Exception) {
            throw IOException("Failed to vote on content: ${e.message}", e)
        }
    }

    /**
     * Get user's current vote on any content type.
     * Returns real-time updates via Firestore listener.
     */
    fun getUserVote(
        itemId: String,
        itemType: VoteType,
        userId: UserId
    ): Flow<Vote?> =
        callbackFlow {
            val voteId = generateVoteId(itemId, userId.value)

            val listener = firestore.collection(VOTES_COLLECTION)
                .document(voteId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(IOException("Failed to get user vote: ${error.message}", error))
                        return@addSnapshotListener
                    }

                    try {
                        if (snapshot?.exists() == true) {
                            val dto = snapshot.toObject<UserVoteDto>()
                            if (dto != null) {
                                val vote = VoteMapper.toDomain(dto)
                                trySend(vote)
                            } else {
                                trySend(null)
                            }
                        } else {
                            trySend(null)
                        }
                    } catch (e: Exception) {
                        close(IOException("Failed to parse vote data: ${e.message}", e))
                    }
                }

            awaitClose { listener.remove() }
        }
}
