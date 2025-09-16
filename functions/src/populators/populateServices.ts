import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  // For populators, we'll load service account from environment or file
  const serviceAccount = require('../../src/serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

// Utility functions (same as your original)
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

const logo = (domain: string): string => (domain ? `https://logo.clearbit.com/${domain}` : "");

interface ServiceData {
  name: string;
  category: string;
  domain: string;
}

const services: ServiceData[] = [
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

async function populateServices(): Promise<void> {
  console.log('🚀 Starting services population...');
  
  const batch = db.batch();
  let count = 0;
  
  for (const s of services) {
    const categorySlug = sanitizeForId(s.category);
    const serviceSlug = sanitizeForId(s.name);

    // Composite document ID: service_category
    const documentId = `${serviceSlug}_${categorySlug}`;

    const doc = {
      name: s.name,
      category: s.category,
      logoUrl: logo(s.domain),
      domain: s.domain,
      promoCodeCount: 0, // Will be updated by Cloud Function or separate process
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now()
    };

    const docRef = db.collection("services").doc(documentId);
    batch.set(docRef, doc, { merge: true });
    count++;
    
    console.log(`📝 Prepared: ${documentId}`);
  }
  
  await batch.commit();
  console.log(`✅ Successfully populated ${count} services!`);
}

// Run if called directly
if (require.main === module) {
  populateServices()
    .then(() => process.exit(0))
    .catch((error) => {
      console.error('❌ Error populating services:', error);
      process.exit(1);
    });
}

export { populateServices };