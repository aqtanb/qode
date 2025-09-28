import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  const serviceAccount = require('../../src/serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

// Utility functions (same as services populator)
const translit = (s: string): string => {
  const m: { [key: string]: string } = {
    а:"a",б:"b",в:"v",г:"g",д:"d",е:"e",ё:"e",ж:"zh",з:"z",и:"i",й:"y",к:"k",л:"l",м:"m",н:"n",о:"o",п:"p",р:"r",с:"s",т:"t",у:"u",ф:"f",х:"h",ц:"c",ч:"ch",ш:"sh",щ:"sch",ъ:"",ы:"y",ь:"",э:"e",ю:"yu",я:"ya",
    А:"a",Б:"b",В:"v",Г:"g",Д:"d",Е:"e",Ё:"e",Ж:"zh",З:"z",И:"i",Й:"y",К:"k",Л:"l",М:"m",Н:"n",О:"o",П:"p",Р:"r",С:"s",Т:"t",У:"u",Ф:"f",Х:"h",Ц:"c",Ч:"ch",Ш:"sh",Щ:"sch",Ъ:"",Ы:"y",Ь:"",Э:"e",Ю:"yu",Я:"ya"
  };
  return s.split("").map(ch => m[ch] ?? ch).join("");
};

// Transliterate THEN sanitize for meaningful IDs
const sanitizeForId = (s: string): string =>
  translit(s)  // Convert Cyrillic to Latin first
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]/g, "_")
    .replace(/_{2,}/g, "_")  // Clean up consecutive underscores
    .replace(/^_+|_+$/g, ""); // Remove leading/trailing underscores

// Enhanced sample data based on your Kazakhstan market
const sampleCodes = [
  "SAVE10", "DISCOUNT20", "OFFER30", "PROMO50", "DEAL15", "COUPON25", "FLASH40", "SALE60", "BONUS5", "EXTRA70",
  "WELCOME10", "FIRST20", "SUMMER30", "WINTER40", "SPRING50", "FALL60", "HOLIDAY70", "BLACKFRIDAY80", "CYBERMONDAY90", "NEWYEAR100",
  "ALMATY10", "ASTANA20", "KAZAKHSTAN30", "TENGE40", "QAZAQ50", "BAITEREK60"
];

// Kazakhstan service definitions - EXACTLY matching populateServices.ts
const serviceDefinitions = [
  // ENTERTAINMENT (STREAMING + GAMING + MUSIC + ENTERTAINMENT)
  { name: "BeeTV", category: "Entertainment", domain: "beetv.kz" },
  { name: "Meloman", category: "Entertainment", domain: "meloman.kz" },
  { name: "Яндекс Плюс", category: "Entertainment", domain: "plus.yandex.kz" },

  // FOOD
  { name: "Dodo Pizza", category: "Food", domain: "dodopizza.kz" },
  { name: "Saya Sushi", category: "Food", domain: "saya-sushi.kz" },
  { name: "Vlife", category: "Food", domain: "vlife.kz" },
  { name: "Del Papa", category: "Food", domain: "delpapa.kz" },
  { name: "DIONA", category: "Food", domain: "diona.kz" },
  { name: "Shaurma Food", category: "Food", domain: "shaurmafood.kz" },
  { name: "Papa John's", category: "Food", domain: "papajohns.kz" },
  { name: "Tanuki", category: "Food", domain: "tanuki.kz" },
  { name: "Manga Sushi", category: "Food", domain: "mangasushi.kz" },
  { name: "Izuimi Sushi", category: "Food", domain: "izuimi.kz" },
  { name: "Burger King", category: "Food", domain: "burgerking.kz" },
  { name: "Яндекс Лавка", category: "Food", domain: "lavka.yandex.kz" },
  { name: "Яндекс Экспресс", category: "Food", domain: "express.yandex.kz" },

  // TRANSPORT
  { name: "Яндекс Go", category: "Transport", domain: "go.yandex.kz" },
  { name: "Anytime", category: "Transport", domain: "anytime.kz" },
  { name: "Vietjet Air", category: "Transport", domain: "vietjetair.com" },

  // SHOPPING (SHOPPING + MARKETPLACE)
  { name: "Flip", category: "Shopping", domain: "flip.kz" },
  { name: "Halyk Market", category: "Shopping", domain: "halykmarket.kz" },
  { name: "Clever Market", category: "Shopping", domain: "clevermarket.kz" },
  { name: "Arbuz", category: "Shopping", domain: "arbuz.kz" },

  // EDUCATION
  { name: "Яндекс Практикум", category: "Education", domain: "praktikum.yandex.kz" },
  { name: "Яндекс 360", category: "Other", domain: "360.yandex.kz" },

  // FITNESS
  { name: "Sportmaster", category: "Fitness", domain: "sportmaster.kz" },

  // BEAUTY
  { name: "BeautyMania", category: "Beauty", domain: "beautymania.kz" },
  { name: "L'Etoile", category: "Beauty", domain: "letoile.kz" },

  // CLOTHING
  { name: "Mark Formelle", category: "Clothing", domain: "markformelle.kz" },
  { name: "DeFacto", category: "Clothing", domain: "defacto.com" },

  // ELECTRONICS
  { name: "Sulpak", category: "Electronics", domain: "sulpak.kz" },
  { name: "Technodom", category: "Electronics", domain: "technodom.kz" },
  { name: "Tefal.kz", category: "Electronics", domain: "tefal.kz" },
  { name: "Xiaomi", category: "Electronics", domain: "xiaomi.com" },

  // TRAVEL
  { name: "Freedom Travel", category: "Travel", domain: "freedom.kz" },

  // JEWELRY
  { name: "Sokolov", category: "Jewelry", domain: "sokolov.ru" }
];


