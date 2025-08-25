import { onDocumentUpdated } from 'firebase-functions/v2/firestore';
import { onCall } from 'firebase-functions/v2/https';
import * as logger from 'firebase-functions/logger';
import * as admin from 'firebase-admin';

// Initialize Firebase Admin
admin.initializeApp();
const db = admin.firestore();

/**
 * Cloud Function: Update voteScore when promo code votes change
 * Triggers: When any field in promocodes/{promoId} is updated
 * Purpose: Maintain computed voteScore field for efficient sorting
 */
export const updateVoteScore = onDocumentUpdated('promocodes/{promoId}', async (event) => {
  const change = event.data;
  const promoId = event.params.promoId;
  
  if (!change) {
    logger.warn('No change data available');
    return;
  }
  
  const before = change.before.data();
  const after = change.after.data();
  
  // Only process if vote counts changed
  const upvotesChanged = before?.upvotes !== after?.upvotes;
  const downvotesChanged = before?.downvotes !== after?.downvotes;
  
  if (upvotesChanged || downvotesChanged) {
    const upvotes = after?.upvotes || 0;
    const downvotes = after?.downvotes || 0;
    const newVoteScore = upvotes - downvotes;
    const currentVoteScore = after?.voteScore || 0;
    
    // Only update if voteScore actually changed (prevent infinite loops)
    if (currentVoteScore !== newVoteScore) {
      logger.info(
        `Updating voteScore for ${promoId}: ${currentVoteScore} -> ${newVoteScore}`,
        { promoId, upvotes, downvotes, newVoteScore }
      );
      
      try {
        await change.after.ref.update({
          voteScore: newVoteScore,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        
        logger.info(`Successfully updated voteScore for ${promoId}`);
      } catch (error) {
        logger.error(`Error updating voteScore for ${promoId}:`, error);
      }
    }
  }
});

/**
 * Callable Function: Initialize voteScore for existing promo codes
 * Usage: Call once to migrate existing data
 * Security: Requires authentication
 */
export const initializeVoteScores = onCall(async (request) => {
  // Security: Require authentication
  if (!request.auth) {
    throw new Error('Must be authenticated to run data migration');
  }

  logger.info('Starting voteScore initialization', { userId: request.auth.uid });
  
  try {
    // Get all promo codes
    const promoCodesSnapshot = await db.collection('promocodes').get();
    
    if (promoCodesSnapshot.empty) {
      return { 
        success: true, 
        message: 'No promo codes found',
        updated: 0 
      };
    }
    
    // Process in batches (Firestore batch limit is 500 operations)
    const batch = db.batch();
    let updateCount = 0;
    
    promoCodesSnapshot.forEach((doc) => {
      const data = doc.data();
      const upvotes = data.upvotes || 0;
      const downvotes = data.downvotes || 0;
      const currentVoteScore = data.voteScore;
      const calculatedVoteScore = upvotes - downvotes;
      
      // Only update if voteScore is missing or incorrect
      if (currentVoteScore === undefined || currentVoteScore !== calculatedVoteScore) {
        batch.update(doc.ref, {
          voteScore: calculatedVoteScore,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        updateCount++;
      }
    });
    
    if (updateCount > 0) {
      await batch.commit();
      logger.info(`Updated ${updateCount} promo codes with voteScore`);
    }
    
    return {
      success: true,
      message: `Initialized voteScore for ${updateCount} promo codes`,
      updated: updateCount,
      total: promoCodesSnapshot.size
    };
    
  } catch (error) {
    logger.error('Error initializing voteScores:', error);
    throw new Error('Failed to initialize vote scores');
  }
});

/**
 * Callable Function: Update service promo code counts
 * Purpose: Maintain denormalized counter for services
 */
export const updateServicePromoCounts = onCall(async (request) => {
  if (!request.auth) {
    throw new Error('Authentication required');
  }

  try {
    const servicesSnapshot = await db.collection('services').get();
    const batch = db.batch();
    let updateCount = 0;

    for (const serviceDoc of servicesSnapshot.docs) {
      const serviceName = serviceDoc.data().name;
      
      // Count promo codes for this service
      const promoCodesSnapshot = await db.collection('promocodes')
        .where('serviceName', '==', serviceName)
        .get();
      
      const currentCount = serviceDoc.data().promoCodeCount || 0;
      const actualCount = promoCodesSnapshot.size;
      
      if (currentCount !== actualCount) {
        batch.update(serviceDoc.ref, {
          promoCodeCount: actualCount,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        updateCount++;
      }
    }

    if (updateCount > 0) {
      await batch.commit();
    }

    return {
      success: true,
      message: `Updated ${updateCount} services`,
      updated: updateCount
    };

  } catch (error) {
    logger.error('Error updating service counts:', error);
    throw new Error('Failed to update service counts');
  }
});