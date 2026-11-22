# iOS

Verified at: 763f6026
Covers: `iosApp`, `shared/src/iosMain`, `shared/build.gradle.kts`

The iPhone app in `iosApp/` is native SwiftUI. It uses the KMP `shared` module as the static framework `Shared`, which holds models, use cases and the Supabase data layer. None of the Android Compose UI is reused.

Status: Milestone 1, signed-out browsing on the simulator against `qode-dev`. It has Home (banners, service chips, sort, promocodes), promocode detail, the service search sheet, Feed and post detail. Still missing: sign-in (email OTP, Google, plus Sign in with Apple, which App Store guideline 4.8 requires alongside Google), votes, report/block, profile/settings, submissions, push, Crashlytics, a prod config and App Store release. The plan is in `~/.claude-personal/plans/could-it-port-it-serene-valley.md`.

## How Swift reaches Kotlin

- `shared/src/iosMain/.../ios/QodeApi.kt` is the only API Swift uses. It wraps use cases and turns `Result` into return values or a thrown `QodeException`. It maps models to plain classes (`PromocodeItem`, `PostItem`, ...) because value classes, `Instant` and generic `Result` export badly to Objective-C. Pagination cursors stay inside `QodeApi`. Kotlin `suspend` functions show up in Swift as `async throws`, so SKIE isn't needed.
- `KoinIos.kt` (`KoinIosKt.doInitKoin` in Swift) starts Koin with `sharedDataModule`, `domainModule` and an iOS module. That module binds the interfaces Android provides from `core/data`. Only `ReportRepository` exists so far (`IosReportRepository`, `NSUserDefaults`). Use cases that need the others (storage, submission schedulers, device preferences, Google ID token) fail at resolution until they're bound.
- Name clashes: `shared` exports its own `Error` type, so Swift code writes `Swift.Error`. A Kotlin property called `description` becomes `description_`.

## Project

- `iosApp/project.yml` is the XcodeGen spec. `Qode.xcodeproj` and `Info.plist` are generated and gitignored. Run `xcodegen generate` after adding files or changing settings.
- A pre-build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, so Xcode builds the Kotlin side too.
- `Config/Debug.xcconfig` holds the `qode-dev` URL and publishable key; both are public. It writes the URL as `https:/$()/…` because xcconfig treats `//` as a comment. Both configurations use it until a prod config exists.
- Strings: `Qode/Localizable.xcstrings` is generated from the Android `strings.xml` files (en, ru, kk) by `iosApp/scripts/import_android_strings.py`. Add a key to `KEYS` there and rerun it. Don't edit the catalog by hand. Swift reads strings with `L("key")`.

## Commands (from `iosApp/`)

- Generate: `xcodegen generate`
- Build: `xcodebuild -project Qode.xcodeproj -scheme Qode -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath build/DerivedData build`
- Run: `xcrun simctl boot "iPhone 17 Pro"; xcrun simctl install booted build/DerivedData/Build/Products/Debug-iphonesimulator/Qode.app; xcrun simctl launch booted com.qodein.qode.debug`
- Another language: add `-AppleLanguages "(kk)"` to `simctl launch`.
- Drive the UI with `maestro test <flow>.yaml`; it works on the simulator. `inputText` needs the search field tapped first.
