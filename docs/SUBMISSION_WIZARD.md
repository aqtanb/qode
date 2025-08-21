# Submission Wizard System

## Overview
Multi-step submission wizard for creating promo codes with sophisticated UX, form validation, and step-by-step guidance. Features smooth animations, dynamic validation, and type-safe step management following MVI pattern with Events.

## Architecture
- **MVI Pattern**: Wizard state management with SubmissionWizardAction sealed classes
- **Events System**: SubmissionWizardEvent for navigation and completion side effects
- **Step Management**: Type-safe SubmissionWizardStep enum with navigation logic
- **Form Validation**: Real-time validation with step-by-step requirements
- **Animated Transitions**: Smooth step transitions with spring animations
- **Transparent Design**: QodeHeroGradient integration with transparent top bars

## Key Files

### Core Wizard Framework
- `feature/promocode/submission/SubmissionWizardScreen.kt` - Main wizard coordinator with animations
- `feature/promocode/submission/SubmissionWizardViewModel.kt` - MVI ViewModel with step management
- `feature/promocode/submission/SubmissionWizardUiState.kt` - Wizard state and validation logic
- `feature/promocode/submission/SubmissionWizardAction.kt` - User actions for all steps
- `feature/promocode/submission/SubmissionWizardEvent.kt` - Events for navigation and completion
- `feature/promocode/submission/SubmissionWizardStep.kt` - Type-safe step enumeration

### Step Implementations
- `feature/promocode/submission/step1/ServiceAndTypeScreen.kt` - Service selection and promo type
- `feature/promocode/submission/step2/TypeDetailsScreen.kt` - Code and discount details
- `feature/promocode/submission/step3/DateSettingsScreen.kt` - Validity period configuration
- `feature/promocode/submission/step4/OptionalDetailsScreen.kt` - Title, description, and screenshot

### Navigation Integration
- `feature/promocode/navigation/SubmissionNavigation.kt` - Type-safe navigation to wizard

## Wizard Step Architecture

### Step Enumeration
```kotlin
enum class SubmissionWizardStep(val stepNumber: Int) {
    SERVICE_AND_TYPE(1),    // Service name and discount type selection
    TYPE_DETAILS(2),        // Promo code and discount configuration
    DATE_SETTINGS(3),       // Start and end date configuration
    OPTIONAL_DETAILS(4);    // Title, description, screenshot

    val isFirst: Boolean
    val isLast: Boolean
    
    fun next(): SubmissionWizardStep?
    fun previous(): SubmissionWizardStep?
}
```

### Step Navigation Logic
- **Forward Navigation**: Enabled only when current step is valid
- **Backward Navigation**: Always available except on first step
- **Step Validation**: Each step has specific validation requirements
- **Submission**: Only available on last step when all data is valid

## Data Management

### Wizard Data Model
```kotlin
data class SubmissionWizardData(
    // Step 1: Service & Type
    val serviceName: String = "",
    val promoCodeType: PromoCodeType? = null,
    
    // Step 2: Type Details
    val promoCode: String = "",
    val discountPercentage: String = "",
    val discountAmount: String = "",
    val minimumOrderAmount: String = "",
    val isFirstUserOnly: Boolean = false,
    
    // Step 3: Date Settings
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    
    // Step 4: Optional Details
    val title: String = "",
    val description: String = "",
    val screenshotUrl: String? = null
) {
    fun isStep1Valid(): Boolean
    fun isStep2Valid(): Boolean
    fun isStep3Valid(): Boolean
    fun isStep4Valid(): Boolean
    fun canProceedFromStep(step: SubmissionWizardStep): Boolean
}
```

### PromoCode Type System
```kotlin
enum class PromoCodeType {
    PERCENTAGE,     // Percentage-based discount
    FIXED_AMOUNT    // Fixed amount discount
}
```

## MVI Implementation

### UI State Management
```kotlin
sealed interface SubmissionWizardUiState {
    data object Loading : SubmissionWizardUiState
    
    data class Success(
        val currentStep: SubmissionWizardStep,
        val wizardData: SubmissionWizardData,
        val isSubmitting: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap()
    ) : SubmissionWizardUiState {
        val canGoNext: Boolean
        val canGoPrevious: Boolean  
        val canSubmit: Boolean
    }
    
    data class Error(val exception: Throwable) : SubmissionWizardUiState
}
```

