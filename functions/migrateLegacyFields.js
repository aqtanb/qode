const admin = require('firebase-admin');

// Initialize Firebase Admin with service account
const serviceAccount = require('./src/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function removeLegacyUserFields() {
  console.log('Starting legacy user fields removal...');

  try {
    // Get all promocodes
    const promoCodesSnapshot = await db.collection('promocodes').get();

    if (promoCodesSnapshot.empty) {
      console.log('No promocodes found');
      return;
    }

    console.log(`Found ${promoCodesSnapshot.size} promocodes to check`);

    // Process in batches (Firestore batch limit is 500 operations)
    const batch = db.batch();
    let updateCount = 0;

    promoCodesSnapshot.forEach((doc) => {
      const data = doc.data();

      // Check if document has any of the legacy fields
      const hasLegacyFields =
        data.hasOwnProperty('isUpvotedByCurrentUser') ||
        data.hasOwnProperty('isDownvotedByCurrentUser') ||
        data.hasOwnProperty('isBookmarkedByCurrentUser') ||
        data.hasOwnProperty('title');

      if (hasLegacyFields) {
        console.log(`Cleaning up document: ${doc.id}`);

        // Use FieldValue.delete() to remove the fields
        batch.update(doc.ref, {
          isUpvotedByCurrentUser: admin.firestore.FieldValue.delete(),
          isDownvotedByCurrentUser: admin.firestore.FieldValue.delete(),
          isBookmarkedByCurrentUser: admin.firestore.FieldValue.delete(),
          title: admin.firestore.FieldValue.delete(),
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        updateCount++;
      }
    });

    if (updateCount > 0) {
      console.log(`Committing batch update for ${updateCount} documents...`);
      await batch.commit();
      console.log(`✅ Successfully removed legacy fields from ${updateCount} promocodes`);
    } else {
      console.log('No documents needed updating');
    }

    console.log({
      success: true,
      message: `Cleaned up legacy user fields from ${updateCount} promocodes`,
      updated: updateCount,
      total: promoCodesSnapshot.size
    });

  } catch (error) {
    console.error('❌ Error removing legacy user fields:', error);
    throw error;
  }
}

// Run the migration
removeLegacyUserFields()
  .then(() => {
    console.log('Migration completed successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Migration failed:', error);
    process.exit(1);
  });
