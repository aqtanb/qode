# Privacy Policy for Qode
_Last updated: 29.12.2025_

Qode (“the App”) is developed and operated by **Ybyraiym Aktanberdi** (“Developer”, “we”, “us”).  
By using Qode, you agree to the practices described in this Privacy Policy.

---

## 1. Developer Information
**Data Controller:**  
Ybyraiym Aktanberdi  
Email: **qodeinhq@gmail.com**

Qode is an independently developed application and is not operated by a registered legal entity.

---

## 2. Data We Collect
We collect only the data required to provide core functionality, maintain stability, and understand how the App is used.

### 2.1. Account Information (Firebase Authentication)
When you sign in with Google, we collect:
- Email address
- Google user ID (UID)
- Profile image URL (if available)

This is required to identify authors of submitted content and prevent impersonation.

### 2.2. User-Generated Content (UGC)
Stored in Firebase Firestore and Firebase Storage:
- Promocodes and posts submitted by users
- Titles, descriptions, service names
- Uploaded images
- Voting actions (upvote/downvote)
- Metadata such as post count and promo count
- Timestamps (created/updated)

All UGC published in the App is publicly visible.

Users may submit, edit, or delete their own user-generated content.
Users may report inappropriate or abusive content. Reported content is manually reviewed by the developer and may be removed if it violates App rules or applicable laws.

### 2.3. Analytics Data (Firebase Analytics)
We use Firebase Analytics to understand app usage and improve user experience.

Data collected may include:
- Screen views
- Button taps
- Session duration
- Feature usage
- Device information such as model, OS version, and app version

Firebase Analytics is configured **without Advertising ID collection**.  
No behavioral profiling or ad personalization is performed.

### 2.4. Crash and Performance Data (Firebase Crashlytics)
Crashlytics may collect:
- Device model
- OS version
- App version
- Crash logs and stack traces
- Temporary IP address (routing purposes only)

This data is used solely for debugging and stability improvements.

### 2.5. Search Index Data (Algolia)
Algolia indexes **public** content to enable efficient search:
- Promocode text
- Service names
- Public UGC fields

Algolia does *not* receive personal identifiers such as email or UID.

### 2.6. Content Moderation & User Blocking
**Content Reporting:**
Qode provides mechanisms to report user-generated content that is misleading, abusive, illegal, or otherwise inappropriate.

When you submit a report, we collect:
- Report reason and details
- Reported content ID
- Reporter user ID
- Report timestamp

Reports are manually reviewed by the developer. After a report is resolved (content removed, warning issued, or dismissed), the report data is deleted.

**User Blocking:**
Users may block other users to prevent seeing their content.

When you block a user, we store:
- Blocked user ID
- Block timestamp

Blocked users' content is completely hidden from the blocker. Blocking is reversible at any time.

---

## 3. Advertising & Promotional Content
The App displays **non-personalized promotional banners**.

- Banners may promote Qode features or third-party brands.
- No tracking or personalization is used.
- No advertising networks (e.g., AdMob) are integrated.

**Potential Future Monetization:**
The App may introduce affiliate links to third-party services in the future. Such links will be clearly marked. No personalized tracking will be used for affiliate links.

Because banners are clearly promotional, the App may be labeled **"Contains ads"** on Google Play.

---

## 4. How We Use Data
Data is used to:
- Authenticate users
- Support content submission and management
- Provide search functionality
- Diagnose crashes and performance issues
- Analyze usage patterns to improve features
- Display non-personalized promotional banners
- Prevent abuse and maintain security

We do not sell or rent personal data.

---

## 5. Permissions
### Current Permissions
- **INTERNET** — Required for core functionality
- **POST_NOTIFICATIONS** — Optional notifications

### Future Permissions (not currently active)
The App does not currently request:
- `CAMERA`
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`

These may be added in future versions to support image uploads.  
Permissions will be activated only after explicit user action, and this policy will be updated accordingly.

---

## 6. Data Sharing
We share data only with essential service providers:
- Google Firebase (Auth, Firestore, Storage, Analytics, Crashlytics)
- Algolia (search indexing of public data)

No personal information is shared with advertisers or third-party marketing partners.

---

## 7. Data Retention
We retain:
- Account data until the user deletes their account
- User-generated content until removed by the user or moderated
- Crash logs based on Firebase's retention policy
- Block data until the user unblocks the blocked user
- Report data only until resolved (then deleted)

---

## 8. Account Deletion
Users can permanently delete their account from the Settings screen.

Deleting your account will remove:
- Authentication data (email, Google UID, profile image)
- All user-generated content (posts, promocodes, images)
- Voting history
- User metadata (post count, promo count, etc.)
- Block lists and report history

**This action is permanent and irreversible.**
After deletion, your content will no longer be visible in the App, and your account cannot be recovered.

If you experience issues with account deletion, contact us at **qodeinhq@gmail.com**.

---

## 9. Children’s Privacy
Qode does not target children under 13.  
We do not knowingly collect data from children under 13.  
If such data is discovered, it will be deleted.

---

## 10. App Access Rules
- Browsing public content: no login required
- Posting, voting, uploading images: login required
- No special reviewer credentials needed
- No hidden or restricted sections

---

## 11. Security
We rely on Firebase’s infrastructure for secure data handling:
- Encrypted HTTPS
- Server-enforced Firestore and Storage rules
- Access control via Firebase Authentication

Users must protect their Google account credentials.

---

## 12. Changes to This Policy
We may update this Privacy Policy as needed.  
Updates will always be published at:

**https://aqtanb.github.io/qode/PRIVACY_POLICY.html**

Continued use of the App after updates constitutes acceptance of the revised policy.

---

## 13. Contact
For questions or data concerns, contact:

**qodeinhq@gmail.com**  
Developer: **Ybyraiym Aktanberdi**
