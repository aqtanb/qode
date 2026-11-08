# Secrets

What secrets Qode uses, what each is for and where it lives. Values never go in this repo.

## You don't need any to contribute

A debug build needs only `sdk.dir` in `local.properties`. It talks to the Supabase project `qode-dev`, and its publishable key, Google client IDs and `google-services.json` are public and committed. Open a PR from a debug build; releases are built by CI.

If you need access to Supabase, Play Console or GitHub settings, ask the maintainer for a role on that service rather than for a password.

## Release and production

| Secret | Purpose | Where it lives |
|---|---|---|
| Upload keystore (`KEYSTORE_FILE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) | Signs release bundles before upload to Play | GitHub Actions secrets |
| `PLAY_STORE_SERVICE_ACCOUNT` | Lets CI upload to Play internal testing | GitHub Actions secret |
| `LOGO_DEV_PUBLIC_KEY` | Logo.dev key for release builds | GitHub Actions secret |
| `FIREBASE_WEB_CLIENT_ID` | Google web client ID written into `local.properties` by the debug and code-quality workflows. It's public, but kept as a secret | GitHub Actions secret |
| `SUPABASE_PROD_DB_URL` | Session pooler URL with the `qode-prod` database password, used by the daily backup | GitHub Actions secret |
| `BACKUP_PASSPHRASE` | Encrypts and decrypts the daily backup artifacts | GitHub Actions secret |
| `qode-prod` database password | `supabase db push` to prod | Maintainer's password manager |
| Supabase CLI access token | Running the Supabase CLI | Each developer's own login; never shared |

GitHub secrets can be used by workflows but never read back, so every value above also needs a copy in the maintainer's password manager.

## Losing one

- **Upload keystore:** Play App Signing holds the real app signing key, so a lost upload key can be replaced. Request an upload key reset in Play Console → Test and release → App integrity, then update the four keystore secrets.
- **`qode-prod` database password:** reset it in the Supabase dashboard, then update `SUPABASE_PROD_DB_URL`.
- **`BACKUP_PASSPHRASE`:** existing backups can't be decrypted without it. Set a new one so future backups stay usable.
- **Play service account:** create a new key in Google Cloud and update the secret.