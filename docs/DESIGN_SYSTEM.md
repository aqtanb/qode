# Design System

## Overview
Comprehensive design system with centralized tokens, gradients, icons, and Material 3 components for consistent UI across the app.

## Architecture
- **Design Tokens**: Semantic spacing, sizing, colors, and motion tokens
- **Component System**: Reusable UI components following Material 3
- **Icon System**: Centralized icon management with QodeIcons
- **Gradient System**: Premium gradients with floating decorations
- **Theme System**: Dark/light theme support with QodeTheme

## Key Files

### Core Design System
- `core/designsystem/theme/Tokens.kt` - All design tokens (spacing, size, shape, motion, etc.)
- `core/designsystem/theme/Theme.kt` - QodeTheme implementation with Material 3
- `core/designsystem/theme/Color.kt` - Color palette and semantic color mapping
- `core/designsystem/theme/Type.kt` - Typography scale and text styles

### Component Library
- `core/designsystem/component/Button.kt` - QodeButton with variants, sizes, loading states
- `core/designsystem/component/Card.kt` - QodeCard with variants and consistent styling
- `core/designsystem/component/TextField.kt` - QodeTextField with validation and variants
- `core/designsystem/component/TopAppBar.kt` - Transparent top app bars
- `core/designsystem/component/BottomNavigation.kt` - Tab navigation component
- `core/designsystem/component/Gradient.kt` - QodeGradient system with decorations
- `core/designsystem/component/Avatar.kt` - User avatar components
- `core/designsystem/component/Chip.kt` - Filter and category chips
- `core/designsystem/component/Dialog.kt` - Modal dialogs and confirmations

### Icon System
- `core/designsystem/icon/QodeIcons.kt` - Centralized icon definitions using Feather/Tabler icons

## Design Tokens

### Spacing System
```kotlin
SpacingTokens.xs      // 4dp
SpacingTokens.sm      // 8dp  
SpacingTokens.md      // 16dp
SpacingTokens.lg      // 24dp
SpacingTokens.xl      // 32dp
SpacingTokens.xxl     // 48dp
SpacingTokens.xxxl    // 64dp

// Semantic spacing
SpacingTokens.Button.horizontalPadding
SpacingTokens.Card.padding
SpacingTokens.TextField.verticalPadding
```

### Size Tokens
```kotlin
SizeTokens.Button.heightMedium     // 40dp
SizeTokens.Icon.sizeMedium         // 20dp
SizeTokens.Avatar.sizeLarge        // 72dp
SizeTokens.minTouchTarget          // 48dp
```

### Motion & Animation
```kotlin
MotionTokens.Duration.FAST         // 150ms
MotionTokens.Scale.PRESSED         // 0.95f
AnimationTokens.Spec.spring       // Natural spring animations
```

## Gradient System

### QodeGradient Components
```kotlin
QodeHeroGradient()        // Primary hero style with decorations
QodePrimaryGradient()     // Primary color gradient
QodeSecondaryGradient()   // Secondary color gradient
QodeTertiaryGradient()    // Tertiary color gradient
```

### Features
- Responsive floating decorations that adapt to screen size
- Smooth vertical gradients with transparency
- Strategic decoration placement avoiding UI elements
- Preview support with BoxWithConstraints

## Component Usage Guidelines

### Always Use Design System Components
```kotlin
// ✅ Correct
QodeButton(
    onClick = { },
    text = stringResource(R.string.action_submit),
    variant = QodeButtonVariant.Primary,
    size = QodeButtonSize.Medium
)

// ❌ Wrong
Button(onClick = { }) {
    Text("Submit")
}
```

### Icon Usage
```kotlin
// ✅ Always use QodeIcons
Icon(
    imageVector = QodeActionIcons.Copy,
    contentDescription = stringResource(R.string.action_copy)
)

// ❌ Never hardcode icons
Icon(imageVector = Icons.Default.Copy)
```

### Spacing Usage
```kotlin
// ✅ Use semantic tokens
modifier = Modifier.padding(SpacingTokens.Card.padding)

// ✅ Use generic tokens sparingly
modifier = Modifier.padding(SpacingTokens.lg)

// ❌ Never hardcode
modifier = Modifier.padding(16.dp)
```

## Theme System

### QodeTheme Usage
```kotlin
QodeTheme {
    // All composables get consistent theming
    YourScreenContent()
}
```

### Color Access
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.primaryContainer
```

## Transparent Top Bars
All feature screens use transparent top bars for seamless gradient integration:

```kotlin
TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent
    )
)
```

## Development Guidelines

1. **Check design system first** - Before creating any UI component, check if it exists in the design system
2. **Use semantic tokens** - Prefer semantic tokens over generic spacing/sizing
3. **Follow Material 3** - All components follow Material 3 design principles
4. **Consistent animations** - Use motion tokens for consistent animation timing
5. **Responsive design** - Components adapt to different screen sizes
6. **Accessibility** - All components include proper semantic properties

## Preview System
- All components include comprehensive previews
- Preview parameter providers for consistent test data
- Dark/light theme preview variants
- Multiple screen size testing

This design system ensures visual consistency, developer productivity, and maintainable UI code across the entire application.