const sampleDescriptions = [
  "Қазақстанда ең жақсы бағалар", "Сүйікті тауарларыңызға керемет жеңілдіктер алыңыз",
  "Get amazing discounts on your favorite items in Kazakhstan", "Save big on selected products",
  "Limited stock available in Almaty and Astana", "Exclusive offer for first-time users in Kazakhstan",
  "Enjoy extra savings this season", "Special deal just for Kazakhstani customers",
  "Don't miss out on this opportunity", "Best prices guaranteed in Kazakhstan",
  "Астана мен Алматыда жеткізу тегін", "Қазақстанның барлық қалаларына жеткізу"
];

const kazakhTargetCountries = ["KZ"];

interface PromoCodeData {
  code: string;
  serviceId?: string; // Reference to Service document
  serviceName: string;
  category?: string;
  description?: string;
  type: string; // "percentage" or "fixed"
  discountPercentage?: number;
  discountAmount?: number;
  minimumOrderAmount: number;
  isFirstUserOnly: boolean;
  isOneTimeUseOnly: boolean;
  upvotes: number;
  downvotes: number;
  shares: number;
  targetCountries: string[];
  isVerified: boolean;
  startDate: admin.firestore.Timestamp;
  endDate: admin.firestore.Timestamp;
  createdAt: admin.firestore.FieldValue;
  createdBy: string;
  createdByUsername?: string;
  createdByAvatarUrl?: string;
  serviceLogoUrl?: string;
}

function createSamplePromoCode(): PromoCodeData {
  const randomCode = sampleCodes[Math.floor(Math.random() * sampleCodes.length)];

  // Select a service and use its proper category and logo
  const selectedService = serviceDefinitions[Math.floor(Math.random() * serviceDefinitions.length)];
  const serviceName = selectedService.name;
  const category = selectedService.category;
  const serviceLogoUrl = selectedService.domain ? `https://logo.clearbit.com/${selectedService.domain}` : undefined;

  // Generate service ID matching the services collection format
  const categorySlug = sanitizeForId(category);
  const serviceSlug = sanitizeForId(serviceName);
  const serviceId = `${serviceSlug}_${categorySlug}`;

  const randomDescription = sampleDescriptions[Math.floor(Math.random() * sampleDescriptions.length)];

  const type = Math.random() < 0.7 ? "percentage" : "fixed";
  const discountPercentage = type === "percentage" ? Math.floor(Math.random() * 50) + 10 : undefined; // 10-60%
  const discountAmount = type === "fixed" ? Math.floor(Math.random() * 5000) + 500 : undefined; // 500-5500 KZT
  const minimumOrderAmount = Math.floor(Math.random() * 10000) + 1000; // 1000-11000 KZT
  const isFirstUserOnly = Math.random() < 0.3;
  const isOneTimeUseOnly = Math.random() < 0.2; // 20% are one-time use only
  const isVerified = Math.random() < 0.4;

  // More realistic vote distribution
  const upvotes = Math.floor(Math.random() * 500) + 1; // 1-500 upvotes
  const downvotes = Math.floor(Math.random() * 50); // 0-49 downvotes

  // Random dates
  const now = new Date();
  const startOffset = Math.floor(Math.random() * 30) * 86400000; // Past 30 days
  const endOffset = (Math.floor(Math.random() * 90) + 7) * 86400000; // Future 7-97 days
  const startDate = admin.firestore.Timestamp.fromDate(new Date(now.getTime() - startOffset));
  const endDate = admin.firestore.Timestamp.fromDate(new Date(now.getTime() + endOffset));

  const data: any = {
    code: randomCode,
    serviceId: serviceId,
    serviceName: serviceName,
    category: category,
    description: randomDescription,
    type: type,
    minimumOrderAmount: minimumOrderAmount,
    isFirstUserOnly: isFirstUserOnly,
    isOneTimeUseOnly: isOneTimeUseOnly,
    upvotes: upvotes,
    downvotes: downvotes,
    voteScore: upvotes - downvotes, // Computed field for Firestore sorting
    shares: Math.floor(Math.random() * 50),
    targetCountries: kazakhTargetCountries,
    isVerified: isVerified,
    startDate: startDate,
    endDate: endDate,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    createdBy: "mock_data",
    createdByUsername: "Mock Data",
    createdByAvatarUrl: "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png"
  };

  // Only add optional fields if they have values
  if (discountPercentage !== undefined) {
    data.discountPercentage = discountPercentage;
  }
  if (discountAmount !== undefined) {
    data.discountAmount = discountAmount;
  }

  // Add service logo URL if available
  if (serviceLogoUrl) {
    data.serviceLogoUrl = serviceLogoUrl;
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

      // Create composite document ID: servicename_promocode (matching Kotlin format)
      const sanitizedServiceName = sanitizeForId(promoCodeData.serviceName);
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
