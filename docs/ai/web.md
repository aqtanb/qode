# Web

Verified at: 13ccef33
Covers: `web`, `.github/workflows/web.yml`

The site at qodein.com lives in `web/`. It's Next.js App Router code (Next 16) built by vinext and deployed as the Cloudflare Worker `qode-web`. `next` stays installed only for `next typegen` (the `PageProps`/`LayoutProps` types) and the bundled docs; don't use `next build`. Cache Components is off because vinext doesn't fully support it.

## Data

It talks to Supabase directly with `supabase-js`; no Kotlin is shared. Server components read with the anon key (`src/lib/queries.ts`), so pages need no session and link previews render server-side. Sign-in, votes and submissions run in the browser (`src/components/auth-context.tsx`), with the session in localStorage.

`.env.development` points at qode-dev and `.env.production` at qode-prod. Both hold only public values (publishable key, Google web client ID) and are committed.

Client rules mirror the Kotlin models: promocode ID and validation in `src/lib/promocode.ts` follow `Promocode.kt` and `Service.kt`. Change both when a rule changes. The database still enforces ownership and server-owned columns.

## Auth

Email codes use the same Supabase OTP flow as Android. Google uses Google Identity Services and `signInWithIdToken` with a hashed nonce, so no client secret is needed, but each origin (`https://qodein.com`, `http://localhost:3000`) must be an Authorized JavaScript origin on the web OAuth client: `qodein` for prod, `qode-dev` for dev. A first sign-in shows the same consent step and creates the `users` row.

## Routes and i18n

Locales are `kk` (default), `ru` and `en`, in the path. `src/proxy.ts` redirects paths without a locale, using the `locale` cookie or `kk`, so the app's share links (`/promocodes/:id`, `/posts/:id`) keep working. Strings live in `src/i18n/dictionaries/`. Dates are formatted by hand in `src/lib/format.ts` because Workers ship limited ICU data.

## Deploy

`npm run deploy` builds with `.env.production` and deploys the Worker. The custom domain `qodein.com` is attached in `wrangler.jsonc` (`routes`). CI (`web.yml`) runs lint, typecheck, Prettier and the build on `web/**` changes; the Android workflows ignore `web/**`.
