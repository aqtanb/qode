package com.qodein.core.data.datasource

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
            val voteType = when (targetVoteState) {
                VoteState.UPVOTE -> "UPVOTE"
                VoteState.DOWNVOTE -> "DOWNVOTE"
                VoteState.NONE -> "REMOVE"
            }

            val data = hashMapOf(
                "itemId" to itemId,
                "itemType" to itemType.name,
                "voteType" to voteType,
            )

            // Verify user is authenticated - Firebase Functions will automatically use auth context
            val currentFirebaseUser = Firebase.auth.currentUser
                ?: throw SecurityException("User must be authenticated to vote")

            val result = functions
                .getHttpsCallable("handleContentVote")
                .call(data)
                .await()

            val resultData = result.data as? Map<String, Any>
                ?: throw IOException("Invalid Cloud Function response")

            if (resultData["success"] != true) {
                // Cloud Function returned failure - likely auth issue
                throw SecurityException("authentication failed: permission denied")
            }

            // Return updated vote or null if removed
            val currentVote = resultData["currentVote"] as? String
            return if (currentVote == null) {
                null
            } else {
                val voteState = when (currentVote) {
                    "UPVOTE" -> VoteState.UPVOTE
                    "DOWNVOTE" -> VoteState.DOWNVOTE
                    else -> VoteState.NONE
                }
                Vote.create(
                    userId = userId,
                    itemId = itemId,
                    itemType = itemType,
                    voteState = voteState,
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

    /**
     * Get user's vote status for a single item - MOST EFFICIENT.
     * Perfect for promo code detail screens.
     * Cost: 1 document read
     */
    suspend fun getUserVoteStatus(
        itemId: String,
        userId: UserId
    ): VoteState {
        val voteId = generateVoteId(itemId, userId.value)

        try {
            val document = firestore.collection(VOTES_COLLECTION)
                .document(voteId)
                .get()
                .await()

            return if (document.exists()) {
                val dto = document.toObject<UserVoteDto>()
                if (dto != null) {
                    VoteMapper.toDomain(dto).voteState
                } else {
                    VoteState.NONE
                }
            } else {
                VoteState.NONE
            }
        } catch (e: Exception) {
            // Return NONE on any error to prevent crashes
            return VoteState.NONE
        }
    }
}
