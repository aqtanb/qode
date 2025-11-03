# Qode – Promo Code Aggregation Platform

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

## 👨‍💻 Author
**Aktanberdi Ybyraiym**  
