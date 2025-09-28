import * as admin from 'firebase-admin';

// Initialize Firebase Admin (if not already initialized)
if (!admin.apps.length) {
  const serviceAccount = require('../../src/serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

// Utility functions (same as populateServices.ts)
const translit = (s: string): string => {
  const m: { [key: string]: string } = {
    а:"a",б:"b",в:"v",г:"g",д:"d",е:"e",ё:"e",ж:"zh",з:"z",и:"i",й:"y",к:"k",л:"l",м:"m",н:"n",о:"o",п:"p",р:"r",с:"s",т:"t",у:"u",ф:"f",х:"h",ц:"c",ч:"ch",ш:"sh",щ:"sch",ъ:"",ы:"y",ь:"",э:"e",ю:"yu",я:"ya",
    А:"a",Б:"b",В:"v",Г:"g",Д:"d",Е:"e",Ё:"e",Ж:"zh",З:"z",И:"i",Й:"y",К:"k",Л:"l",М:"m",Н:"n",О:"o",П:"p",Р:"r",С:"s",Т:"t",У:"u",Ф:"f",Х:"h",Ц:"c",Ч:"ch",Ш:"sh",Щ:"sch",Ъ:"",Ы:"y",Ь:"",Э:"e",Ю:"yu",Я:"ya"
  };
  return s.split("").map(ch => m[ch] ?? ch).join("");
};

const sanitizeForId = (s: string): string =>
  translit(s)
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]/g, "_")
    .replace(/_{2,}/g, "_")
    .replace(/^_+|_+$/g, "");

const logo = (domain: string): string => (domain ? `https://logo.clearbit.com/${domain}` : "");

interface ServiceInput {
  name: string;
  category: string;
  domain?: string;
}

async function addSingleService(service: ServiceInput): Promise<string> {
  console.log(`🚀 Adding service: ${service.name}`);

  const categorySlug = sanitizeForId(service.category);
  const serviceSlug = sanitizeForId(service.name);

  // Composite document ID: service_category
  const documentId = `${serviceSlug}_${categorySlug}`;

  const doc = {
    name: service.name,
    category: service.category,
    logoUrl: service.domain ? logo(service.domain) : "",
    domain: service.domain || "",
    promoCodeCount: 0,
    createdAt: admin.firestore.Timestamp.now(),
    updatedAt: admin.firestore.Timestamp.now()
  };

  const docRef = db.collection("services").doc(documentId);
  await docRef.set(doc, { merge: true });

  console.log(`✅ Successfully added service: ${documentId}`);
  return documentId;
}

// Command line usage
if (require.main === module) {
  const args = process.argv.slice(2);

  if (args.length < 2) {
    console.error('Usage: npm run add-service <name> <category> [domain]');
    console.error('Example: npm run add-service "Netflix" "Entertainment" "netflix.com"');
    process.exit(1);
  }

  const [name, category, domain] = args;

  addSingleService({ name, category, domain })
    .then((id) => {
      console.log(`🎉 Service added with ID: ${id}`);
      process.exit(0);
    })
    .catch((error) => {
      console.error('❌ Error adding service:', error);
      process.exit(1);
    });
}

export { addSingleService };