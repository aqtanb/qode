import * as admin from 'firebase-admin';

// Initialize Firebase Admin with your service account
admin.initializeApp({
  credential: admin.credential.cert(require('../src/serviceAccountKey.json'))
});

const db = admin.firestore();

async function initializeVoteScores() {
  console.log('🚀 Starting voteScore initialization...');
  
  try {
    // Get all promo codes
    const promoCodesSnapshot = await db.collection('promocodes').get();
    
    if (promoCodesSnapshot.empty) {
      console.log('❌ No promo codes found in Firestore');
      return;
    }
    
    console.log(`📊 Found ${promoCodesSnapshot.size} promo codes`);
    
    // Process in batches (Firestore batch limit is 500 operations)
    const batch = db.batch();
    let updateCount = 0;
    
    promoCodesSnapshot.forEach((doc) => {
      const data = doc.data();
      const upvotes = data.upvotes || 0;
      const downvotes = data.downvotes || 0;
      const currentVoteScore = data.voteScore;
      const calculatedVoteScore = upvotes - downvotes;
      
      console.log(`📝 ${doc.id}: upvotes=${upvotes}, downvotes=${downvotes}, current=${currentVoteScore}, calculated=${calculatedVoteScore}`);
      
      // Only update if voteScore is missing or incorrect
      if (currentVoteScore === undefined || currentVoteScore !== calculatedVoteScore) {
        batch.update(doc.ref, {
          voteScore: calculatedVoteScore,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        updateCount++;
        console.log(`✅ Will update ${doc.id} with voteScore=${calculatedVoteScore}`);
      } else {
        console.log(`⏭️ Skipping ${doc.id} (already has correct voteScore)`);
      }
    });
    
    if (updateCount > 0) {
      console.log(`📤 Committing batch update for ${updateCount} documents...`);
      await batch.commit();
      console.log(`✨ Successfully updated ${updateCount} promo codes with voteScore`);
    } else {
      console.log('✅ All promo codes already have correct voteScore values');
    }
    
    console.log(`🎉 Initialization complete! Updated ${updateCount}/${promoCodesSnapshot.size} promo codes`);
    
  } catch (error) {
    console.error('💥 Error initializing voteScores:', error);
    throw error;
  }
}

// Run the initialization
initializeVoteScores()
  .then(() => {
    console.log('✅ Done!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Failed:', error);
    process.exit(1);
  });