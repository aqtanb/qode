
const admin = require("firebase-admin");

// --- IMPORTANT ---
// The populator script will look for the service account key
// in the `src` folder.
// -----------------
const serviceAccount = require("./src/serviceAccountKey.json");

if (admin.apps.length === 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

const banners = [
    {
        documentId: "S5qRtbh64SW2eWyhp3px",
        brandName: "Qode",
        createdAt: new admin.firestore.Timestamp(1755998031, 0),
        ctaDescription: {
            "default": "Contact us",
            "kk": "Бізбен байланысыңыз",
            "ru": "Свяжитесь с нами",
            "en": "Contact us"
        },
        ctaTitle: {
            "default": "Place your advertisement",
            "ru": "Разместите вашу рекламу",
            "kk": "Жарнамаңызды орналастырыңыз",
            "en": "Place your advertisement"
        },
        ctaUrl: "mailto:qodeinhq@gmail.com?subject=Advertising%20Inquiry&body=Hi%20Qodein!",
        expiresAt: new admin.firestore.Timestamp(2195242800, 0),
        imageUrl: "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1764677064/qode_ofoygq.jpg",
        isActive: true,
        priority: 99,
        targetCountries: [],
        updatedAt: new admin.firestore.Timestamp(1755998031, 0)
    },
    {
        documentId: "Xqi9v41RZPSpCiBRfGMo",
        brandName: "Qode",
        createdAt: new admin.firestore.Timestamp(1755998031, 0),
        ctaDescription: {
            "en": "The latest Qode related news",
            "kk": "Qode-қа қатысты соңғы жаңалықтар",
            "ru": "Последние новости, связанные с Qode",
            "default": "We post the latest Qode related news"
        },
        ctaTitle: {
            "default": "Join our community",
            "ru": "Подписывайтесь на телеграм",
            "kk": "Телеграм арнамызға тіркеліңіз",
            "en": "Join our community"
        },
        ctaUrl: "tg:resolve?domain=qodeinhq",
        expiresAt: new admin.firestore.Timestamp(2195242800, 0),
        imageUrl: "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1764679501/georg-arthur-pflueger-ukfrgATPD4Y-unsplash_twtrjh.jpg",
        isActive: true,
        priority: 98,
        targetCountries: [],
        updatedAt: new admin.firestore.Timestamp(1755998031, 0)
    }
];

async function populateBanners() {
    const bannersCollection = db.collection("banners");
    console.log("🚀 Starting banners population...");

    const batch = db.batch();
    let count = 0;

    for (const banner of banners) {
        const { documentId, ...data } = banner;
        const docRef = bannersCollection.doc(documentId);
        batch.set(docRef, data);
        count++;
        console.log(`📝 Prepared: ${documentId}`);
    }

    await batch.commit();
    console.log(`✅ Successfully populated ${count} banners!`);
}

// Run if called directly from the command line
if (require.main === module) {
    populateBanners()
        .then(() => process.exit(0))
        .catch((error) => {
            console.error("❌ Error populating banners:", error);
            process.exit(1);
        });
}

// Export the function in case you want to use it elsewhere
module.exports = { populateBanners };
