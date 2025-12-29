import { onCall, HttpsError } from 'firebase-functions/v2/https';
import * as admin from 'firebase-admin';
import * as logger from 'firebase-functions/logger';

// Guard Admin SDK initialization
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * Callable Function: Delete user account and all associated data
 *
 * Security: Requires authentication - only users can delete their own account
 *
 * Deletes:
 * 1. All posts by user from posts collection
 * 2. All promocodes by user from promocodes collection
 * 3. All user_interactions by user from user_interactions collection
 * 4. All reports by user from reports collection
 * 5. Blocks subcollection (users/{userId}/blocks)
 * 6. User document from users collection
 * 7. Firebase Auth account
 *
 * @returns {{ success: boolean, deletedItems: object }} Deletion results
 */
export const deleteUserAccount = onCall(async (request) => {
  // Security: Check authentication
  if (!request.auth) {
    logger.warn('Unauthenticated deletion attempt');
    throw new HttpsError('unauthenticated', 'Must be authenticated');
  }

  const userId = request.auth.uid;
  logger.info('Starting account deletion', { userId });

  const deletionResults = {
    userDoc: false,
    posts: 0,
    promocodes: 0,
    interactions: 0,
    reports: 0,
    blocks: 0,
    authAccount: false,
  };

  try {
    // 1. Delete posts
    const postsSnapshot = await db.collection('posts')
      .where('authorId', '==', userId)
      .get();

    if (!postsSnapshot.empty) {
      const postBatch = db.batch();
      postsSnapshot.forEach(doc => postBatch.delete(doc.ref));
      await postBatch.commit();
      deletionResults.posts = postsSnapshot.size;
      logger.info(`Deleted ${postsSnapshot.size} posts`, { userId });
    }

    // 2. Delete promocodes
    const promocodesSnapshot = await db.collection('promocodes')
      .where('authorId', '==', userId)
      .get();

    if (!promocodesSnapshot.empty) {
      const promoBatch = db.batch();
      promocodesSnapshot.forEach(doc => promoBatch.delete(doc.ref));
      await promoBatch.commit();
      deletionResults.promocodes = promocodesSnapshot.size;
      logger.info(`Deleted ${promocodesSnapshot.size} promocodes`, { userId });
    }

    // 3. Delete user_interactions
    const interactionsSnapshot = await db.collection('user_interactions')
      .where('userId', '==', userId)
      .get();

    if (!interactionsSnapshot.empty) {
      const interactionBatch = db.batch();
      interactionsSnapshot.forEach(doc => interactionBatch.delete(doc.ref));
      await interactionBatch.commit();
      deletionResults.interactions = interactionsSnapshot.size;
      logger.info(`Deleted ${interactionsSnapshot.size} interactions`, { userId });
    }

    // 4. Delete reports submitted by user
    const reportsSnapshot = await db.collection('reports')
      .where('reporterId', '==', userId)
      .get();

    if (!reportsSnapshot.empty) {
      const reportBatch = db.batch();
      reportsSnapshot.forEach(doc => reportBatch.delete(doc.ref));
      await reportBatch.commit();
      deletionResults.reports = reportsSnapshot.size;
      logger.info(`Deleted ${reportsSnapshot.size} reports`, { userId });
    }

    // 5. Delete blocks subcollection
    const blocksSnapshot = await db.collection('users')
      .doc(userId)
      .collection('blocks')
      .get();

    if (!blocksSnapshot.empty) {
      const blockBatch = db.batch();
      blocksSnapshot.forEach(doc => blockBatch.delete(doc.ref));
      await blockBatch.commit();
      deletionResults.blocks = blocksSnapshot.size;
      logger.info(`Deleted ${blocksSnapshot.size} blocks`, { userId });
    }

    // 6. Delete user document
    await db.collection('users').doc(userId).delete();
    deletionResults.userDoc = true;
    logger.info('Deleted user document', { userId });

    // 7. Delete Auth account LAST (point of no return)
    await admin.auth().deleteUser(userId);
    deletionResults.authAccount = true;
    logger.info('Account deletion completed', { userId, deletionResults });

    return {
      success: true,
      deletedItems: deletionResults,
    };

  } catch (error) {
    logger.error('Account deletion failed', { userId, error, partialResults: deletionResults });
    throw new HttpsError(
      'internal',
      'Account deletion failed',
      { partialDeletion: deletionResults }
    );
  }
});