### Action System
```kotlin
sealed interface SubmissionWizardAction {
    // Navigation
    data object GoToNextStep : SubmissionWizardAction
    data object GoToPreviousStep : SubmissionWizardAction
    data class GoToStep(val step: SubmissionWizardStep) : SubmissionWizardAction
    
    // Step 1 Actions
    data class UpdateServiceName(val serviceName: String) : SubmissionWizardAction
    data class UpdatePromoCodeType(val type: PromoCodeType) : SubmissionWizardAction
    
    // Step 2 Actions  
    data class UpdatePromoCode(val promoCode: String) : SubmissionWizardAction
    data class UpdateDiscountPercentage(val percentage: String) : SubmissionWizardAction
    data class UpdateDiscountAmount(val amount: String) : SubmissionWizardAction
    data class UpdateMinimumOrderAmount(val amount: String) : SubmissionWizardAction
    data class UpdateFirstUserOnly(val isFirstUserOnly: Boolean) : SubmissionWizardAction
    
    // Step 3 Actions
    data class UpdateStartDate(val date: LocalDate) : SubmissionWizardAction
    data class UpdateEndDate(val date: LocalDate) : SubmissionWizardAction
    
    // Step 4 Actions
    data class UpdateTitle(val title: String) : SubmissionWizardAction
    data class UpdateDescription(val description: String) : SubmissionWizardAction
    data class UpdateScreenshotUrl(val url: String?) : SubmissionWizardAction
    
    // Submission
    data object SubmitPromoCode : SubmissionWizardAction
    data object RetryClicked : SubmissionWizardAction
}
```

### Event System
```kotlin
sealed interface SubmissionWizardEvent {
    data object PromoCodeSubmitted : SubmissionWizardEvent
    data object NavigateBack : SubmissionWizardEvent
}
```

## UI/UX Features

### Visual Design
- **QodeHeroGradient Background**: Gorgeous gradient with floating decorations
- **Transparent Integration**: Seamless gradient integration with transparent components
- **Glass Effects**: Premium loading indicators with glassmorphism
- **Material 3**: Modern design system with QodeTheme integration
- **Design Tokens**: Consistent spacing and sizing using SpacingTokens

### Animation System
```kotlin
AnimatedContent(
    targetState = uiState.currentStep,
    transitionSpec = {
        slideInHorizontally(
            initialOffsetX = { if (targetState.stepNumber > initialState.stepNumber) it else -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn() togetherWith slideOutHorizontally(
            targetOffsetX = { if (targetState.stepNumber > initialState.stepNumber) -it else it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeOut()
    }
)
```

### Progress Indication
- **Animated Progress Bar**: Smooth progress updates with spring animations
- **Step Indicators**: Circular indicators with completion states
- **Dynamic Scaling**: Active step indicators scale with spring animation
- **Color Transitions**: Smooth color changes based on step state
- **Visual Feedback**: Check marks for completed steps

### Interactive Elements
- **Dynamic Navigation Buttons**: Context-aware button text and states
- **Validation Feedback**: Real-time form validation with error messages
- **Loading States**: Elegant loading indicators during submission
- **Back Button**: Glassmorphism back button with proper navigation
- **Form Focus**: Intelligent focus management across steps

## Validation System

### Step-by-Step Validation
```kotlin
// Step 1: Service & Type
fun isStep1Valid(): Boolean = 
    serviceName.isNotBlank() && promoCodeType != null

// Step 2: Type Details  
fun isStep2Valid(): Boolean =
    when (promoCodeType) {
        PromoCodeType.PERCENTAGE -> 
            promoCode.isNotBlank() &&
            discountPercentage.isNotBlank() &&
            minimumOrderAmount.isNotBlank()
        PromoCodeType.FIXED_AMOUNT ->
            promoCode.isNotBlank() &&
            discountAmount.isNotBlank() &&
            minimumOrderAmount.isNotBlank()
        null -> false
    }

// Step 3: Date Settings
fun isStep3Valid(): Boolean = endDate.isAfter(startDate)

// Step 4: Optional Details
fun isStep4Valid(): Boolean = title.isNotBlank()
```

