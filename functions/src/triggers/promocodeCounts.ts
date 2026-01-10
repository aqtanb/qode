import { onDocumentCreated, onDocumentDeleted, onDocumentUpdated } from 'firebase-functions/v2/firestore';
import * as admin from 'firebase-admin';
import * as logger from 'firebase-functions/logger';

// Ensure Admin SDK is initialized when this module is loaded directly.
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const PROMO_COLLECTION = 'promocodes';
const SERVICE_COLLECTION = 'services';

async function incrementServiceCount(serviceId: string, delta: number) {
  try {
    await db.collection(SERVICE_COLLECTION).doc(serviceId).update({
      promoCodeCount: admin.firestore.FieldValue.increment(delta),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  } catch (error) {
    logger.error(`Failed to increment promoCodeCount for service ${serviceId} by ${delta}`, error);
  }
}

export const onPromoCreated = onDocumentCreated(`${PROMO_COLLECTION}/{promoId}`, async (event) => {
  const data = event.data?.data();
  const serviceId = data?.service?.id as string | undefined;
  if (!serviceId) return;
  await incrementServiceCount(serviceId, 1);
});

export const onPromoDeleted = onDocumentDeleted(`${PROMO_COLLECTION}/{promoId}`, async (event) => {
  const data = event.data?.data();
  const serviceId = data?.service?.id as string | undefined;
  if (!serviceId) return;
  await incrementServiceCount(serviceId, -1);
});

export const onPromoUpdated = onDocumentUpdated(`${PROMO_COLLECTION}/{promoId}`, async (event) => {
  const before = event.data?.before.data();
  const after = event.data?.after.data();
  const beforeService = before?.service?.id as string | undefined;
  const afterService = after?.service?.id as string | undefined;

  if (!beforeService && !afterService) return;
  if (beforeService === afterService) return;

  if (beforeService) {
    await incrementServiceCount(beforeService, -1);
  }
  if (afterService) {
    await incrementServiceCount(afterService, 1);
  }
});
