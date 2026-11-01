# Backend

Verified at: 30922d10
Covers: `supabase`, `shared/src/commonMain/kotlin/com/qodein/shared/data`, `core/data/src/main/java/com/qodein/core/data`

Debug builds use the Supabase project `qode-dev` (`sjfzxrnqqlgywwqofdhu`). The prod project doesn't exist yet, so release builds have empty Supabase values and can't run. The Play release still uses Firebase project `qodein`.

Firebase stays for Crashlytics, Analytics and messaging only. The `firebase/`, `functions/` and `firestore.*` files serve the released app and go away once prod moves.

## Database

`supabase/migrations` is the source of truth. Push with `supabase db push --linked`.

Clients get named columns through grants, so counts, votes and `verified` are server-owned. Triggers maintain them. `vote_score` is a generated column.

| Table | Written by |
|---|---|
| `users` | Owner; readable only by the owner |
| `services` | Signed-in users create; no client updates |
| `promocodes`, `posts` | Author inserts and deletes |
| `interactions` | Owner, through `save_interaction` |
| `blocks` | Owner |
| `reports` | Reporter creates and reads |
| `banners`, `app_config` | Admin only |

`promocodes_view` and `posts_view` run as their owner on purpose, so feeds can show author names while `users` stays private. Expose public columns only there. Other users' names come from `get_user_profile`.

This project doesn't expose new tables by default. A new table needs explicit grants, including for `service_role`.

## Storage and functions

Buckets `post-images` and `promocode-images` are public to read. Uploads must go under `<user id>/`, up to 5 MB, as JPEG, PNG or WebP.

`supabase/functions/delete-account` removes a user's files, then their auth user. Foreign keys cascade the rest. Deploy with `supabase functions deploy delete-account --use-api`.

## Search

`search_services` uses `pg_trgm` word similarity, so it tolerates typos. It only searches service names.

## Client

Data sources, DTOs, mappers and repositories live in `shared/commonMain`, so iOS can reuse them. `core/data` holds only Android parts: WorkManager, DataStore, reading image bytes.

`supabase-kt` stays at 3.6.0 because 3.7 and later are built with Kotlin 2.4, and iOS compilation fails on Kotlin 2.3. Keep `:shared:compileKotlinIosSimulatorArm64` passing.

Google sign-in sends a SHA-256 hash of the nonce to Google and the raw nonce to Supabase. The dev web client ID is in `supabase/config.toml`; apply changes with `supabase config push`.

Adding a new service in the app needs `LOGO_DEV_PUBLIC_KEY` in `local.properties`. Without it, the Service step fails with only a brief snackbar.

## Scripts

Run these in `supabase/scripts` after `npm install`. They need `supabase login` and only target `qode-dev`.

- `npm run seed`: fake data. `npm run seed -- --reset` removes it. A re-run skips existing rows, so triggers' counts aren't reset.
- `npm run test:security`: allowed and denied cases for policies, grants, triggers and storage. Run it after any migration.

Run the browse flow with `maestro test .maestro/browse.yaml`. It needs seeded data. Signed-in flows aren't scripted, because Google's sheet needs a person.

The Supabase CLI's `login` needs a real terminal, not Claude Code's `!` prefix.
