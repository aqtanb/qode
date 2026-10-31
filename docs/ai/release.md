# Release

Verified at: 5ca35d55
Covers: `.github/workflows`, `androidApp/build.gradle.kts`, `gradle/libs.versions.toml`

## Version

`versionCode` and `versionName` are in `gradle/libs.versions.toml`.

## CI

| Workflow | Purpose |
|---|---|
| `code-quality.yml` | Runs `spotlessCheck` and `checkFullyQualifiedNames` |
| `ci-debug.yml` | Debug CI build |
| `deploy-internal.yml` | Deploys to Play internal testing on pushed `v*` tags |

Pushing a `v*` tag publishes. Treat it as a production action.

## Signing

Release signing reads `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` from properties, and CI supplies them through secrets. The keystore and passwords must never be committed.

Debug builds use each machine's `~/.android/debug.keystore`. A build from another machine can't update an installed debug app, and the install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstalling the debug app fixes that but clears its local data. Google sign-in also needs each machine's SHA-1 registered in the `qode-dev` app.

## Local setup

- Gradle needs `sdk.dir` in `local.properties`, which Android Studio generates. Search and sign-in need the values listed in `docs/ai/backend.md`.
- `.gitignore` has a `release/` pattern, so new files under `androidApp/src/release/` need `git add -f`.
