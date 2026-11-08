# Changelog

What changed in each Qode release. Generated from conventional commits with [git-cliff](https://git-cliff.org); earlier versions are in the git tags.

## 0.9.2 - 2026-11-08

### Features

- **data:** Retry transient upload failures in workers
- **data:** Require network, back off and dedupe upload work

### Tests

- Add repository, scheduler and notifier fakes to core:testing
- **data:** Cover upload workers, retry policy and scheduling
- **domain:** Cover post and promocode validation and submit use cases
- **post:** Cover PostSubmissionViewModel actions and submission
- **promocode:** Cover wizard step gating and submit rules

### CI

- **release:** Publish internal releases without manual review again
- Bump checkout to v7, setup-java to v6, upload-artifact to v7 and action-gh-release to v2

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

