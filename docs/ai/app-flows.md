# App flows

Verified at: 5ca35d55
Covers: `androidApp/src/main/java/com/qodein/qode/navigation`, `feature/*/src/main/java/com/qodein/feature/*/navigation`

Routes are type-safe `@Serializable` objects. `QodeNavHost.kt` wires every section together and starts at `HomeBaseRoute`.

## Top level

The bottom bar has two tabs, Home and Feed. A floating + button opens **Submit Promocode** and **Create Post**. The profile avatar and settings gear are in the Feed top bar.

## Signed-out behavior

Browsing works without an account: Home, Feed, details and Settings. Profile, Submit Promocode and Create Post show the sign-in sheet (`AuthBottomSheetRoute`), and its `AuthPromptAction` argument sets the message. Sign-in is Google only.

## Journeys

| Journey | Path | Owner |
|---|---|---|
| Browse deals | Home → banners, Service and Sort filters → promocode detail | `feature/home`, `feature/promocode` |
| Filter by service | Home → Service sheet → popular chips or search | `feature/service` (search uses Algolia) |
| Read posts | Feed → post detail → vote or share | `feature/post` |
| Submit promocode | + → auth if needed → form → background upload | `feature/promocode`, `UploadPromocodeWorker` |
| Create post | + → auth if needed → form → background upload | `feature/post`, `UploadPostWorker` |
| New service | Submission → `GetOrCreateServiceUseCase` | `shared`, `FirestoreServiceDataSource` |
| Report content | Detail → More → report | `feature/report` |
| Block user | Detail → More → block dialog | `feature/block` |
| Manage blocks | Profile → Blocked Users | `feature/block` |
| Account | Profile, or Settings → language, theme, notifications, licenses, feedback, about, delete account | `feature/profile`, `feature/settings` |

## Signup

The first sign-in runs `AcceptConsentAndCreateUserUseCase`, which creates `users/{uid}`. `EnsureUserExists` checks for an existing document. Email isn't stored in the app's `User` model; Firebase Auth owns it.

## Other reads of user documents

The Blocked Users screen is the only place that loads another person's `users/{uid}` document. It uses the display name and photo. Author details on content are copied into each post or promocode, so feeds don't need a read per user.
