# Phase 1: Progress Indicator Component Cleanup

## Current Issues Identified

### 1. **Naming Problems**
- `EnhancedProgressIndicator` → should be just `ProgressIndicator`
- "Enhanced" prefix is meaningless and confusing
- File naming should be consistent and clear

### 2. **Architecture Issues**
- **Validation Logic**: Progress indicator shouldn't handle validation - that belongs in step transition logic
- **Icon Duplication**: Step icons are defined here but used elsewhere - needs centralization
- **Responsibility Confusion**: Progress indicator should only show progress, not validate steps

### 3. **Visual Issues**
- **Random Colors**: Too many colors without clear purpose
- **Inconsistent Coloring**: START_DATE, END_DATE, OPTIONS have different colors for no reason
- **Connection Lines/Dots**: Look weird and misaligned, either make proper lines or remove
- **Random Validation Dots**: Appear under some step names but not others, unclear purpose

### 4. **Code Quality Issues**
- **Hard-coded Strings**: Not translatable, should use string resources
- **Hard-coded Design Values**: Should use Tokens.kt from core design system
- **Duplicate Functionality**: Both progress indicator AND controller show step progress

## Phase 1 Action Plan

### **Task 1: Rename & Clean Structure**
- [ ] Rename `EnhancedProgressIndicator.kt` → `ProgressIndicator.kt`
- [ ] Update all imports and references
- [ ] Clean up component naming within file

### **Task 2: Remove Validation Logic**
- [ ] Remove all step validation logic from progress indicator
- [ ] Remove `StepValidationState` enum and related validation functions
- [ ] Remove validation-based coloring and dots
- [ ] Keep only progress indication functionality

### **Task 3: Centralize Step Icons**
- [ ] Move step icon logic to shared location (maybe `SubmissionWizardStep.kt`)
- [ ] Create centralized step icon mapping
- [ ] Remove icon duplication from progress indicator

### **Task 4: Fix Visual Consistency**
- [ ] Remove random color differences between step types
- [ ] Use consistent coloring: completed = primary, current = primary container, pending = surface variant
- [ ] Fix or remove connection lines/dots between steps
- [ ] Remove validation dots under step names

### **Task 5: Use Design System Properly**
- [ ] Replace hard-coded values with Tokens.kt values
- [ ] Use consistent spacing, colors, and sizing from design system
- [ ] Remove custom animation values, use AnimationTokens

### **Task 6: Make Translatable**
- [ ] Move all hard-coded strings to string resources
- [ ] Add proper string keys for step names and descriptions
- [ ] Ensure RTL support for step progression

### **Task 7: Remove Duplicate Step Indicators**
- [ ] Remove step progress percentage from ModernFloatingController
- [ ] Keep step progress only in ProgressIndicator (that's its job)
- [ ] Clean up controller to focus only on navigation actions

### **Task 8: Code Quality Improvements**
- [ ] Add proper documentation and comments
- [ ] Improve function naming and organization
- [ ] Remove unused code and composables
- [ ] Follow consistent code style

## Expected Outcome

### **What We Keep** ✅
- Swipable/clickable step interaction (you liked this)
- Overall visual structure and layout
- Step navigation functionality
- Clean, modern appearance

### **What We Remove** ❌
- "Enhanced" naming nonsense
- Validation logic (belongs elsewhere)
- Random colors and dots
- Hard-coded strings and values
- Duplicate step indicators in controller
- Confusing connection lines

### **What We Fix** 🔧
- Single responsibility: only show progress
- Consistent coloring system
- Proper design token usage
- Centralized step icon management
- Translation support
- Clean, maintainable code

## Success Criteria

1. **Single Purpose**: Progress indicator only shows progress, nothing else
2. **Clean Naming**: No more "Enhanced" or confusing names
3. **Consistent Colors**: Logical color usage without randomness
4. **Translatable**: All strings in resources
5. **Design System**: Uses proper tokens instead of hard-coded values
6. **No Duplication**: Step indicators only in progress indicator, not controller

## Next Steps After Phase 1

- Phase 2: Service Selector Component
- Phase 3: Controller Button Fixes
- Phase 4: Error Message Strategy

---

**Focus**: Clean up the progress indicator to be a single-purpose, well-architected component that only handles progress indication.