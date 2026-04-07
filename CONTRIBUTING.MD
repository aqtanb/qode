# Contributing to Qode

Thank you for taking the time to contribute to **Qode** — a promo codes aggregation app for the Kazakhstan market built with Kotlin and Jetpack Compose. Every contribution, big or small, is appreciated.

---

## Table of Contents

- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Submitting a Pull Request](#submitting-a-pull-request)
- [Development Guidelines](#development-guidelines)
  - [Project Structure](#project-structure)
  - [Code Style](#code-style)
  - [Commit Messages](#commit-messages)
- [Review Process](#review-process)

---

## Getting Started

1. **Fork** the repository and clone your fork:
   ```bash
   git clone https://github.com/<your-username>/qode.git
   cd qode
   ```

2. Open the project in **Android Studio Hedgehog** or later.

3. Make sure you're using the JDK version configured in the project (`local.properties` or `gradle/wrapper`).

4. Sync Gradle and run the app on an emulator or physical device (API 26+).

---

## How to Contribute

### Reporting Bugs

Before opening a new issue, please search existing issues to avoid duplicates.

When filing a bug report, include:
- A clear and descriptive title
- Steps to reproduce the issue
- Expected vs. actual behavior
- Device / emulator model, Android API level
- Relevant logs or screenshots

### Suggesting Features

Open a **Feature Request** issue with:
- A clear description of the problem your feature solves
- Your proposed solution or idea
- Any relevant examples or mockups

### Submitting a Pull Request

1. Create a new branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. Make your changes following the [Development Guidelines](#development-guidelines).

3. Test your changes on at least one physical device or emulator.

4. Push your branch and open a Pull Request against `main`:
   - Fill out the PR template completely
   - Reference any related issues (e.g. `Closes #42`)
   - Provide screenshots or a short screen recording for UI changes

5. A maintainer will review your PR and may request changes before merging.

---

## Development Guidelines

### Project Structure

The project follows **Clean Architecture** with an **MVI** pattern:

```
app/
├── data/          # Repositories, data sources, models
├── domain/        # Use cases, domain models, repository interfaces
├── presentation/  # ViewModels, UI state, Composable screens
└── di/            # Hilt modules
```

Keep concerns separated — don't let domain logic leak into the UI layer.

### Code Style

- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use **Jetpack Compose** best practices: stateless composables, hoist state to ViewModels.
- All new features should be covered by **unit tests** where applicable.
- Avoid hardcoded strings — use string resources (`strings.xml`).
- Use `@Preview` annotations for new Composable components.
- Run `./gradlew lint` before submitting and fix any warnings.

### Commit Messages

Use the **Conventional Commits** format:

```
<type>(<scope>): <short description>

[optional body]
```

Types: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`

Examples:
```
feat(promo): add sorting by expiry date
fix(auth): resolve null pointer on sign-out
docs(readme): update installation steps
```

---

## Review Process

- PRs are reviewed by maintainers within a few business days.
- At least **one approval** is required before merging.
- All CI checks must pass.
- Maintainers may close stale PRs (no activity for 30 days) with a comment.

---

By contributing, you agree that your contributions will be licensed under the same license as this project. See [`LICENSE.md`](LICENSE.md) for details.
