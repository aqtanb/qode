# iOS

Verified at: 3d1d02a2
Covers: `iosApp`, `shared/src/iosMain`, `shared/build.gradle.kts`

The iPhone app in `iosApp/` is native SwiftUI. It uses the KMP `shared` module as the static framework `Shared`, which holds models, use cases and the Supabase data layer. None of the Android Compose UI is reused.

Status: browsing plus Google and email sign-in on the simulator against `qode-dev`. It has Home (banners, service chips, sort, promocodes), promocode detail, the service search sheet, Feed and post detail. The account button in the Feed toolbar opens sign-in (Google or an emailed code); new users then go through the consent step, and signed-in users get a menu with Sign out. Promocode and post detail have vote buttons and a "…" menu with Report and Block user (Block is hidden on your own content); signed-out taps open sign-in. Still missing: Sign in with Apple, which App Store guideline 4.8 requires because Google sign-in exists; a Blocked Users screen (no unblock on iOS yet), profile/settings, submissions, push, Crashlytics, a prod config and App Store release. The plan is in `~/.claude-personal/plans/could-it-port-it-serene-valley.md`.

## How Swift reaches Kotlin

- `shared/src/iosMain/.../ios/QodeApi.kt` is the only API Swift uses. It wraps use cases and turns `Result` into return values or a thrown `QodeException`. It maps models to plain classes (`PromocodeItem`, `PostItem`, ...) because value classes, `Instant` and generic `Result` export badly to Objective-C. Pagination cursors stay inside `QodeApi`. Kotlin `suspend` functions show up in Swift as `async throws`, so SKIE isn't needed.
- `AuthApi.kt` covers email codes, Google ID tokens, consent, sign-out, legal documents and `currentAccount()`. One instance holds one sign-in attempt, because it keeps the new user that is waiting for consent. `currentAccount()` reads the Supabase session directly. `GetAuthStateUseCase` and `ObserveCurrentUserUseCase` share their flows with a replayed value, and `observeUser` emits only once, so they'd return stale state right after sign-in. Swift's `AccountStore` refreshes after every auth action instead of observing.
- `InteractionApi.kt` handles votes (`GetUserInteractionUseCase`, `ToggleVoteUseCase`), reports (`ReportRepository`, the same call Android's report screen makes) and blocks (`BlockUserUseCase`). It reads the user ID from the Supabase session. Swift uses the shared `VoteState` enum and `VoteState.Companion.shared.computeScoreDelta` for the optimistic score. After a report or block, `AccountStore.contentChanged()` bumps `contentVersion`, and Home and Feed reload on that and on account changes.
- The session is stored by supabase-kt's default settings storage (`NSUserDefaults`), not the Keychain. Move it to the Keychain before an App Store release.
- `KoinIos.kt` (`KoinIosKt.doInitKoin` in Swift) starts Koin with `sharedDataModule`, `domainModule` and an iOS module. That module binds the interfaces Android provides from `core/data`. Only `ReportRepository` exists so far (`IosReportRepository`, `NSUserDefaults`). Use cases that need the others (storage, submission schedulers, device preferences, Google ID token) fail at resolution until they're bound.
- Name clashes: `shared` exports its own `Error` type, so Swift code writes `Swift.Error`. A Kotlin property called `description` becomes `description_`.

## Project

- `iosApp/project.yml` is the XcodeGen spec. `Qode.xcodeproj` and `Info.plist` are generated and gitignored. Run `xcodegen generate` after adding files or changing settings.
- A pre-build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, so Xcode builds the Kotlin side too.
- `Config/Debug.xcconfig` holds the `qode-dev` URL and publishable key; both are public. It writes the URL as `https:/$()/…` because xcconfig treats `//` as a comment. Both configurations use it until a prod config exists.
- Google sign-in uses the GoogleSignIn-iOS SDK, added through Swift Package Manager in `project.yml`, with the same nonce scheme as Android: Google gets the SHA-256 hex of a random nonce, Supabase gets the raw nonce. The iOS OAuth client belongs to the Firebase iOS app `qode-ios-debug` in `qode-dev` (`firebase apps:sdkconfig IOS 1:539742119334:ios:a20a09f5945a54878f9b67`). Its ID and reversed URL scheme are in `Debug.xcconfig`, and it's also in Supabase's Google `client_id` list in `supabase/config.toml`. A prod build needs its own iOS client in `qodein` added to `qode-prod`.
- Strings: `Qode/Localizable.xcstrings` is generated from the Android `strings.xml` files (en, ru, kk) by `iosApp/scripts/import_android_strings.py`. Add a key to `KEYS` there and rerun it. Don't edit the catalog by hand. Swift reads strings with `L("key")`.

## Commands (from `iosApp/`)

- Generate: `xcodegen generate`
- Build: `xcodebuild -project Qode.xcodeproj -scheme Qode -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath build/DerivedData build`
- Run: `xcrun simctl boot "iPhone 17 Pro"; xcrun simctl install booted build/DerivedData/Build/Products/Debug-iphonesimulator/Qode.app; xcrun simctl launch booted com.qodein.qode.debug`
- Another language: add `-AppleLanguages "(kk)"` to `simctl launch`.
- Drive the UI with `maestro test <flow>.yaml`; it works on the simulator. `inputText` needs the field tapped first.
- Google sign-in on the simulator needs a person to type the Google password.
- Test votes, reports and blocks with a throwaway `+alias` account, then delete it through `delete-account` (email code via `/auth/v1/otp` and `/auth/v1/verify`, then `POST /functions/v1/delete-account` with its token). Foreign keys cascade its votes, reports and blocks. Reported IDs also live in the simulator's `NSUserDefaults`, so reported items stay hidden there until the app is reinstalled.
- To test sign-in, use a `+alias` of a Gmail address for a fresh user (it shows the consent step) and read the 6-digit code from the email subject ("Qode code: NNNNNN").
