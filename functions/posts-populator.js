
const admin = require("firebase-admin");
const { v4: uuidv4 } = require("uuid");

// Load the service account key
const serviceAccount = require("./src/serviceAccountKey.json");

// Initialize Firebase Admin
if (admin.apps.length === 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

async function populatePosts() {
    console.log("🚀 Starting posts population...");

    // Generate 32-character UUID hex string (UUID without hyphens)
    const postId = uuidv4().replace(/-/g, '');

    const post = {
        authorId: "STbrUCIexCfig6atxi83QPapX3Y2",
        authorName: "aktan",
        authorAvatarUrl: "https://lh3.googleusercontent.com/a/ACg8ocKAbCmwm0d8nX0tzVbBLODz-jgEpD4TpLCW9WTKEkqyUb_9XqVi9Q=s96-c",
        title: "Posts Feature Coming Soon!",
        content: "The posts feature is not yet ready. Please wait while we finish development. Thank you for your patience!",
        imageUrls: ["https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png"],
        tags: ["qode"],
        upvotes: 9999,
        downvotes: 0,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    const docRef = db.collection("posts").doc(postId);
    await docRef.set(post);

    console.log(`✅ Successfully created post with ID: ${docRef.id}`);
}

// Run if called directly from the command line
if (require.main === module) {
    populatePosts()
        .then(() => process.exit(0))
        .catch((error) => {
            console.error("❌ Error populating posts:", error);
            process.exit(1);
        });
}

module.exports = { populatePosts };
