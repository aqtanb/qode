# Submission Feature Refactor Plan

## Overview
Complete refactoring of the promo code submission feature to fix authentication issues, improve architecture, and enhance user experience.

## Current Issues
- Duplicate authentication checks causing conflicts
- Connection errors during submission
- Poor separation of concerns
- Authentication gate not properly implemented
- Data flow issues between screen and ViewModel

## Development Phases

### Phase 1: Authentication & Data Layer Foundation
**Goal**: Fix authentication flow and establish solid data foundation

#### 1.1 Authentication Architecture Cleanup
- [ ] Remove duplicate auth checks from ViewModel
- [ ] Implement single authentication gate at screen entry point
- [ ] Ensure authenticated user data flows properly through the system
- [ ] Fix auth state synchronization issues

#### 1.2 Data Model Optimization
- [ ] Review PromoCode model structure (ContentModels.kt)
- [ ] Implement composite document IDs (`lowercased_brandname_promocode` format)
- [ ] Ensure proper denormalization of user fields (`createdBy`, `createdByUsername`, `createdByAvatarUrl`)
- [ ] Verify service logo URL denormalization
- [ ] Study `populatePromocodes.js` for Firebase structure alignment

#### 1.3 Firebase Integration
- [ ] Fix connection issues with Firestore
- [ ] Implement proper error handling for network failures
- [ ] Ensure data validation before submission
- [ ] Add retry mechanisms for failed submissions

### Phase 2: Domain Layer Refactoring
**Goal**: Create clean, testable business logic layer

#### 2.1 Use Cases Restructuring
- [ ] Refactor `CreatePromoCodeUseCase` for better error handling
- [ ] Implement validation use cases for form data
- [ ] Create user data retrieval use cases
- [ ] Add service lookup and validation use cases

#### 2.2 Repository Pattern Enhancement
- [ ] Improve PromoCode repository interface
- [ ] Implement proper caching strategies
- [ ] Add offline support considerations
- [ ] Implement proper data transformation layers

#### 2.3 Domain Models
- [ ] Refine PromoCode domain models
- [ ] Implement proper validation rules
- [ ] Add business rule enforcement
- [ ] Create proper error domain models

### Phase 3: Presentation Layer Architecture
**Goal**: Clean, maintainable UI architecture following MVI + Events pattern

#### 3.1 ViewModel Refactoring
- [ ] Simplify `SubmissionWizardViewModel` responsibilities
- [ ] Remove authentication logic from ViewModel
- [ ] Implement proper state management
- [ ] Add comprehensive error handling
- [ ] Improve loading states and user feedback

#### 3.2 Screen Architecture
- [ ] Implement proper authentication gate at `SubmissionScreen` level
- [ ] Clean up screen composition and responsibilities
- [ ] Improve navigation flow
- [ ] Add proper loading and error states

#### 3.3 State Management
- [ ] Refine `SubmissionWizardUiState` structure
- [ ] Implement proper event handling
- [ ] Add form validation states
- [ ] Improve progress tracking

### Phase 4: UI/UX Enhancement
**Goal**: Create beautiful, intuitive submission experience

#### 4.1 Design System Integration
- [ ] Apply QodeTheme consistently
- [ ] Use proper spacing tokens
- [ ] Implement Material 3 design principles
- [ ] Add proper animations and transitions

#### 4.2 Form Experience
- [ ] Improve step navigation UX
- [ ] Add better form validation feedback
- [ ] Implement auto-save functionality
- [ ] Add progress indicators

#### 4.3 Error Handling UX
- [ ] Design better error states
- [ ] Add contextual help and guidance
- [ ] Implement proper retry mechanisms
- [ ] Add offline state handling

#### 4.4 Authentication UX
- [ ] Improve authentication bottom sheet design
- [ ] Add better onboarding for new users
- [ ] Implement seamless sign-in flow
- [ ] Add proper success feedback

## Technical Specifications

### Authentication Flow
```
User → SubmissionScreen → AuthenticationGate → Form Steps → Submit → Success
                      ↓
                  (if not authenticated)
                      ↓
              AuthenticationBottomSheet → Sign In → Continue to Form
```

### Data Flow
```
Form Data → ViewModel → Use Case → Repository → Firebase
                                      ↓
User Data → Auth State → Denormalized Fields → PromoCode Model
```

### Key Architecture Principles
1. **Single Responsibility**: Each component has one clear purpose
2. **Dependency Inversion**: UI depends on domain, not the reverse
3. **Testability**: All components are easily testable
4. **Error Handling**: Comprehensive error handling at all layers
5. **User Experience**: Smooth, intuitive user journey

## Implementation Strategy

### Phase 1-2 Focus (Immediate)
- Fix authentication and connection issues
- Establish solid data foundation
- Create working submission flow

### Phase 3-4 Focus (Enhancement)
- Improve architecture and maintainability
- Enhance user experience
- Polish UI and interactions

## Success Criteria

### Phase 1 Complete When:
- [ ] No authentication conflicts
- [ ] Successful submissions to Firebase
- [ ] Proper user data flow
- [ ] No connection errors

### Phase 2 Complete When:
- [ ] Clean domain layer
- [ ] Testable business logic
- [ ] Proper error handling
- [ ] Maintainable code structure

### Phase 3 Complete When:
- [ ] Clean presentation layer
- [ ] Proper state management
- [ ] Good separation of concerns
- [ ] Maintainable UI code

### Phase 4 Complete When:
- [ ] Beautiful, intuitive UI
- [ ] Smooth user experience
- [ ] Proper animations
- [ ] Excellent error handling UX

## Next Steps
1. Begin with Phase 1.1 - Authentication cleanup
2. Create detailed implementation plan for Phase 1
3. Execute phases incrementally with testing at each step
4. Gather feedback and iterate on each phase

---

**Note**: Each phase will have its own detailed implementation plan created before execution to ensure proper planning and execution.