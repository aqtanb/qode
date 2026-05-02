# Qode – Promocode Aggregation Platform

> Android app that connects users through promo codes, deals, and community discussions.  
> Built with Jetpack Compose, Firebase, and Clean Architecture for scalability and future Kotlin Multiplatform support.

---

## 🧩 Overview
Qode is a modern Android platform designed for sharing and discovering verified promo codes across popular services in Kazakhstan — from delivery to streaming and retail.  
Users can explore trending deals, upload their own, and engage with a growing community focused on real, verified discounts.

---

## 🛠 Tech Stack
**Language:** Kotlin  
**UI:** Jetpack Compose, Material 3, Navigation  
**Architecture:** MVVM / MVI, Clean Architecture  
**Dependency Injection:** Hilt → Koin migration  
**Async:** Coroutines + Flows, WorkManager  
**Storage:** Room, DataStore  
**Backend:** Firebase (Auth, Firestore, Storage, Functions, Analytics)  
**Other:** Foreground services, Notifications, Multilingual theming  

---

## 🧱 Modules
```
androidApp/
└── Main Android application (Jetpack Compose, Material 3, Navigation, Koin)

core/
├── analytics/ # Firebase + internal logging
├── data/ # Repository and data sources
├── designsystem/ # Reusable Compose components, theming, tokens
├── notifications/ # Push notifications + foreground services
├── testing/ # Shared test utilities and mocks
└── ui/ # Core Compose utilities (snackbars, scaffolds, animations)

feature/
├── auth/ # Firebase Auth integration
├── comment/ # Commenting system for posts & promocodes
├── home/ # Main feed & navigation entry
├── post/ # User posts, uploads, media
├── profile/ # User info, stats
├── promocode/ # Promo code feed, filters, voting
└── settings/ # Preferences, theme, language management

shared/
├── commonMain/ # Shared KMP logic & models
├── commonTest/ # Cross-platform tests
├── iosMain/ # iOS-specific implementation
├── iosSimulatorArm64Main/
├── iosX64Main/
├── jvmMain/ # Android-specific shared logic
└── nativeMain/ # KMP native targets
```

---

## ✨ Key Features
- 🔐 Secure Firebase Authentication
- 💬 Community feed for sharing verified promo codes
- 🔄 Real-time Firestore updates with offline caching
- 📸 Image upload + media integration with Firebase Storage
- 🌙 Adaptive theming (Light/Dark)
- 🌍 Multilingual UI (English / Kazakh / Russian)
- 🧩 Modular architecture ready for future KMP expansion

---

## 🖼 Screenshots
| Discover | Share | Connect | Explore |
|-----------|--------|----------|----------|
| ![Discover](https://github.com/user-attachments/assets/48a64903-7a03-4c79-870f-3f209b812733) | ![Share](https://github.com/user-attachments/assets/d6d3f923-3cd7-429f-9db8-abe62d483cf9) | ![Connect](https://github.com/user-attachments/assets/910d2fed-e127-412f-89d0-c3b4896a00a2) | ![Explore](https://github.com/user-attachments/assets/f560d03f-0a94-4de0-b080-44066e286b80) |

---

## 🌍 SDG Alignment

Qode is aligned with **SDG 12 — Responsible Consumption and Production**.

By aggregating verified, community-curated promotional codes, Qode helps users make more informed purchasing decisions, reduce impulsive spending, and access savings that are typically only available through closed networks or paid newsletters. For users in Kazakhstan, where disposable income varies widely, access to verified deals directly supports more conscious and budget-aware consumption habits.

| SDG | Connection |
|-----|-----------|
| **SDG 12** — Responsible Consumption | Promotes smarter, verified purchasing over impulse buying |
| **SDG 10** — Reduced Inequalities | Makes discount access equal across income levels |
| **SDG 8** — Decent Work & Growth | Supports small businesses reaching customers with low-cost promotion |

---

## 🏛 Digital Public Good Justification

Qode meets the DPG Standard across the following indicators:

| DPG Criterion | Status |
|---|---|
| Relevance to SDGs | ✅ SDG 12, 10, 8 |
| Open license | ✅ GPL-3.0 |
| Clear ownership | ✅ Documented team & contributors |
| Platform independence | ✅ Kotlin Multiplatform — Android + iOS targets |
| No harmful content | ✅ Community moderation + Code of Conduct |
| Data privacy | ✅ PRIVACY_POLICY.md + Firebase data rules |
| Open standards | ✅ REST/Firestore, open-source stack |

The source code is publicly available, the platform is free to use, and community contributions are actively welcomed under open-source governance.

---

## 🚀 How to Use

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Android SDK 26+
- A Firebase project (see `firebase/` directory for config structure)

### Run Locally
```bash
# Clone the repository
git clone https://github.com/aqtanb/qode.git
cd qode

# Open in Android Studio and sync Gradle
# Or build via CLI:
./gradlew :androidApp:assembleDebug
```

### Firebase Setup
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package `com.aqtanb.qode`
3. Download `google-services.json` and place in `androidApp/`
4. Enable Authentication, Firestore, and Storage in the Firebase console

---

## 🤝 How to Contribute

We welcome contributions of all kinds — bug fixes, new features, translations, and documentation improvements.

1. **Fork** the repository
2. **Create a branch**: `git checkout -b feature/your-feature-name`
3. **Make your changes** following our [Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
4. **Test** your changes: `./gradlew test`
5. **Submit a Pull Request** against the `main` branch

Please read [CONTRIBUTING.md](./CONTRIBUTING.MD) for full guidelines and our [Code of Conduct](./CODE_OF_CONDUCT.md) before contributing.

**Good first issues** are labeled [`good first issue`](https://github.com/aqtanb/qode/issues?q=label%3A%22good+first+issue%22) in the Issues tab.

---

## 📄 License

Qode is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

This means:
- ✅ You can freely use, study, and modify the code
- ✅ You can distribute your modified version
- ⚠️ Any derivative work **must also be open-sourced** under GPL-3.0
- ⚠️ You must include the original license and copyright notice

See [LICENSE.md](./LICENSE.md) for the full license text.

---

## 👥 Team

| Name | Role | GitHub | Contributions |
|------|------|--------|--------------|
| **Aktanberdi Ybyraiym** | Lead Developer — architecture, core modules, feature development, Firebase integration | [@aqtanb](https://github.com/aqtanb) | 339 commits |
| **Mubarakuly Magzhan** | Co-Developer — UI components, feature modules, testing | [@hashiroii](https://github.com/hashiroii) | 15 commits |

