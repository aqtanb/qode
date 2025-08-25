import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  const serviceAccount = require('../../src/serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function updateServicePromoCounts(): Promise<void> {
  console.log('🚀 Starting weekly service promo code count update...');
  
  try {
    // Get all services
    const servicesSnapshot = await db.collection('services').get();
    console.log(`📊 Found ${servicesSnapshot.size} services to update`);
    
    const batch = db.batch();
    const serviceCounts: { name: string; count: number }[] = [];
    
    // Count promo codes for each service
    for (const serviceDoc of servicesSnapshot.docs) {
      const serviceData = serviceDoc.data();
      const serviceName = serviceData.name;
      
      console.log(`📝 Counting promos for: ${serviceName}`);
      
      // Count promocodes for this service
      const promoCodesSnapshot = await db.collection('promocodes')
        .where('serviceName', '==', serviceName)
        .get();
      
      const promoCount = promoCodesSnapshot.size;
      const currentCount = serviceData.promoCodeCount || 0;
      
      serviceCounts.push({ name: serviceName, count: promoCount });
      
      // Update the count (even if unchanged, to update timestamp)
      batch.update(serviceDoc.ref, {
        promoCodeCount: promoCount,
        countsUpdatedAt: admin.firestore.Timestamp.now()
      });
      
      if (promoCount !== currentCount) {
        console.log(`  → ${serviceName}: ${currentCount} → ${promoCount} codes`);
      } else {
        console.log(`  → ${serviceName}: ${promoCount} codes (unchanged)`);
      }
    }
    
    await batch.commit();
    
    // Sort and show rankings
    serviceCounts.sort((a, b) => b.count - a.count);
    
    console.log('\n📈 Top services by promo code count:');
    serviceCounts.slice(0, 20).forEach((service, index) => {
      console.log(`  ${index + 1}. ${service.name}: ${service.count} codes`);
    });
    
    const totalPromos = serviceCounts.reduce((sum, s) => sum + s.count, 0);
    const servicesWithPromos = serviceCounts.filter(s => s.count > 0).length;
    
    console.log(`\n✅ Service counts update completed!`);
    console.log(`📊 Total services: ${serviceCounts.length}`);
    console.log(`🔥 Services with promos: ${servicesWithPromos}`);
    console.log(`📈 Total promo codes: ${totalPromos}`);
    
  } catch (error) {
    console.error('❌ Error updating service counts:', error);
    throw error;
  }
}

// Export for Cloud Functions or other scripts
export { updateServicePromoCounts };

// Run if called directly
if (require.main === module) {
  updateServicePromoCounts()
    .then(() => process.exit(0))
    .catch((error) => {
      console.error('❌ Error:', error);
      process.exit(1);
    });
}