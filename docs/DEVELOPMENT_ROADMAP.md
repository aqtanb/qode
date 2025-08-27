# Development Roadmap

## Critical Issues (Fix First)
- **Filtering System**: First user filter broken, service icons not dynamic, can't combine filters
- **HomeViewModel**: 700+ lines God-class needs refactoring

## Core Features
- **PromoCode System**: Remove title field, add one-time use codes, detail screen, copy/share
- **Comments**: Single-level branching, vote sorting, notifications, achievements integration
- **User Profile**: Edit profile, achievements, karma points, activity history
- **Notifications**: Follow categories/services, social notifications, system alerts

## New Screens
- **Promos Screen**: Replace inbox with store promotions (not promo codes)
- **Feed Screen**: Rename search to social feed with posts, comments, tags
- **Comments Screen**: Dedicated screen for comment threads
- **Country Selection**: Content filtering by country

## UI/UX Improvements
- **Floating Navigation**: Auto-hide FAB and bottom nav on scroll
- **Animations**: Splash screen, screen transitions, micro-interactions
- **Filter Icons**: Dynamic icons based on selection (use service.logoUrl)

## Architecture
- **Move to Shared**: Data module to KMP for iOS prep
- **Hilt → Koin**: Simplify DI, better KMP support
- **Convention Plugins**: Standardize Gradle configs
- **iOS App**: SwiftUI with shared logic

## Infrastructure
- **Testing**: Expand core/testing, unit tests, UI tests
- **Crashlytics**: Production monitoring
- **Logging**: Timber with custom trees
- **Localization**: Kazakh and Russian translations

## Optional
- **Catalog App**: Component showcase (like NiA catalog)
- **Design System**: Improve QodeIcons usage consistency