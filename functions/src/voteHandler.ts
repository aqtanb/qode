import { onCall, HttpsError } from 'firebase-functions/v2/https';
import * as logger from 'firebase-functions/logger';
import * as admin from 'firebase-admin';

const db = admin.firestore();

interface VoteRequest {
  promoCodeId: string;
  isUpvote: boolean;
}

interface VoteResponse {
  success: boolean;
  voteId: string;
  isUpvote: boolean;
  action: 'added' | 'removed' | 'switched';
  newUpvotes: number;
  newDownvotes: number;
}

/**
 * Callable Function: Handle promo code votes with proper logic.
 *
 * This function handles all possible vote actions (add, remove, switch)
 * within a single Firestore transaction for data integrity. The logic for
 * updating vote counts is consolidated for a cleaner implementation.
 */
export const handlePromoCodeVote = onCall<VoteRequest, Promise<VoteResponse>>(
  async (request) => {
    // 1. Validate authentication
    if (!request.auth) {
      throw new HttpsError('unauthenticated', 'Must be authenticated to vote');
    }

    const { promoCodeId, isUpvote } = request.data;
    const userId = request.auth.uid;

    // 2. Validate input
    if (!promoCodeId || typeof promoCodeId !== 'string') {
      throw new HttpsError('invalid-argument', 'Valid promoCodeId is required');
    }

    if (typeof isUpvote !== 'boolean') {
      throw new HttpsError('invalid-argument', 'isUpvote must be a boolean');
    }

    const voteId = `${promoCodeId}_${userId}`;

    logger.info('Processing vote request', {
      userId,
      promoCodeId,
      isUpvote,
      voteId
    });

    try {
      // 3. Use transaction for atomic operations
      const result = await db.runTransaction(async (transaction) => {
        const voteRef = db.collection('votes').doc(voteId);
        const promoCodeRef = db.collection('promocodes').doc(promoCodeId);

        // Fetch both documents in a single round-trip for efficiency
        const [voteDoc, promoCodeDoc] = await transaction.getAll(voteRef, promoCodeRef);
        
        if (!promoCodeDoc.exists) {
          throw new HttpsError('not-found', 'Promo code not found');
        }

        const existingVote = voteDoc.exists ? voteDoc.data() : null;
        const promoCodeData = promoCodeDoc.data()!;
        const currentUpvotes = promoCodeData.upvotes || 0;
        const currentDownvotes = promoCodeData.downvotes || 0;

        let action: 'added' | 'removed' | 'switched';

        // First, determine the action and perform the transaction operation
        if (!existingVote) {
          action = 'added';
          transaction.set(voteRef, {
            promoCodeId,
            userId,
            isUpvote,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          logger.info('Adding new vote', { voteId, isUpvote });
        } else if (existingVote.isUpvote === isUpvote) {
          action = 'removed';
          transaction.delete(voteRef);
          logger.info('Removing existing vote', { voteId, isUpvote });
        } else {
          action = 'switched';
          transaction.update(voteRef, {
            isUpvote,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          logger.info('Switching vote', { voteId, from: existingVote.isUpvote, to: isUpvote });
        }
        
        // 4. Consolidate vote count changes based on the action
        let upvoteChange = 0;
        let downvoteChange = 0;

        switch (action) {
          case 'added':
            upvoteChange = isUpvote ? 1 : 0;
            downvoteChange = isUpvote ? 0 : 1;
            break;
          case 'removed':
            upvoteChange = isUpvote ? -1 : 0;
            downvoteChange = isUpvote ? 0 : -1;
            break;
          case 'switched':
            upvoteChange = isUpvote ? 1 : -1;
            downvoteChange = isUpvote ? -1 : 1;
            break;
        }

        const newUpvotes = Math.max(0, currentUpvotes + upvoteChange);
        const newDownvotes = Math.max(0, currentDownvotes + downvoteChange);

        // Apply vote count changes in a single operation
        transaction.update(promoCodeRef, {
          upvotes: newUpvotes,
          downvotes: newDownvotes,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // 5. Return the result
        return {
          success: true,
          voteId,
          isUpvote,
          action,
          newUpvotes,
          newDownvotes
        };
      });

      logger.info('Vote processed successfully', {
        userId,
        promoCodeId,
        result
      });

      return result;

    } catch (error) {
      logger.error('Error processing vote', {
        userId,
        promoCodeId,
        isUpvote,
        error: error instanceof Error ? error.message : String(error)
      });

      if (error instanceof HttpsError) {
        throw error;
      }

      throw new HttpsError('internal', 'Failed to process vote');
    }
  }
);