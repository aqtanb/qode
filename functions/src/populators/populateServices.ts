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

const slug = (s: string): string =>                                                                                                                                                                                                                                                                                               
  translit(s)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");

const logo = (domain: string): string => (domain ? `https://logo.clearbit.com/${domain}` : "");

interface ServiceData {
  name: string;
  category: string;
  domain: string;
}

const services: ServiceData[] = [
  // Food
  { name: "Яндекс Лавка", category: "Food", domain: "lavka.yandex.ru" },
  { name: "Dodo", category: "Food", domain: "dodopizza.kz" },
  { name: "chocofood", category: "Food", domain: "chocofood.kz" },
  { name: "arbuz", category: "Food", domain: "arbuz.kz" },
  { name: "abr", category: "Food", domain: "" },

  // Beauty
  { name: "Золотое Яблоко", category: "Beauty", domain: "goldapple.ru" },
  { name: "Letoile", category: "Beauty", domain: "letoile.ru" },

  // Electronics
  { name: "Sulpak", category: "Electronics", domain: "sulpak.kz" },
  { name: "Technodom", category: "Electronics", domain: "technodom.kz" },

  // Jewelry
  { name: "Sokolov", category: "Jewelry", domain: "sokolov.ru" },

  // Health
  { name: "iHerb", category: "Health", domain: "iherb.com" },

  // Transport
  { name: "anytime", category: "Transport", domain: "anytime.kz" },

  // Education
  { name: "Яндекс Практикум", category: "Education", domain: "practicum.yandex.ru" },

  // Entertainment / Streaming
  { name: "Яндекс Плюс", category: "Entertainment", domain: "plus.yandex.ru" },
  { name: "КиноПоиск", category: "Entertainment", domain: "kinopoisk.ru" },
  { name: "YouTube Premium", category: "Streaming", domain: "youtube.com" },
  { name: "Netflix", category: "Streaming", domain: "netflix.com" },
  { name: "Spotify", category: "Music", domain: "spotify.com" },

  // Marketplace
  { name: "Teez", category: "Marketplace", domain: "" },
  { name: "Halyk Market", category: "Marketplace", domain: "halykmarket.kz" },
  { name: "clever market", category: "Marketplace", domain: "" },
  { name: "ForteMarket", category: "Marketplace", domain: "fortemarket.kz" },
  { name: "flowwow", category: "Marketplace", domain: "flowwow.com" },

  // Shopping
  { name: "Kaspi.kz", category: "Shopping", domain: "kaspi.kz" },
  { name: "Wildberries", category: "Shopping", domain: "wildberries.kz" },

  // Gaming
  { name: "Steam", category: "Gaming", domain: "store.steampowered.com" },
  { name: "Epic Games", category: "Gaming", domain: "epicgames.com" },

  // Finance
  { name: "Kaspi Bank", category: "Finance", domain: "kaspi.kz" },
  { name: "Halyk Bank", category: "Finance", domain: "halykbank.kz" },

  // Services
  { name: "Naimi.kz", category: "Services", domain: "naimi.kz" },
  { name: "Freedom Travel", category: "Services", domain: "" },

  // Telecom
  { name: "izi", category: "Telecom", domain: "izi.me" },
  { name: "Beeline", category: "Telecom", domain: "beeline.kz" },
  { name: "Tele2", category: "Telecom", domain: "tele2.kz" },

  // Fitness
  { name: "World Class", category: "Fitness", domain: "worldclass.kz" },

  // Travel
  { name: "Booking.com", category: "Travel", domain: "booking.com" },
  { name: "Airbnb", category: "Travel", domain: "airbnb.com" },

  // Pharmacy
  { name: "Eapteka", category: "Pharmacy", domain: "eapteka.kz" },

  // Clothing
  { name: "Zara", category: "Clothing", domain: "zara.com" },

  // Other
  { name: "Google", category: "Other", domain: "google.com" }
];

async function populateServices(): Promise<void> {
  console.log('🚀 Starting services population...');
  
  const batch = db.batch();
  let count = 0;
  
  for (const s of services) {
    const categorySlug = slug(s.category);
    const serviceSlug = slug(s.name);

    // Composite document ID: category_service                                                                                                                                                                                                                                                                    
    const documentId = `${categorySlug}_${serviceSlug}`;

    const doc = {
      name: s.name,
      category: s.category,
      logoUrl: logo(s.domain),
      domain: s.domain,
      isPopular: true,
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