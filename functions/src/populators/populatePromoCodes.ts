import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  const serviceAccount = require('../serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

// Enhanced sample data based on your Kazakhstan market
const sampleCodes = [
  "SAVE10", "DISCOUNT20", "OFFER30", "PROMO50", "DEAL15", "COUPON25", "FLASH40", "SALE60", "BONUS5", "EXTRA70",
  "WELCOME10", "FIRST20", "SUMMER30", "WINTER40", "SPRING50", "FALL60", "HOLIDAY70", "BLACKFRIDAY80", "CYBERMONDAY90", "NEWYEAR100",
  "ALMATY10", "ASTANA20", "KAZAKHSTAN30", "TENGE40", "QAZAQ50", "BAITEREK60"
];

// Kazakhstan-focused service names from your existing services
const kazakhServiceNames = [
  "Яндекс Лавка", "Dodo", "chocofood", "arbuz", "abr", "Sulpak", "Technodom", "anytime",
  "Halyk Market", "ForteMarket", "Naimi.kz", "izi", "Золотое Яблоко", "Letoile", 
  "Sokolov", "iHerb", "Яндекс Практикум", "Яндекс Плюс", "КиноПоиск", "Teez",
  "clever market", "flowwow", "Freedom Travel"
];

const kazakhCategories = [
  "Food", "Electronics", "Beauty", "Jewelry", "Health", "Transport", "Education", 
  "Entertainment", "Marketplace", "Services", "Telecom"
];

const sampleTitles = [
  "Жаңа қолданушыларға арнайы жеңілдік", "Астанада тегін жеткізу", "Алматыда арнайы ұсыныс",
  "Special Discount for Kazakhstan", "Limited Time Offer", "Flash Sale", "Exclusive Deal", 
  "Welcome Bonus", "Seasonal Savings", "Holiday Special", "New User Promo", "Loyalty Reward", 
  "Clearance Sale", "Free Shipping", "Extra Savings", "Weekend Deal", "Midweek Madness", 
  "Казахстанда арнайы баға", "Тегін жеткізу", "Жаз маусымының жеңілдігі"
];

const sampleDescriptions = [
  "Қазақстанда ең жақсы бағалар", "Сүйікті тауарларыңызға керемет жеңілдіктер алыңыз",
  "Get amazing discounts on your favorite items in Kazakhstan", "Save big on selected products",
  "Limited stock available in Almaty and Astana", "Exclusive offer for first-time users in Kazakhstan",
  "Enjoy extra savings this season", "Special deal just for Kazakhstani customers",
  "Don't miss out on this opportunity", "Best prices guaranteed in Kazakhstan",
  "Астана мен Алматыда жеткізу тегін", "Қазақстанның барлық қалаларына жеткізу"
];

const kazakhTargetCountries = [["KZ"]];

interface PromoCodeData {
  code: string;
  serviceName: string;
  category: string;
  title: string;
  description: string;
  type: string;
  discountPercentage?: number;
  discountAmount?: number;
  minimumOrderAmount: number;
  isFirstUserOnly: boolean;
  upvotes: number;
  downvotes: number;
  voteScore: number; // 🆕 Pre-calculated voteScore!
  views: number;
  shares: number;
  screenshotUrl?: string;
  targetCountries: string[];
  isVerified: boolean;
  startDate: admin.firestore.Timestamp;
  endDate: admin.firestore.Timestamp;
  createdAt: admin.firestore.FieldValue;
  createdBy: string | null;
  isUpvotedByCurrentUser: boolean;
  isDownvotedByCurrentUser: boolean;
  isBookmarkedByCurrentUser: boolean;
}

function createSamplePromoCode(): PromoCodeData {
  const randomCode = sampleCodes[Math.floor(Math.random() * sampleCodes.length)];
  const randomServiceName = kazakhServiceNames[Math.floor(Math.random() * kazakhServiceNames.length)];
  const randomCategory = kazakhCategories[Math.floor(Math.random() * kazakhCategories.length)];
  const randomTitle = sampleTitles[Math.floor(Math.random() * sampleTitles.length)];
  const randomDescription = sampleDescriptions[Math.floor(Math.random() * sampleDescriptions.length)];

  const type = Math.random() < 0.7 ? "percentage" : "fixed"; // More percentage discounts
  const discountPercentage = type === "percentage" ? Math.floor(Math.random() * 50) + 10 : undefined; // 10-60%
  const discountAmount = type === "fixed" ? Math.floor(Math.random() * 5000) + 500 : undefined; // 500-5500 KZT
  const minimumOrderAmount = Math.floor(Math.random() * 10000) + 1000; // 1000-11000 KZT
  const isFirstUserOnly = Math.random() < 0.3;
  const isVerified = Math.random() < 0.4; // 40% verified (more realistic)

  // More realistic vote distribution
  const upvotes = Math.floor(Math.random() * 500) + 1; // 1-500 upvotes
  const downvotes = Math.floor(Math.random() * 50); // 0-49 downvotes
  const voteScore = upvotes - downvotes; // 🆕 Pre-calculate voteScore!

  // Random dates with Kazakhstan timezone consideration
  const now = new Date();
  const startOffset = Math.floor(Math.random() * 30) * 86400000; // Past 30 days
  const endOffset = (Math.floor(Math.random() * 90) + 7) * 86400000; // Future 7-97 days
  const startDate = admin.firestore.Timestamp.fromDate(new Date(now.getTime() - startOffset));
  const endDate = admin.firestore.Timestamp.fromDate(new Date(now.getTime() + endOffset));

  return {
    code: randomCode,
    serviceName: randomServiceName,
    category: randomCategory,
    title: randomTitle,
    description: randomDescription,
    type: type,
    discountPercentage: discountPercentage,
    discountAmount: discountAmount,
    minimumOrderAmount: minimumOrderAmount,
    isFirstUserOnly: isFirstUserOnly,
    upvotes: upvotes,
    downvotes: downvotes,
    voteScore: voteScore, // 🆕 Store computed voteScore!
    views: Math.floor(Math.random() * 1000), // Random view count
    shares: Math.floor(Math.random() * 50), // Random share count
    screenshotUrl: Math.random() < 0.3 ? `https://res.cloudinary.com/demo/image/upload/sample_${Math.floor(Math.random() * 5) + 1}.jpg` : undefined,
    targetCountries: kazakhTargetCountries[0],
    isVerified: isVerified,
    startDate: startDate,
    endDate: endDate,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    createdBy: null, // Set to actual user ID if needed
    isUpvotedByCurrentUser: false,
    isDownvotedByCurrentUser: false,
    isBookmarkedByCurrentUser: false
  };
}

async function populatePromoCodes(count: number = 100): Promise<void> {
  console.log(`🚀 Starting promo codes population (${count} codes)...`);

  const collectionRef = db.collection("promocodes");
  
  // Process in batches of 500 (Firestore batch limit)
  const batchSize = 500;
  const batches = Math.ceil(count / batchSize);
  
  for (let batchIndex = 0; batchIndex < batches; batchIndex++) {
    const batch = db.batch();
    const startIndex = batchIndex * batchSize;
    const endIndex = Math.min(startIndex + batchSize, count);
    const currentBatchSize = endIndex - startIndex;
    
    console.log(`📝 Processing batch ${batchIndex + 1}/${batches} (${currentBatchSize} codes)...`);
    
    for (let i = 0; i < currentBatchSize; i++) {
      const promoCodeData = createSamplePromoCode();
      
      // Create composite document ID: SERVICENAME_CODE (your pattern)
      const docId = `${promoCodeData.serviceName.toUpperCase().replace(/\s+/g, '_')}_${promoCodeData.code}`;
      const docRef = collectionRef.doc(docId);
      
      batch.set(docRef, promoCodeData);
    }

    await batch.commit();
    console.log(`✅ Batch ${batchIndex + 1} committed successfully!`);
  }
  
  console.log(`🎉 Successfully populated ${count} promo codes!`);
  console.log('💡 Run the initializeVoteScores Cloud Function to ensure all voteScores are correct.');
}

// Run if called directly
if (require.main === module) {
  const count = parseInt(process.argv[2]) || 100;
  populatePromoCodes(count)
    .then(() => process.exit(0))
    .catch((error) => {
      console.error('❌ Error populating promo codes:', error);
      process.exit(1);
    });
}

export { populatePromoCodes };