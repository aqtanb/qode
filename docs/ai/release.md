# Release

Verified at: 73044745
Covers: `.github/workflows`, `androidApp/build.gradle.kts`, `gradle/libs.versions.toml`

## Version

`versionCode` and `versionName` are in `gradle/libs.versions.toml`.

## CI

| Workflow | Purpose |
|---|---|
| `code-quality.yml` | Runs `spotlessCheck` and `checkFullyQualifiedNames` |
| `ci-debug.yml` | Debug CI build |
| `deploy-internal.yml` | Deploys to Play internal testing on pushed `v*` tags |
| `supabase-keep-alive.yml` | Queries `qode-dev` and `qode-prod` every 2 days |
| `supabase-backup.yml` | Daily encrypted `pg_dump` of `qode-prod`; see `docs/ai/backend.md` for its secrets |

Pushing a `v*` tag publishes. Treat it as a production action.

If the commit step fails with "Changes cannot be sent for review automatically", the app has changes that need manual review, such as after a removal. Add `changesNotSentForReview: true` to the upload step, push a new tag, then send the changes for review in Play Console under Publishing overview.

Scheduled workflows run only from the default branch, so the keep-alive and backup start once they reach `main`.

Release builds connect to Supabase `qode-prod`, using the URL, publishable key and web client ID committed in the Gradle files. `WEB_CLIENT_ID` in `local.properties` overrides the client ID, and CI doesn't set it. Don't publish a Supabase release before prod has its data and the release and Play signing SHA-1s are in the `qodein` Android app, or Google sign-in fails.

A local `assembleRelease` or `bundleRelease` uploads R8 mapping files to Firebase `qodein`. Add `-x uploadCrashlyticsMappingFileRelease` to skip that.

## Signing

Release signing reads `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` from properties, and CI supplies them through secrets. The keystore and passwords must never be committed.

Debug builds use each machine's `~/.android/debug.keystore`. A build from another machine can't update an installed debug app, and the install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstalling the debug app fixes that but clears its local data. Google sign-in also needs each machine's SHA-1 registered in the `qode-dev` app.

## Local setup

- Gradle needs `sdk.dir` in `local.properties`, which Android Studio generates. Other config defaults are committed; see `docs/ai/backend.md`.
- `.gitignore` has a `release/` pattern, so new files under `androidApp/src/release/` need `git add -f`.
