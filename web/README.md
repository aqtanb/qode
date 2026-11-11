# Qode web

The web version of Qode at [qodein.com](https://qodein.com): browse deals and posts, sign in with Google or an emailed code, vote and submit promocodes. Next.js App Router code, built and served by [vinext](https://github.com/cloudflare/vinext) on Cloudflare Workers.

```sh
npm install
npm run dev          # http://localhost:3000, against qode-dev
npm run build        # production build, against qode-prod
npm run preview      # run the built Worker locally
npm run deploy       # deploy to Cloudflare (needs `npx wrangler login`)
npm run lint && npm run typecheck && npm run format:check
```

See `docs/ai/web.md` in the repo root for how it fits with the backend.
