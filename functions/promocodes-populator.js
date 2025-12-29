
const admin = require("firebase-admin");

// Load the service account key
const serviceAccount = require("./src/serviceAccountKey.json");

// Initialize Firebase Admin
if (admin.apps.length === 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function populatePromocodes() {
    console.log("🚀 Starting promocodes population...");

    const authors = [
        { id: "5bNoK0lQ19UVAoW0IVoMmXbg6qk1", name: "aktan", avatar: "https://lh3.googleusercontent.com/a/ACg8ocKAbCmwm0d8nX0tzVbBLODz-jgEpD4TpLCW9WTKEkqyUb_9XqVi9Q=s96-c" },
        { id: "user_sarah_92", name: "sarah_92", avatar: "https://i.pravatar.cc/150?u=sarah_92" },
        { id: "user_michael_k", name: "michael_k", avatar: "https://i.pravatar.cc/150?u=michael_k" },
        { id: "user_priya_shah", name: "priya_shah", avatar: "https://i.pravatar.cc/150?u=priya_shah" },
        { id: "user_alex_chen", name: "alex_chen", avatar: "https://i.pravatar.cc/150?u=alex_chen" },
        { id: "user_maria_lopez", name: "maria_lopez", avatar: "https://i.pravatar.cc/150?u=maria_lopez" },
        { id: "user_james_wilson", name: "james_wilson", avatar: "https://i.pravatar.cc/150?u=james_wilson" },
        { id: "user_nina_patel", name: "nina_patel", avatar: "https://i.pravatar.cc/150?u=nina_patel" }
    ];

    const services = [
        { name: "Glovo", id: "glovo_kz", logoUrl: "https://picsum.photos/seed/glovo/200" },
        { name: "Yandex Go", id: "yandex_go", logoUrl: "https://picsum.photos/seed/yandex/200" },
        { name: "Chocofood", id: "chocofood", logoUrl: "https://picsum.photos/seed/choco/200" },
        { name: "Halyk Market", id: "efd5d8d94faf46fc8afa6a116c9798e4", logoUrl: "https://picsum.photos/seed/halyk/200" },
        { name: "Kaspi.kz", id: "kaspi_kz", logoUrl: "https://picsum.photos/seed/kaspi/200" },
        { name: "Arbuz.kz", id: "arbuz_kz", logoUrl: "https://picsum.photos/seed/arbuz/200" },
        { name: "Magnum", id: "magnum_kz", logoUrl: "https://picsum.photos/seed/magnum/200" },
        { name: "Small", id: "small_kz", logoUrl: "https://picsum.photos/seed/small/200" },
        { name: "Wolt", id: "wolt_kz", logoUrl: "https://picsum.photos/seed/wolt/200" },
        { name: "Chocobook", id: "chocobook", logoUrl: "https://picsum.photos/seed/chocobook/200" }
    ];

    const promoCodes = [
        "WELCOME2025", "NEWYEAR", "FIRSTORDER", "FREESHIP", "SAVE10",
        "WINTER25", "FLASHSALE", "MEGADEAL", "BONUS50", "LUCKY7",
        "SPECIAL20", "DISCOUNT15", "VIPSALE", "EXTRA5", "SUPERSAVE",
        "HAPPYDAY", "WEEKENDDEAL", "FLASHDEAL", "BIGSAVE", "PROMO100",
        "PITOMOPILIS", "GETMORE", "SAVEMORE", "BESTDEAL", "HOTDEAL"
    ];

    const descriptions = [
        "Valid for first-time users only",
        "Limited time offer - don't miss out!",
        "Available for all products",
        "Weekend special discount",
        "New user welcome bonus",
        "Flash sale - ends soon!",
        "Exclusive discount for app users",
        null,
        "Special promotion for loyal customers",
        null,
        "Limited quantity available"
    ];

    const batch = db.batch();
    let count = 0;

    for (let i = 0; i < 50; i++) {
        const randomAuthor = authors[Math.floor(Math.random() * authors.length)];
        const randomService = services[Math.floor(Math.random() * services.length)];
        const randomCode = promoCodes[Math.floor(Math.random() * promoCodes.length)];

        // Random dates
        const now = Date.now();
        const daysAgo = Math.floor(Math.random() * 30); // Created 0-30 days ago
        const createdDate = new Date(now - daysAgo * 24 * 60 * 60 * 1000);
        const startDate = new Date(createdDate.getTime() - Math.floor(Math.random() * 5) * 24 * 60 * 60 * 1000);
        const endDate = new Date(now + Math.floor(Math.random() * 60 + 10) * 24 * 60 * 60 * 1000); // Ends 10-70 days from now

        // Random discount
        const isPercentage = Math.random() > 0.5;
        const discountValue = isPercentage
            ? Math.floor(Math.random() * 30) + 5 // 5-35% discount
            : Math.floor(Math.random() * 10000) + 1000; // 1000-11000 fixed amount

        const upvotes = Math.floor(Math.random() * 50);
        const downvotes = Math.floor(Math.random() * 20);

        const promocode = {
            code: `${randomCode}${Math.random() > 0.7 ? Math.floor(Math.random() * 100) : ''}`,
            startDate: admin.firestore.Timestamp.fromDate(startDate),
            endDate: admin.firestore.Timestamp.fromDate(endDate),
            serviceName: randomService.name,
            discount: {
                type: isPercentage ? "Percentage" : "FixedAmount",
                value: discountValue
            },
            minimumOrderAmount: Math.floor(Math.random() * 20000) + 5000, // 5000-25000
            description: descriptions[Math.floor(Math.random() * descriptions.length)],
            serviceId: randomService.id,
            serviceLogoUrl: randomService.logoUrl,
            firstUserOnly: Math.random() > 0.7,
            oneTimeUseOnly: Math.random() > 0.6,
            verified: Math.random() > 0.5,
            upvotes: upvotes,
            downvotes: downvotes,
            voteScore: upvotes - downvotes,
            authorId: randomAuthor.id,
            authorUsername: randomAuthor.name,
            authorAvatarUrl: randomAuthor.avatar,
            createdAt: admin.firestore.Timestamp.fromDate(createdDate),
            updatedAt: admin.firestore.Timestamp.fromDate(createdDate)
        };

        const docRef = db.collection("promocodes").doc();
        batch.set(docRef, promocode);
        count++;
    }

    await batch.commit();
    console.log(`✅ Successfully populated ${count} promocodes!`);
}

// Run if called directly from the command line
if (require.main === module) {
    populatePromocodes()
        .then(() => process.exit(0))
        .catch((error) => {
            console.error("❌ Error populating promocodes:", error);
            process.exit(1);
        });
}

module.exports = { populatePromocodes };