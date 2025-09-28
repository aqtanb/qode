import { onDocumentWritten } from 'firebase-functions/v2/firestore';
import * as logger from 'firebase-functions/logger';
import * as admin from 'firebase-admin';

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

/**
 * Calculate vote count deltas based on vote state transition
 */
function calculateVoteDeltas(
  oldVoteState: string | null,
  newVoteState: string | null
): { upvoteDelta: number; downvoteDelta: number } {
  const oldState = oldVoteState || 'NONE';
  const newState = newVoteState || 'NONE';

  let upvoteDelta = 0;
  let downvoteDelta = 0;

  // Remove old vote
  if (oldState === 'UPVOTE') {
    upvoteDelta -= 1;
  } else if (oldState === 'DOWNVOTE') {
    downvoteDelta -= 1;
  }

  // Add new vote
  if (newState === 'UPVOTE') {
    upvoteDelta += 1;
  } else if (newState === 'DOWNVOTE') {
    downvoteDelta += 1;
  }

  return { upvoteDelta, downvoteDelta };
}

/**
 * Cloud Function: Update content vote counts when user interactions change
 * Triggers: When any document in user_interactions/{interactionId} is written (created/updated/deleted)
 * Purpose: Maintain accurate vote counts on content based on unified interaction system
 */
export const updateContentVotesFromInteractions = onDocumentWritten(
  'user_interactions/{interactionId}',
  async (event) => {
    const change = event.data;
    const interactionId = event.params.interactionId;

    if (!change) {
      logger.warn('No change data available for interaction', { interactionId });
      return;
    }

    const db = admin.firestore();
    const beforeData = change.before?.exists ? change.before.data() : null;
    const afterData = change.after?.exists ? change.after.data() : null;

    // Extract interaction details
    let itemId: string;
    let itemType: string;

    if (afterData) {
      itemId = afterData.itemId;
      itemType = afterData.itemType;
    } else if (beforeData) {
      itemId = beforeData.itemId;
      itemType = beforeData.itemType;
    } else {
      logger.warn('No interaction data found in before or after', { interactionId });
      return;
    }

    // Validate required fields
    if (!itemId || !itemType) {
      logger.warn('Missing required fields in interaction data', {
        interactionId,
        itemId,
        itemType
      });
      return;
    }

    try {
      const collectionName = getCollectionName(itemType);

      // Get vote state changes
      const oldVoteState = beforeData?.voteState || null;
      const newVoteState = afterData?.voteState || null;

      // Skip if vote state hasn't changed
      if (oldVoteState === newVoteState) {
        logger.debug('Vote state unchanged, skipping update', {
          interactionId,
          itemId,
          voteState: newVoteState
        });
        return;
      }

      // Calculate vote deltas
      const { upvoteDelta, downvoteDelta } = calculateVoteDeltas(oldVoteState, newVoteState);

      // Skip if no vote changes
      if (upvoteDelta === 0 && downvoteDelta === 0) {
        logger.debug('No vote deltas to apply', {
          interactionId,
          itemId,
          oldVoteState,
          newVoteState
        });
        return;
      }

      logger.info('Updating content vote counts', {
        interactionId,
        itemId,
        itemType,
        oldVoteState,
        newVoteState,
        upvoteDelta,
        downvoteDelta
      });

      // Use transaction to atomically update vote counts
      await db.runTransaction(async (transaction) => {
        const contentRef = db.collection(collectionName).doc(itemId);
        const contentDoc = await transaction.get(contentRef);

        if (!contentDoc.exists) {
          throw new Error(`Content not found: ${itemType} ${itemId}`);
        }

        const contentData = contentDoc.data()!;
        const currentUpvotes = contentData.upvotes || 0;
        const currentDownvotes = contentData.downvotes || 0;

        // Calculate new vote counts (ensure non-negative)
        const newUpvotes = Math.max(0, currentUpvotes + upvoteDelta);
        const newDownvotes = Math.max(0, currentDownvotes + downvoteDelta);
        const newVoteScore = newUpvotes - newDownvotes;

        // Update content with new vote counts
        transaction.update(contentRef, {
          upvotes: newUpvotes,
          downvotes: newDownvotes,
          voteScore: newVoteScore,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        logger.info('Content vote counts updated successfully', {
          interactionId,
          itemId,
          itemType,
          oldUpvotes: currentUpvotes,
          newUpvotes,
          oldDownvotes: currentDownvotes,
          newDownvotes,
          newVoteScore
        });
      });

    } catch (error) {
      logger.error('Error updating content vote counts', {
        interactionId,
        itemId,
        itemType,
        error: error instanceof Error ? error.message : String(error)
      });

      // Don't throw error to avoid retries - log and continue
      // This prevents infinite retry loops for permanent failures
    }
  }
);