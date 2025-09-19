# Submission Screen UI Restructuring Plan

## Current Issues & Problems

### Critical Blocking Issues
1. **Discount Type Selection Bug**: Users cannot select discount type, blocking entire submission flow
2. **Service Selector Crashes**: Random crashes and non-functional popular services display
3. **ViewModel State Issues**: Submission doesn't work due to state management problems

### UX/UI Problems
1. **Overwhelming Error Messages**: Validation errors show immediately on screen open instead of on user action
2. **Progress Indicator Issues**:
   - "1 of 7" feels overwhelming
   - Inconsistent coloring for different step types
   - Alignment issues between steps
   - Confusing dots under certain steps
3. **Visual Noise**: Unnecessary shadows on components
4. **Button Layout**: "Previous" button doesn't fit screen, "Complete required fields" text overflow
5. **Legacy Naming**: "Enhanced" prefixes and redundant component names

## Restructuring Plan

### Phase 1: Critical Bug Fixes & Stability ⚡
**Timeline: Week 1 - URGENT**

#### 1.1 Fix Discount Type Selection
- [ ] Debug PromoCodeTypeSelector component state binding
- [ ] Fix validation logic preventing selection
- [ ] Test selection flow end-to-end

#### 1.2 Replace Service Selector
- [ ] Remove broken ServiceSelectionStep implementation
- [ ] Integrate working ServiceSelectorBottomSheet from core/ui
- [ ] Implement search-first UX pattern
- [ ] Add "can't find it? type manually" fallback option

#### 1.3 Fix ViewModel State Management
- [ ] Debug state update issues in SubmissionWizardViewModel
- [ ] Fix service selection state binding
- [ ] Fix discount type state persistence
- [ ] Ensure state consistency across step navigation

### Phase 2: Remove Legacy & Clean Architecture 🧹
**Timeline: Week 2**

#### 2.1 Component Naming Cleanup
- [ ] Rename `EnhancedProgressIndicator` → `ProgressIndicator`
- [ ] Remove "Enhanced" prefixes from all components
- [ ] Standardize naming conventions
- [ ] Remove redundant/unused components

#### 2.2 Error Message Strategy Overhaul
- [ ] Remove immediate validation error display
- [ ] Implement context-aware error showing:
  - Show errors only when user attempts to proceed
  - Show field-specific errors on field blur/focus loss
  - Progressive error disclosure
- [ ] Fix text overflow issues in error messages

### Phase 3: UI/UX Flow Improvements 🎨
**Timeline: Week 2-3**

#### 3.1 Progress Indicator Redesign
- [ ] Remove "X of Y" step counter
- [ ] Implement consistent step coloring across all types
- [ ] Fix step alignment issues (service vs time positioning)
- [ ] Remove confusing validation dots under specific steps
- [ ] Keep swipable step icons (working well)

#### 3.2 Visual Cleanup
- [ ] Remove shadows from FloatingStepCard and ProgressSummary
- [ ] Consistent elevation and shadow usage
- [ ] Clean up color inconsistencies

#### 3.3 Controller Button Fixes
- [ ] Fix "Previous" button layout overflow
- [ ] Implement responsive button sizing
- [ ] Context-aware button text that fits available space
- [ ] Proper button state management

### Phase 4: Polish & Performance 💎
**Timeline: Week 3**

#### 4.1 Spacing & Layout
- [ ] Consistent spacing throughout forms
- [ ] Improve typography hierarchy
- [ ] Responsive layout improvements
- [ ] Better focus states and accessibility

#### 4.2 Progress Summary Optimization
- [ ] Smooth animations (keep existing - working well)
- [ ] Optimize navigation vs inline editing experience
- [ ] Improved visual hierarchy for completed vs pending steps

## Service Selection UX Flow

### New Improved Flow
1. **Primary Action**: Search functionality using ServiceSelectorBottomSheet
2. **Popular Services**: Chips display below search
3. **Secondary Option**: "Can't find your service? Type manually"
4. **State Management**: Single source of truth for selected service

### Removed Confusing Elements
- Dual service selection UI (service + serviceName confusion)
- Broken popular services implementation
- Manual entry immediately marking step as complete

## Success Criteria

### After Phase 1 (Critical Fixes)
✅ User can complete entire submission flow without crashes
✅ Service selection works reliably with search functionality
✅ Discount type selection functions properly
✅ ViewModel state management is stable

### After Phase 2 (Cleanup)
✅ Clean, readable codebase without legacy naming
✅ Error messages only appear when contextually appropriate
✅ No more blocking validation messages on screen open

### After Phase 3 (UX Improvements)
✅ Intuitive progress indicator without overwhelming step counts
✅ Consistent visual design language
✅ Responsive button layout that works on all screen sizes
✅ No visual noise or confusing elements

### After Phase 4 (Polish)
✅ Professional, polished submission experience
✅ Smooth animations and micro-interactions
✅ Optimal spacing and typography
✅ High performance and accessibility compliance

## Implementation Notes

### Quick Wins (Immediate Impact)
- Fix discount type selection bug
- Replace service selector component
- Remove "Enhanced" naming
- Fix error message timing

### Key Architecture Principles
- Single responsibility for each component
- Progressive disclosure of validation errors
- Search-first service selection UX
- Consistent visual design system
- Responsive, mobile-first layout

### Testing Strategy
- Unit tests for critical state management fixes
- Integration tests for complete submission flow
- Manual testing on different screen sizes
- Accessibility testing with screen readers

---
**Status**: Implementation in progress
**Priority**: Critical path for user onboarding and revenue
**Owner**: Development Team
**Last Updated**: Current session