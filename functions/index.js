
const functions = require("firebase-functions");
const { populateBanners } = require("./populator");
const { populatePromocodes } = require("./promocodes-populator");

exports.populateBanners = functions.https.onRequest(async (req, res) => {
    try {
        await populateBanners();
        res.status(200).send("Banners populated successfully!");
    } catch (error) {
        console.error("Error populating banners:", error);
        res.status(500).send("Error populating banners.");
    }
});

exports.populatePromocodes = functions.https.onRequest(async (req, res) => {
    try {
        await populatePromocodes();
        res.status(200).send("Promocodes populated successfully!");
    } catch (error) {
        console.error("Error populating promocodes:", error);
        res.status(500).send("Error populating promocodes.");
    }
});
