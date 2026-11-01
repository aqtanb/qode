# Qode

Android app for sharing promocodes and posts, with iOS planned. Kotlin, Compose, Koin, Supabase. Domain and data code live in the KMP `shared` module.

## Knowledge map

Read a doc only when the task touches its area. Don't read them all up front.

| Doc | Read it when working on |
|---|---|
| `docs/ai/app-flows.md` | Screens, navigation, user journeys |
| `docs/ai/backend.md` | Database, Storage, Edge Functions, search, environments |
| `docs/ai/release.md` | Versioning, signing, CI, Play deployment |
| `docs/ERROR_HANDLING.md` | `Result` and error types |

To see the app running on a device, use the `check-app-on-device` skill.

## Keeping knowledge current

- Each doc in `docs/ai/` has a `Verified at` commit and `Covers` paths. Before relying on one, run `git log --oneline <commit>..HEAD -- <paths>`. If it lists commits, check the changed code and correct the doc.
- The code is the source of truth. If a doc disagrees with it, trust the code and fix the doc in the same change.
- When a change alters a flow, collection, or release step, update the doc in the same commit and move `Verified at` forward.
- Save hard-won facts that the code can't show. Don't copy what a quick search can find, such as file lists or class signatures.
- This repo is public. Secrets, unfixed vulnerabilities and private notes never go in tracked files; keep them in auto memory.

## Layout

- `androidApp`: entry point, `QodeNavHost`, app state.
- `feature/*`: screens, ViewModels, and navigation in each feature's `navigation/` package.
- `shared`: domain models, use cases, repository interfaces, and the Supabase data layer.
- `core/data`: Android-only data code: upload workers, DataStore, image reading.
- `core/designsystem`, `core/ui`: design system and shared UI.
- `supabase`: migrations, Edge Functions, seed and security test scripts.
- `functions`, `firebase`: only for the released Firebase app, until prod moves.

The module list in `settings.gradle.kts` is authoritative; the README's list is out of date.

## Commands

Run `sh ./gradlew` if `./gradlew` isn't executable.

- Debug build: `./gradlew :androidApp:assembleDebug`
- Install on device: `./gradlew :androidApp:installDebug`
- Unit tests: `./gradlew test`
- iOS compile check: `./gradlew :shared:compileKotlinIosSimulatorArm64`
- CI checks: `./gradlew spotlessCheck checkFullyQualifiedNames`
- Format: `./gradlew spotlessApply`

## Conventions

- Dependency direction: feature → shared use case → repository interface → `shared/data`. Keep `shared/commonMain` free of Android and Java APIs.
- Return `Result<Data, OperationError>` instead of throwing across layers.
- User-facing strings need `values`, `values-ru`, and `values-kk`.
- Debug builds use Supabase project `qode-dev` and app ID `com.qodein.qode.debug`. Release builds use `com.qodein.qode`, and have no Supabase project yet.

## Guardrails

- Ask before touching Firebase `qodein`, any future Supabase prod project, or pushing a `v*` tag, which starts Play deployment.
- Changes to data that released apps read must stay compatible with those apps. Check the rollout order first.
