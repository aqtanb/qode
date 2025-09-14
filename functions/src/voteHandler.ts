import { onCall, HttpsError } from 'firebase-functions/v2/https';
import * as logger from 'firebase-functions/logger';
import * as admin from 'firebase-admin';

/**
 * Sanitizes a string for use in Firestore document IDs.
 * Converts to lowercase and replaces invalid characters with underscores.
 */
function sanitizeDocumentId(input: string): string {
  return input
    .toLowerCase()
    .replace(/[^a-z0-9_-]/g, '_')
    .replace(/_{2,}/g, '_')
    .replace(/^_+|_+$/g, '');
}

/**
 * Generates a sanitized vote document ID.
 * Format: {itemid}_{userid}
 */
function generateVoteId(itemId: string, userId: string): string {
  const sanitizedItemId = sanitizeDocumentId(itemId);
  const sanitizedUserId = sanitizeDocumentId(userId);
  return `${sanitizedItemId}_${sanitizedUserId}`;
}

/**
 * Gets the collection name for the given item type
 */
function getCollectionName(itemType: string): string {
  switch (itemType) {
    case 'PROMO_CODE': return 'promocodes';
    case 'POST': return 'posts';
    case 'COMMENT': return 'comments';
    case 'PROMO': return 'promos';
    default: throw new Error(`Unsupported item type: ${itemType}`);
  }
}

interface VoteRequest {
  itemId: string;
  itemType: 'PROMO_CODE' | 'POST' | 'COMMENT' |  'PROMO';
  voteType: 'UPVOTE' | 'DOWNVOTE' | 'REMOVE';
}

interface VoteResponse {
  success: boolean;
  voteId: string;
  currentVote: 'UPVOTE' | 'DOWNVOTE' | null;
  action: 'added' | 'removed' | 'switched';
  newUpvotes: number;
  newDownvotes: number;
}

/**
 * Callable Function: Handle content votes (PromoCode, Post, Comment, Promo) with proper logic.
 *
 * This function handles all possible vote actions (add, remove, switch)
 * within a single Firestore transaction for data integrity. The logic for
 * updating vote counts is consolidated for a cleaner implementation.
 */
export const handleContentVote = onCall<VoteRequest, Promise<VoteResponse>>(
  async (request) => {
    // 1. Validate authentication
    if (!request.auth) {
      throw new HttpsError('unauthenticated', 'Must be authenticated to vote');
    }

    const { itemId, itemType, voteType } = request.data;
    const userId = request.auth.uid;
    const db = admin.firestore();

    // 2. Validate input
    if (!itemId || typeof itemId !== 'string') {
      throw new HttpsError('invalid-argument', 'Valid itemId is required');
    }

    if (!itemType || typeof itemType !== 'string') {
      throw new HttpsError('invalid-argument', 'Valid itemType is required');
    }

    if (!['PROMO_CODE', 'POST', 'COMMENT', 'PROMO'].includes(itemType)) {
      throw new HttpsError('invalid-argument', 'Invalid itemType');
    }

    if (!voteType || !['UPVOTE', 'DOWNVOTE', 'REMOVE'].includes(voteType)) {
      throw new HttpsError('invalid-argument', 'Valid voteType is required: UPVOTE, DOWNVOTE, or REMOVE');
    }

    const voteId = generateVoteId(itemId, userId);
    const collectionName = getCollectionName(itemType);

    logger.info('Processing vote request', {
      userId,
      itemId,
      itemType,
      voteType,
      voteId
    });

    try {
      // 3. Use transaction for atomic operations
      const result = await db.runTransaction(async (transaction) => {
        const voteRef = db.collection('votes').doc(voteId);
        const contentRef = db.collection(collectionName).doc(itemId);

        // Fetch both documents in a single round-trip for efficiency
        const [voteDoc, contentDoc] = await transaction.getAll(voteRef, contentRef);
        
        if (!contentDoc.exists) {
          throw new HttpsError('not-found', `${itemType} not found`);
        }

        const existingVote = voteDoc.exists ? voteDoc.data() : null;
        const contentData = contentDoc.data()!;
        const currentUpvotes = contentData.upvotes || 0;
        const currentDownvotes = contentData.downvotes || 0;

        let action: 'added' | 'removed' | 'switched';
        let currentVote: 'UPVOTE' | 'DOWNVOTE' | null = null;

        // Handle 3-state voting logic
        if (voteType === 'REMOVE') {
          if (existingVote) {
            action = 'removed';
            transaction.delete(voteRef);
            logger.info('Removing vote', { voteId });
          } else {
            // No vote to remove, return current state
            return {
              success: true,
              voteId,
              currentVote: null,
              action: 'removed' as 'removed',
              newUpvotes: currentUpvotes,
              newDownvotes: currentDownvotes
            };
          }
        } else {
          // UPVOTE or DOWNVOTE
          const isUpvote = voteType === 'UPVOTE';
          currentVote = voteType;
          
          if (!existingVote) {
            action = 'added';
            transaction.set(voteRef, {
              itemId,
              itemType,
              userId,
              isUpvote,
              createdAt: admin.firestore.FieldValue.serverTimestamp(),
              updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            logger.info('Adding new vote', { voteId, voteType });
          } else if (existingVote.isUpvote === isUpvote) {
            // Same vote clicked = remove (toggle off)
            action = 'removed';
            currentVote = null;
            transaction.delete(voteRef);
            logger.info('Toggling off vote', { voteId, voteType });
          } else {
            // Different vote = switch
            action = 'switched';
            transaction.update(voteRef, {
              isUpvote,
              updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            logger.info('Switching vote', { voteId, from: existingVote.isUpvote ? 'UPVOTE' : 'DOWNVOTE', to: voteType });
          }
        }
        
        // 4. Calculate vote count changes based on the action and vote type
        let upvoteChange = 0;
        let downvoteChange = 0;

        if (action === 'added') {
          if (voteType === 'UPVOTE') {
            upvoteChange = 1;
          } else if (voteType === 'DOWNVOTE') {
            downvoteChange = 1;
          }
        } else if (action === 'removed') {
          if (existingVote) {
            if (existingVote.isUpvote) {
              upvoteChange = -1;
            } else {
              downvoteChange = -1;
            }
          }
        } else if (action === 'switched') {
          // Remove old vote and add new vote
          if (existingVote?.isUpvote) {
            upvoteChange = -1;
            downvoteChange = 1;
          } else {
            upvoteChange = 1;
            downvoteChange = -1;
          }
        }

        const newUpvotes = Math.max(0, currentUpvotes + upvoteChange);
        const newDownvotes = Math.max(0, currentDownvotes + downvoteChange);

        // Apply vote count changes in a single operation
        transaction.update(contentRef, {
          upvotes: newUpvotes,
          downvotes: newDownvotes,
          voteScore: newUpvotes - newDownvotes,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // 5. Return the result
        return {
          success: true,
          voteId,
          currentVote,
          action,
          newUpvotes,
          newDownvotes
        };
      });

      logger.info('Vote processed successfully', {
        userId,
        itemId,
        itemType,
        result
      });

      return result;

    } catch (error) {
      logger.error('Error processing vote', {
        userId,
        itemId,
        itemType,
        voteType,
        error: error instanceof Error ? error.message : String(error)
      });

      if (error instanceof HttpsError) {
        throw error;
      }

      throw new HttpsError('internal', 'Failed to process vote');
    }
  }
);