---
paths:
  - "supabase/**"
  - "shared/src/commonMain/kotlin/com/qodein/shared/data/**"
---

Read `docs/ai/backend.md` before changing these files.

- Run `npm run test:security` in `supabase/scripts` after any migration, and add allowed and denied cases for new rules.
- New tables need RLS, explicit grants and a `service_role` grant.
- Never put the service role or secret key in the app, tracked files or output.
- Server-owned columns stay out of client grants.
