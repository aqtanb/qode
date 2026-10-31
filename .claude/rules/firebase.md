---
paths:
  - "firestore.rules"
  - "firebase/**"
  - "functions/src/**"
  - "core/data/src/main/java/com/qodein/core/data/dto/**"
---

Read `docs/ai/backend.md` before changing these files.

- Test rule changes in the emulator, covering both allowed and denied cases.
- Old app versions keep reading production data, so check the rollout order for schema changes.
- A DTO field name must match the Firestore field exactly, including case.
- Admin SDK writes bypass rules, so server-owned fields stay blocked for clients.
