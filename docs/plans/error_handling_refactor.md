# Error Handling Refactor: Bottom-Up Clean Architecture

> **Status**: Active Refactoring
> **Approach**: Bottom-up layer migration with Result<D, E>
> **Breaking Changes**: YES - Complete system overhaul

## Strategy: Proper Layer Dependencies

**Bottom-up refactoring** following clean architecture dependency flow:

1. **Domain Layer** → Domain error enums
2. **Data Layer** → Repositories return Result<D, DomainError>
3. **Domain Use Cases** → Pass through typed Results
4. **Presentation** → Type-safe error handling

## Implementation Plan

### Step 1: Domain Error Enums
Create realistic error enums based on actual operations:
1. PromoCodeError - 16 realistic errors (network, validation, permissions, business logic)
2. UserError - 15 realistic errors (auth, profile, account states)
3. ServiceError - 13 realistic errors (search, cache, business logic)
4. InteractionError - 12 realistic errors (votes, bookmarks, real-time sync)

### Step 2: Repository Layer Migration
- Use Firebase error codes directly (`FirestoreException.code`)
- Return `Result<Data, DomainError>` from repositories
- Map exceptions → domain errors at repository boundary

### Step 3: Use Case Layer Migration
- Use cases return domain-specific: `Result<Data, DomainError>`
- **Eliminate `.asResult()` extension completely**
- Direct Result construction: `Result.Success(data)` / `Result.Error(domainError)`
- No transformation needed - pass through repository Results

### Step 4: Presentation Layer Migration
- ViewModels consume typed `Result<Data, DomainError>`
- UI states hold separate loading boolean + error state
- Type-safe exhaustive when expressions for error handling
- String resource mapping for error display

## Key Decisions
- ✅ **Use cases return domain-specific errors** - `Result<List<PromoCode>, PromoCodeError>`
- ✅ **No Loading in Result** - Handle loading state separately in UI
- ✅ **Delete ErrorMapper completely** - No string parsing
- ✅ **Bottom-up migration** - Domain → Data → UseCase → Presentation
- ✅ **Railway-oriented programming** - Chain operations with map/andThen

*Updated: Drastic refactoring approach - no half measures*
