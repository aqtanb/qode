# Changelog

What changed in each Qode release. Generated from conventional commits with [git-cliff](https://git-cliff.org); earlier versions are in the git tags.

## 0.9.4 - 2026-11-25

### Features

- **ios:** SwiftUI app with signed-out browsing on top of shared
- **ios:** Email-code sign-in with consent and sign-out
- **ios:** Google sign-in with the iOS OAuth client
- **ios:** Votes, report and block on detail screens
- **ios:** Profile, blocked users and settings with account deletion
- **ios:** Submit promocodes and posts with photos
- **ios:** Firebase Analytics and Crashlytics, iOS CI build
- Display names in signup consent and profile, with a Qode user fallback

### Fixes

- **web:** Pin fflate to 0.7.5 for the ZIP64 infinite-loop advisory
- **ios:** Build only arm64 simulators, since shared has no iosX64 target
- Show a new user right after consent, without a relaunch (#145)

### Chores

- Ping Supabase daily and add weekly deals report script

## 0.9.3 - 2026-11-16

### Features

- **deals:** Start the import routine when the fetch finishes
- **auth:** Sign in with an emailed 6-digit code
- **web:** Add qodein.com website
- **web:** Add app icons and security headers
- **web:** Add error page and enable Worker traces
- **web:** Show the Qode logo in the header
- **web:** Add home banner images
- **web:** Show the logo as an app-icon tile
- **web:** Add icon buttons for language and theme
- **web:** Add search and filters to the home page
- **home:** Popular service chips ranked by picks
- **web:** Popular service chips and sort by the heading
- **web:** Create posts and host privacy and terms
- Send in-app feedback to support@qodein.com

### Fixes

- **deals:** Say "would publish" in dry runs
- **web:** Resolve npm packages from the public registry
- **functions:** Update vulnerable transitive dependencies
- Drop 'from 0₸' and unverified rows from promocode details

### Performance

- **web:** Lazy-load and right-size logos and avatars

## 0.9.2 - 2026-11-08

### Features

- **data:** Retry transient upload failures in workers
- **data:** Require network, back off and dedupe upload work
- **deals:** Import deals from the Telegram chat as Qode Deals
- **deals:** Take the Supabase key from proxy-injected headers
- **deals:** Fetch Telegram in a GitHub Action and hand off through a private bucket

### Tests

- Add repository, scheduler and notifier fakes to core:testing
- **data:** Cover upload workers, retry policy and scheduling
- **domain:** Cover post and promocode validation and submit use cases
- **post:** Cover PostSubmissionViewModel actions and submission
- **promocode:** Cover wizard step gating and submit rules

### CI

- **release:** Publish internal releases without manual review again
- Bump checkout to v7, setup-java to v6, upload-artifact to v7 and action-gh-release to v2
- Run debug build and code quality on dev, release branches and PRs to dev

## 0.9.1 - 2026-11-07

### CI

- **release:** Skip automatic Play review and bump to 0.9.1

## 0.9.0 - 2026-11-01

### Features

- **dev:** Add seed script, fix service counts and 16 KB alignment
- **supabase:** Add schema, security rules, seed and security tests
- **data:** Move data layer to Supabase in shared and add iOS targets
- **backend:** Set up qode-prod for release builds

### Fixes

- **firestore:** Restrict services writes and users listing
- **data:** Build list filters with the client and add browse flow

### Refactoring

- **user:** Remove email from Firestore user model

### Tests

- **data:** Cover Supabase mapping and paging

### Build

- Default public debug config so fresh clones run

### Chores

- Commit Firebase configs and project npm registry

