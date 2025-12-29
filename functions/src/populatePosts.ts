import { onCall, HttpsError } from 'firebase-functions/v2/https';
import * as admin from 'firebase-admin';
import * as logger from 'firebase-functions/logger';

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * Callable Function: Populate database with sample posts
 *
 * Security: Requires authentication
 * Usage: Call once to add sample posts for testing
 */
export const populatePosts = onCall(async (request) => {
  if (!request.auth) {
    logger.warn('Unauthenticated populate attempt');
    throw new HttpsError('unauthenticated', 'Must be authenticated');
  }

  logger.info('Starting posts population', { userId: request.auth.uid });

  const authorId = 'STbrUCIexCfig6atxi83QPapX3Y2';
  const authorName = 'aktan';
  const authorAvatarUrl = 'https://lh3.googleusercontent.com/a/ACg8ocKAbCmwm0d8nX0tzVbBLODz-jgEpD4TpLCW9WTKEkqyUb_9XqVi9Q=s96-c';
  const imageUrl = 'https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png';

  try {
    const postRef = db.collection('posts').doc();

    await postRef.set({
      authorId,
      authorName,
      authorAvatarUrl,
      title: 'Posts Feature Coming Soon!',
      content: 'The posts feature is not yet ready. Please wait while we finish development. Thank you for your patience!',
      imageUrls: [imageUrl],
      tags: ['qode'],
      upvotes: 9999,
      downvotes: 0,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    logger.info('Created 1 sample post');

    return {
      success: true,
      message: 'Successfully created 1 sample post',
      created: 1,
    };

  } catch (error) {
    logger.error('Posts population failed', error);
    throw new HttpsError('internal', 'Failed to populate posts');
  }
});