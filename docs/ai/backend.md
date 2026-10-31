# Backend

Verified at: 5ca35d55
Covers: `firestore.rules`, `firebase/storage.rules`, `functions/src`, `core/data/src/main/java/com/qodein/core/data/dto`

## Environments

| | Debug | Release |
|---|---|---|
| Firebase project | `qode-dev` | `qodein` |
| App ID | `com.qodein.qode.debug` | `com.qodein.qode` |
| Config | `androidApp/src/debug/google-services.json` | `androidApp/src/release/google-services.json` |

## Firestore

Rules apply per document, so they can't hide one field. Put data with a different audience in a different document.

| Collection | Holds | Written by |
|---|---|---|
| `banners` | Home banners | Admin only |
| `posts` | Posts | Author |
| `promocodes` | Promocodes | Author |
| `services` | Brands and their promocode count | Signed-in users create; server updates |
| `users/{uid}` | Profile, stats, consent | Owner |
| `users/{uid}/blocks` | Blocked user IDs | Owner |
| `users/{uid}/interactions` | Votes and bookmarks | Owner |
| `reports` | Content reports | Reporter creates; immutable afterward |
| `app_config` | Update configuration | Admin only |

Everything else is denied by the catch-all rule. Clients fetch `users` one document at a time, so listing the collection is denied.

## Cloud Functions

The deployed entry point is `functions/src/index.ts`. `functions/index.js` isn't used.

- Triggers keep service counts, user stats and vote scores up to date.
- `updateContentVotesFromInteractions` turns user interactions into content votes.
- `deleteUserAccount` removes a user's content and data.
- `cleanupOldStorageFiles` runs daily at 02:00 UTC.
- The Algolia extension syncs Firestore to search indexes.

## Search and config

Service search goes through Algolia. Its values, plus `WEB_CLIENT_ID`, come from `local.properties`. If they're missing, the app builds but search and Google sign-in fail. Algolia then tries to reach `null-*.algolianet.com`.

## Firebase CLI

Use `--project qode-dev` or `--project qodein`; the repo has no `.firebaserc`. The emulator needs Java 21 or newer. Android Studio's bundled JDK works.

Test rule changes against the Firestore emulator before deploying:

```
firebase emulators:exec --only firestore --project demo-qode "<test command>"
```
