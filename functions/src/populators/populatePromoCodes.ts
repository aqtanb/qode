import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  const serviceAccount = require('../../src/serviceAccountKey.json');
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

// Service definitions with proper category linking
const serviceDefinitions = [
  // Food
  { name: "Яндекс Лавка", category: "Food" },
  { name: "Dodo", category: "Food" },
  { name: "chocofood", category: "Food" },
  { name: "arbuz", category: "Food" },
  { name: "abr", category: "Food" },

  // Beauty
  { name: "Золотое Яблоко", category: "Beauty" },
  { name: "Letoile", category: "Beauty" },

  // Electronics
  { name: "Sulpak", category: "Electronics" },
  { name: "Technodom", category: "Electronics" },

  // Jewelry
  { name: "Sokolov", category: "Jewelry" },

  // Health
  { name: "iHerb", category: "Health" },

  // Transport
  { name: "anytime", category: "Transport" },

  // Education
  { name: "Яндекс Практикум", category: "Education" },

  // Entertainment/Streaming
  { name: "Яндекс Плюс", category: "Entertainment" },
  { name: "КиноПоиск", category: "Entertainment" },
  { name: "YouTube Premium", category: "Streaming" },
  { name: "Netflix", category: "Streaming" },
  { name: "Spotify", category: "Music" },

  // Marketplace
  { name: "Teez", category: "Marketplace" },
  { name: "Halyk Market", category: "Marketplace" },
  { name: "clever market", category: "Marketplace" },
  { name: "ForteMarket", category: "Marketplace" },
  { name: "flowwow", category: "Marketplace" },

  // Shopping
  { name: "Kaspi.kz", category: "Shopping" },
  { name: "Wildberries", category: "Shopping" },

  // Gaming
  { name: "Steam", category: "Gaming" },
  { name: "Epic Games", category: "Gaming" },

  // Finance
  { name: "Kaspi Bank", category: "Finance" },
  { name: "Halyk Bank", category: "Finance" },

  // Services
  { name: "Naimi.kz", category: "Services" },
  { name: "Freedom Travel", category: "Services" },

  // Telecom
  { name: "izi", category: "Telecom" },
  { name: "Beeline", category: "Telecom" },
  { name: "Tele2", category: "Telecom" },

  // Fitness
  { name: "World Class", category: "Fitness" },

  // Travel
  { name: "Booking.com", category: "Travel" },
  { name: "Airbnb", category: "Travel" },

  // Pharmacy
  { name: "Eapteka", category: "Pharmacy" },

  // Clothing
  { name: "Zara", category: "Clothing" },

  // Other
  { name: "Google", category: "Other" }
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

  // Select a service and use its proper category (no more random assignment!)
  const randomService = serviceDefinitions[Math.floor(Math.random() * serviceDefinitions.length)];
  const serviceName = randomService.name;
  const category = randomService.category;

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

  const data: any = {
    code: randomCode,
    serviceName: serviceName,
    category: category,
    title: randomTitle,
    description: randomDescription,
    type: type,
    minimumOrderAmount: minimumOrderAmount,
    isFirstUserOnly: isFirstUserOnly,
    upvotes: upvotes,
    downvotes: downvotes,
    voteScore: voteScore, // 🆕 Store computed voteScore!
    views: Math.floor(Math.random() * 1000), // Random view count
    shares: Math.floor(Math.random() * 50), // Random share count
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

  // Only add optional fields if they have values
  if (discountPercentage !== undefined) {
    data.discountPercentage = discountPercentage;
  }
  if (discountAmount !== undefined) {
    data.discountAmount = discountAmount;
  }
  if (Math.random() < 0.3) {
    data.screenshotUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png";
  }

  return data;
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

      // Create composite document ID: lowercase sanitized servicename_promocode
      const sanitizedServiceName = promoCodeData.serviceName
        .toLowerCase()
        .replace(/[^a-z0-9]/g, '_')
        .replace(/_{2,}/g, '_')
        .replace(/^_+|_+$/g, '');
      const sanitizedCode = promoCodeData.code.toLowerCase();
      const docId = `${sanitizedServiceName}_${sanitizedCode}`;
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
