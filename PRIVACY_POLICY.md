# Privacy Policy

This policy describes how Qode, developed by Ybyraiym Aktanberdi, collects, uses, and shares information.

## 1. Data Controller and Developer Identity
The data controller is **Ybyraiym Aktanberdi**, the independent developer of Qode. The Qode name is used solely as the application's brand name and does not represent a separate legal entity or company. Contact information is provided at the end of this policy.

## 2. Information We Collect

We only collect data necessary to provide and improve the Qode platform.

### A. Personal Identifiable Information (PII)
This data is collected and stored via **Google Firebase Authentication**.

* **Google Account ID/Token:** Used to securely identify your account.
* **Email Address:** Stored for account recovery and identification.
* **Profile Picture URL:** Stored if provided by your Google account.

### B. User-Generated Content (UGC)
This content is stored in **Google Firestore**.

* **Promocodes & Posts:** Data submitted by users, including the code value, service name, dates, descriptions, and accompanying images uploaded by the user.
* **User Interactions:** Data reflecting user voting activity (upvotes/downvotes) on content.
* **User Metadata:** Counts of submitted posts and promocodes.

## 3. How We Use Information

| Category | Purpose | Data Stored |
| :--- | :--- | :--- |
| **Authentication** | To provide secure, password-less login and identify the content author. | Email, Profile Picture, Google ID. |
| **Core Functionality** | To display, search, and manage user-submitted codes and posts. | Promocodes, Posts, Interactions. |
| **Analytics & Crash Reporting** | To monitor app stability and diagnose errors. We rely on standard Firebase functionality, which may collect basic device state data. | Performance Data, Stack Traces, Custom UI Events (e.g., button clicks). |

## 4. Permissions and Device Access

Qode requires the following permissions, which may access user data:

* **Internet Access:** Required for all core functionality, including login, data fetching from Firestore, and sharing links.
* **Camera & Storage:** The app accesses the device's **Camera and Media Storage** solely for the purpose of allowing users to **upload images to accompany their posts.** This data is only accessed when the user actively selects the upload function.

## 5. Third-Party Data Processing

### A. Google Firebase Services
Qode uses the following services, and data is stored on Google’s servers:

* **Firebase Authentication:** Handles login and user identity.
* **Firebase Firestore:** Stores all user-generated content (promocodes, posts) and metadata.
* **Firebase Analytics:** Collects anonymous data on app performance and **custom UI events** to understand usage.
* **Firebase Crashlytics:** Collects stack traces and app performance data when crashes occur, strictly for debugging. **We do not collect explicit Device ID or Advertising ID.**

### B. Algolia
We use **Algolia** for fast, optimized searching of promotional content. Algolia may temporarily process text data (promocode names, service names, post titles) as it indexes the content provided by users.

### C. Data Sale
The developer of Qode **does not sell your personal data** to third parties.

## 6. Advertising

Qode **does not use third-party ad networks (like AdMob)**. However, the app maintains an internal `banners` collection to display developer-controlled promotional content or sponsored messages. **No user data is collected or shared for the purpose of serving these internal banners.**

## 7. Children’s Privacy
Qode is not directed to children under the age of 13. If you are under 13, you should not use this app without verifiable parental consent. If we become aware that we have collected personal data from a child under 13 without verifiable parental consent, we will take steps to remove that information from our servers.

## 8. Contact Information
If you have questions or suggestions about this Privacy Policy or your data, please contact the developer, Ybyraiym Aktanberdi, at:

**qodeinhq@gmail.com**
