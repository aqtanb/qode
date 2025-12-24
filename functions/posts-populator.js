
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

    const authors = [
        { id: "QmFOIWZ1D8UmdsJISbWN7mb9BaV2", name: "aktan", avatar: "https://lh3.googleusercontent.com/a/ACg8ocKAbCmwm0d8nX0tzVbBLODz-jgEpD4TpLCW9WTKEkqyUb_9XqVi9Q=s96-c" },
        { id: "user2", name: "jane_doe", avatar: "https://i.pravatar.cc/150?u=jane_doe" },
        { id: "user3", name: "john_smith", avatar: "https://i.pravatar.cc/150?u=john_smith" },
        { id: "user4", name: "emily_jones", avatar: "https://i.pravatar.cc/150?u=emily_jones" }
    ];

    const tags = ["qode", "android", "development", "bug", "feature-request", "feedback", "general", "kotlin", "firebase", "jetpack-compose"];

    const titles = [
        "Just discovered a new bug in the latest release",
        "How do I implement a custom bottom navigation bar?",
        "This is an amazing feature, you should all try it!",
        "Some feedback on the new user interface design",
        "Random thoughts on modern Android development",
        "Check out this awesome new Kotlin library I found",
        "What is everyone working on this week?",
        "Need some help with a complex Firestore query",
        "My app keeps crashing and I can't figure out why",
        "The state of reactive programming in 2025"
    ];

    const contents = [
        "I was trying to use the new feature and the app crashed unexpectedly. I have attached the logs and the steps to reproduce the issue. It seems to happen only on API 33.",
        "I'm trying to build a custom view that looks like the one in the attached screenshot, but I'm completely stuck. Can anyone point me to a good tutorial or a code example?",
        "The new update is fantastic! The performance improvements are very noticeable, and the app feels much smoother now. Keep up the great work, team!",
        "The latest UI update looks very clean, but I think the main call-to-action button is a bit hard to see. I've created a quick mockup with a few suggestions.",
        "Sometimes I wonder if we are all just building the same application over and over again but with slightly different color palettes and feature sets. What are your thoughts?",
        "I found this really cool Kotlin library for declarative UI testing, it's called 'Testify'. It makes writing UI tests so much more intuitive. Has anyone else used it?",
        "This week I'm focused on refactoring our messy data layer. It's a huge undertaking, but I'm confident it's going to improve stability and make future development much faster.",
        "I have a tricky Firestore query that I can't seem to get right. It involves a 'not-in' filter which is not directly supported, and the workarounds are getting very complicated.",
        "I'm at my wit's end. My app builds and runs, but then crashes randomly after a few seconds with a NullPointerException. The stack trace doesn't make any sense.",
        "With the rise of KMP and Compose Multiplatform, it feels like we are entering a new era of reactive UIs. It's exciting to see how this will evolve over the next few years."
    ];

    const batch = db.batch();
    let count = 0;

    for (let i = 0; i < 100; i++) {
        const randomAuthor = authors[Math.floor(Math.random() * authors.length)];
        const randomTimestamp = new Date(Date.now() - Math.floor(Math.random() * 60 * 24 * 60 * 60 * 1000));
        const docId = uuidv4().replace(/-/g, "");

        const post = {
            authorId: randomAuthor.id,
            authorName: randomAuthor.name,
            authorAvatarUrl: randomAuthor.avatar,
            title: titles[Math.floor(Math.random() * titles.length)],
            content: contents[Math.floor(Math.random() * contents.length)],
            imageUrls: Math.random() < 0.25 ? [`https://picsum.photos/seed/${uuidv4()}/800/600`] : [],
            tags: [...tags].sort(() => 0.5 - Math.random()).slice(0, Math.floor(Math.random() * 3) + 1),
            upvotes: Math.floor(Math.random() * 1500),
            downvotes: Math.floor(Math.random() * 100),
            shares: Math.floor(Math.random() * 50),
            commentCount: Math.floor(Math.random() * 75),
            voteScore: 0, // This should be calculated, setting to 0 for now
            createdAt: admin.firestore.Timestamp.fromDate(randomTimestamp),
            updatedAt: admin.firestore.Timestamp.fromDate(randomTimestamp),
        };
        post.voteScore = post.upvotes - post.downvotes;

        const docRef = db.collection("posts").doc(docId);
        batch.set(docRef, post);
        count++;
    }

    await batch.commit();
    console.log(`✅ Successfully populated ${count} posts!`);
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
