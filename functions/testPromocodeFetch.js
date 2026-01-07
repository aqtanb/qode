const admin = require('firebase-admin');

// Initialize Firebase Admin with service account (reuse existing instance if available)
if (!admin.apps.length) {
  const serviceAccount = require('./src/serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function testPromocodeFetch() {
  console.log('Testing promocode fetching after migration...');

  try {
    // Try to fetch one specific promocode
    const testPromoId = 'AIRBNB_SALE60'; // One from the migration logs
    console.log(`\nFetching promocode: ${testPromoId}`);

    const doc = await db.collection('promocodes').doc(testPromoId).get();

    if (!doc.exists) {
      console.log(`❌ Document ${testPromoId} does not exist`);
      return;
    }

    console.log(`✅ Document ${testPromoId} exists`);

    // Get the raw data to see what fields exist
    const data = doc.data();
    console.log('\n📋 Document fields:');
    Object.keys(data).forEach(key => {
      console.log(`  - ${key}: ${typeof data[key]}`);
    });

    // Check for any remaining legacy fields
    const legacyFields = ['isUpvotedByCurrentUser', 'isDownvotedByCurrentUser', 'isBookmarkedByCurrentUser', 'title'];
    const foundLegacyFields = legacyFields.filter(field => data.hasOwnProperty(field));

    if (foundLegacyFields.length > 0) {
      console.log(`\n⚠️  Still has legacy fields: ${foundLegacyFields.join(', ')}`);
    } else {
      console.log('\n✅ No legacy fields found');
    }

    // Check required fields for mapping
    const requiredFields = ['code', 'serviceName', 'type'];
    const missingFields = requiredFields.filter(field => !data.hasOwnProperty(field) || !data[field]);

    if (missingFields.length > 0) {
      console.log(`\n❌ Missing required fields: ${missingFields.join(', ')}`);
    } else {
      console.log('\n✅ All required fields present');
    }

    console.log('\n📊 Sample data:');
    console.log(JSON.stringify({
      code: data.code,
      serviceName: data.serviceName,
      type: data.type,
      discountPercentage: data.discountPercentage,
      discountAmount: data.discountAmount
    }, null, 2));

  } catch (error) {
    console.error('❌ Error testing promocode fetch:', error);
  }
}

// Run the test
testPromocodeFetch()
  .then(() => {
    console.log('\n✅ Test completed!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Test failed:', error);
    process.exit(1);
  });