### Dynamic Validation
- **Real-Time Updates**: Validation updates as user types
- **Visual Feedback**: Error states and success indicators
- **Navigation Control**: Next button enabled only when step is valid
- **Error Messages**: Contextual validation error display
- **Business Rules**: Date logic, discount limits, required fields

## Step Implementation Details

### Step 1: Service and Type Selection
- **Service Name Input**: Text field for service/company name
- **Promo Type Selection**: Toggle between Percentage and Fixed Amount
- **Visual Preview**: Shows how the selected type will appear
- **Validation**: Requires both service name and type selection

### Step 2: Type Details Configuration
- **Promo Code Input**: Text field for the actual promo code
- **Discount Configuration**: 
  - Percentage: Discount percentage input
  - Fixed Amount: Discount amount input  
- **Minimum Order Amount**: Required minimum for code usage
- **First User Only**: Toggle for new user restriction
- **Dynamic UI**: Form fields change based on selected type

### Step 3: Date Settings
- **Start Date Picker**: When promo code becomes active
- **End Date Picker**: When promo code expires
- **Date Validation**: End date must be after start date
- **Visual Calendar**: Modern date picker implementation
- **Default Values**: Sensible defaults with future end date

### Step 4: Optional Details
- **Title Input**: User-friendly title for the promo code
- **Description Input**: Optional detailed description
- **Screenshot URL**: Optional image URL for proof/example
- **Preview**: Live preview of how the promo code will appear
- **Completion Check**: Validates minimum required information

## Navigation Integration

### Type-Safe Navigation
```kotlin
@Serializable object SubmissionRoute

fun NavController.navigateToSubmission(navOptions: NavOptions? = null) {
    navigate(route = SubmissionRoute, navOptions = navOptions)
}

fun NavGraphBuilder.submissionSection(
    onNavigateBack: () -> Unit = {},
    onSubmissionComplete: () -> Unit = {}
) {
    composable<SubmissionRoute> {
        SubmissionWizardScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
```

### Event Handling
```kotlin
LaunchedEffect(events) {
    when (events) {
        SubmissionWizardEvent.NavigateBack -> onNavigateBack()
        SubmissionWizardEvent.PromoCodeSubmitted -> {
            // Success feedback, navigate to success screen
        }
        null -> { /* No event */ }
    }
}
```

## Error Handling

### Error States
- **Loading State**: Elegant loading indicator with glass effects
- **Validation Errors**: Field-level error messages and visual indicators
- **Submission Errors**: Network/server error handling with retry options
- **Navigation Errors**: Graceful handling of invalid step transitions

### Recovery Mechanisms
- **Retry Actions**: User can retry failed operations
- **Error Clear**: Validation errors clear when user corrects input
- **Graceful Degradation**: Wizard continues working with partial data
- **Auto-Save**: Form data preserved during navigation

## Testing Strategy

### Unit Tests
- **Step Navigation Logic**: Test step transitions and validation
- **Data Validation**: Test all validation rules and edge cases
- **Action Handling**: Test ViewModel action processing
- **State Transitions**: Test UI state changes

### UI Tests
- **Step Transitions**: Test animated transitions between steps
- **Form Input**: Test all form field interactions
- **Validation Display**: Test error message display
- **Navigation Flow**: Test complete wizard flow

## Development Guidelines

1. **Use Events for Navigation**: All navigation should go through events
2. **Validate Early and Often**: Real-time validation for better UX
3. **Follow MVI Pattern**: Unidirectional data flow for predictable state
4. **Animate Transitions**: Smooth animations for professional feel
5. **Design System First**: Use QodeTheme and design tokens consistently
6. **Transparent Design**: Integrate with gradient backgrounds seamlessly
7. **Type Safety**: Leverage sealed classes and enums for compile-time safety
8. **Progressive Enhancement**: Each step builds on previous step data

This submission wizard provides a world-class user experience for promo code creation with modern Android development patterns and sophisticated UX design.