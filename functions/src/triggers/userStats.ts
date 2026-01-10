import { onDocumentCreated, onDocumentDeleted, onDocumentUpdated } from 'firebase-functions/v2/firestore';
import * as admin from 'firebase-admin';
import * as logger from 'firebase-functions/logger';

// Ensure Admin SDK is initialized when this module is loaded directly.
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const USER_COLLECTION = 'users';
const PROMO_COLLECTION = 'promocodes';
const POST_COLLECTION = 'posts';
const SUBMITTED_PROMOCODES_FIELD = 'stats.submittedPromocodesCount';
const SUBMITTED_POSTS_FIELD = 'stats.submittedPostsCount';

async function incrementUserStat(userId: string, field: string, delta: number, context: Record<string, unknown>) {
  try {
    await db.collection(USER_COLLECTION).doc(userId).update({
      [field]: admin.firestore.FieldValue.increment(delta),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } catch (error) {
    logger.error(`Failed to update user stat ${field} for user ${userId} by ${delta}`, {
      ...context,
      error,
    });
  }
}

export const onPromocodeCreatedUpdateUserStats = onDocumentCreated(
  `${PROMO_COLLECTION}/{promoId}`,
  async (event) => {
    const data = event.data?.data();
    const authorId = data?.author?.id as string | undefined;
    if (!authorId) return;

    await incrementUserStat(authorId, SUBMITTED_PROMOCODES_FIELD, 1, {
      trigger: 'promocodeCreated',
      promoId: event.params.promoId,
    });
  }
);

export const onPromocodeDeletedUpdateUserStats = onDocumentDeleted(
  `${PROMO_COLLECTION}/{promoId}`,
  async (event) => {
    const data = event.data?.data();
    const authorId = data?.author?.id as string | undefined;
    if (!authorId) return;

    await incrementUserStat(authorId, SUBMITTED_PROMOCODES_FIELD, -1, {
      trigger: 'promocodeDeleted',
      promoId: event.params.promoId,
    });
  }
);

export const onPromocodeUpdatedUpdateUserStats = onDocumentUpdated(
  `${PROMO_COLLECTION}/{promoId}`,
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    const beforeAuthorId = before?.author?.id as string | undefined;
    const afterAuthorId = after?.author?.id as string | undefined;

    if (!beforeAuthorId && !afterAuthorId) return;
    if (beforeAuthorId === afterAuthorId) return;

    if (beforeAuthorId) {
      await incrementUserStat(beforeAuthorId, SUBMITTED_PROMOCODES_FIELD, -1, {
        trigger: 'promocodeAuthorChanged',
        promoId: event.params.promoId,
      });
    }
    if (afterAuthorId) {
      await incrementUserStat(afterAuthorId, SUBMITTED_PROMOCODES_FIELD, 1, {
        trigger: 'promocodeAuthorChanged',
        promoId: event.params.promoId,
      });
    }
  }
);

export const onPostCreatedUpdateUserStats = onDocumentCreated(
  `${POST_COLLECTION}/{postId}`,
  async (event) => {
    const data = event.data?.data();
    const authorId = data?.authorId as string | undefined;
    if (!authorId) return;

    await incrementUserStat(authorId, SUBMITTED_POSTS_FIELD, 1, {
      trigger: 'postCreated',
      postId: event.params.postId,
    });
  }
);

export const onPostDeletedUpdateUserStats = onDocumentDeleted(
  `${POST_COLLECTION}/{postId}`,
  async (event) => {
    const data = event.data?.data();
    const authorId = data?.authorId as string | undefined;
    if (!authorId) return;

    await incrementUserStat(authorId, SUBMITTED_POSTS_FIELD, -1, {
      trigger: 'postDeleted',
      postId: event.params.postId,
    });
  }
);

export const onPostUpdatedUpdateUserStats = onDocumentUpdated(
  `${POST_COLLECTION}/{postId}`,
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    const beforeAuthorId = before?.authorId as string | undefined;
    const afterAuthorId = after?.authorId as string | undefined;

    if (!beforeAuthorId && !afterAuthorId) return;
    if (beforeAuthorId === afterAuthorId) return;

    if (beforeAuthorId) {
      await incrementUserStat(beforeAuthorId, SUBMITTED_POSTS_FIELD, -1, {
        trigger: 'postAuthorChanged',
        postId: event.params.postId,
      });
    }
    if (afterAuthorId) {
      await incrementUserStat(afterAuthorId, SUBMITTED_POSTS_FIELD, 1, {
        trigger: 'postAuthorChanged',
        postId: event.params.postId,
      });
    }
  }
);
