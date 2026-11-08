# Backend

Verified at: e38a5b57
Covers: `supabase`, `shared/src/commonMain/kotlin/com/qodein/shared/data`, `core/data/src/main/java/com/qodein/core/data`

Debug builds use the Supabase project `qode-dev` (`sjfzxrnqqlgywwqofdhu`). Release builds use `qode-prod` (`hirfattxysizfprsydyq`). 0.9.1, the first Supabase release, went to Play production on 2026-11-08 as a fresh start: no Firestore data was imported, only the 23 services. Old 0.8.x installs keep reading Firebase `qodein` until they update.

Firebase stays for Crashlytics, Analytics and messaging only. The `firebase/`, `functions/` and `firestore.*` files serve the released app and go away once prod moves.

## Database

`supabase/migrations` is the source of truth. The local link stays on dev, so `supabase db push --linked` pushes there. Push to prod with `supabase db push --project-ref hirfattxysizfprsydyq`, with the password from the Keychain item "Supabase qode-prod database password" in `SUPABASE_DB_PASSWORD`.

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

Neither project exposes new tables, sequences or functions by default. New objects need explicit grants, including for `service_role`.

## Storage and functions

Buckets `post-images` and `promocode-images` are public to read. Uploads must go under `<user id>/`, up to 5 MB, as JPEG, PNG or WebP.

`supabase/functions/delete-account` removes a user's files, then their auth user. Foreign keys cascade the rest. Deploy with `supabase functions deploy delete-account --use-api`, adding `--project-ref hirfattxysizfprsydyq` for prod.

## Search

`search_services` uses `pg_trgm` word similarity, so it tolerates typos. It only searches service names.

## Client

Data sources, DTOs, mappers and repositories live in `shared/commonMain`, so iOS can reuse them. `core/data` holds only Android parts: WorkManager, DataStore, reading image bytes.

Upload workers need a network, back off exponentially and are enqueued as unique work named from the submission's content, so a double-tap uploads once. `UploadFailurePolicy` decides retry or failure for every `OperationError` with no `else`, so a new error type must be classified there. Transient errors retry up to 5 attempts, and the error notification shows only on the final failure.

## Deals import

A daily Claude routine imports deals from topic 24 of the Telegram chat t.me/superpodeshevle (the admin agreed) as the "Qode Deals" account. `tools/deals/ROUTINE.md` is its prompt. `fetch.py` reads new messages with Telethon, Claude turns them into promocodes (codes, including gifts as `FreeItem`) and posts (offers without a code), and `publish.py` checks them against the app's rules and saves them, published right away. `source_url` keeps the message link; it's unique on posts, and promocode ids stop duplicate codes. `ingest_state` holds the last message read and is service-role only.

The routine's environment needs `TG_API_ID`, `TG_API_HASH`, `TG_SESSION`, `SUPABASE_URL`, `SUPABASE_SECRET_KEY` and `DEALS_AUTHOR_ID`. The Telegram values are in Bitwarden ("Qode Telegram API"); `login.py` creates a new session if Telegram logs it out, and `create_author.py` creates the account once per project.

Google's pre-launch robot signs in to prod during review and creates placeholder content, such as a "Test Title" post. Delete its auth user after each review; foreign keys cascade its rows.

`supabase-kt` stays at 3.6.0 because 3.7 and later are built with Kotlin 2.4, and iOS compilation fails on Kotlin 2.3. Keep `:shared:compileKotlinIosSimulatorArm64` passing.

Google sign-in sends a SHA-256 hash of the nonce to Google and the raw nonce to Supabase. The dev web client ID is in `supabase/config.toml`; apply changes with `supabase config push`.

`supabase/config.toml` describes dev, so never run a plain `config push` against prod: it would set the dev client ID and change prod's auth settings. The push only writes declared properties, so make a temporary folder whose `supabase/config.toml` declares just the setting to change, then run `supabase config diff` and `supabase config push` with `--workdir <folder> --project-ref hirfattxysizfprsydyq`. Prod's Google provider uses the `qodein` web client ID, which is also the release default in `androidApp` and `core/ui`. The native ID-token flow needs no client secret.

## New developer setup

A debug build needs only `sdk.dir` in `local.properties`. The Supabase publishable key, web client ID, Logo.dev key and `google-services.json` are public and committed.

- **Google sign-in:** each developer's debug SHA-1 must be added to the `qode-dev` Android app. Get it with `keytool -list -v -keystore ~/.android/debug.keystore -storepass android`, then run `firebase apps:android:sha:create` or use the console.
- **Seed and migrations:** the developer needs access to the Supabase organization and `supabase login` in a real terminal.

Signed release builds also need the signing values in `local.properties`.

## Backups and keep-alive

`supabase-keep-alive.yml` queries both projects so free-tier projects don't pause. `supabase-backup.yml` dumps prod's `public`, `auth` and `storage` schemas daily and keeps them for 14 days as GPG-encrypted artifacts, because artifacts on a public repo can be downloaded by others. Storage files aren't in the dump.

The backup needs two repository secrets: `SUPABASE_PROD_DB_URL`, the session pooler URL from the dashboard's Connect panel, and `BACKUP_PASSPHRASE`. Keep the passphrase somewhere outside GitHub too. To restore, run `gpg --decrypt` on the artifact, then `pg_restore` into the target database.

## Scripts

Run these in `supabase/scripts` after `npm install`. They need `supabase login` and only target `qode-dev`.

- `npm run seed`: fake data. `npm run seed -- --reset` removes it. A re-run skips existing rows, so triggers' counts aren't reset.
- `npm run test:security`: allowed and denied cases for policies, grants, triggers and storage. Run it after any migration.

Run the browse flow with `maestro test .maestro/browse.yaml`. It needs seeded data. Signed-in flows aren't scripted, because Google's sheet needs a person.

The Supabase CLI's `login` needs a real terminal, not Claude Code's `!` prefix.
