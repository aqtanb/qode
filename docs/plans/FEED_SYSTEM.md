# Feed System - Development Plan

## Overview

Social content platform where users create posts with tags. Posts are filtered/searched by tags only (NoSQL limitations).

## Phase 1: Core Infrastructure

### Components

**1. Tags Collection** (Copy `FirestoreServiceDataSource` pattern)
- Firestore: `/tags/{tagId}` where tagId = lowercase tag value
- Fields: `postCount: Int`, `createdAt: Timestamp`
- Tag allowed chars: `[a-z0-9_-]` only (from Tag model validation)
- TagRepository: `searchTags()`, `getPopularTags()`
- SearchTagsUseCase, GetPopularTagsUseCase (like services)
- Load popular tags ONLY when filter UI opened (NOT on feed load)

**2. Post Repository**
- FirestorePostDataSource: CREATE and READ only (no update/delete for MVP)
- Methods: `createPost()`, `getPosts()`, `getPostById()`, `getPostsByTags()`
- Returns `Flow<Result<T, OperationError>>` (NOT throw exceptions)
- Sorting: POPULARITY (upvotes DESC), RECENT (createdAt DESC)
- Filtering: Tags only via `whereArrayContains` (NO content search, NO author filter)
- Caching: QueryCache for first page
- Error mapping: IOException→Offline, SecurityException→NotAuthorized, etc.

**3. Use Cases**
- GetPostsUseCase (delegates to repository)
- CreatePostUseCase (validates with `Post.create()`)
- GetPostByIdUseCase (delegates)
- SearchTagsUseCase (delegates to TagRepository)
- GetPopularTagsUseCase (delegates to TagRepository)

**4. DI**
- Bind PostRepository → PostRepositoryImpl
- Bind TagRepository → TagRepositoryImpl
- Auto-inject FirestorePostDataSource, FirestoreTagDataSource

---

## Phase 2: Post Submission

- **ONE screen** (NOT multistep wizard - Reddit/VK don't use wizards)
- Order: title → content → tags (mandatory) → images (optional, 0-5)
- NO review step (redundant, disruptive UX)
- Tag selection: popular tags + search
- Image upload validation

---

## Phase 3: Feed UI

- FeedViewModel with FeedAction/FeedUiState
- PostCard component
- FeedSearchBar with tag chips
- Pull-to-refresh, infinite scroll

---

## Phase 4: Post Details

- PostDetailScreen (separate screen)
- Type-safe navigation: FeedRoute, PostDetailRoute
- Action buttons section
- Comments: SKIP FOR NOW (will add later for both posts & promos)

---

## Phase 5: Advanced

- Real-time updates
- Feed algorithms
- Image caching

---

## Phase 6: Polish

- Analytics
- Error states
- Tests

---

## Data Models

**Post** (exists) - Max 5 images, 10 tags, 200 char title, 2000 char content

**Tag** (exists) - `[a-z0-9_-]`, max 50 chars

**TagData** (new, for Firestore)
- id: String (tag value)
- postCount: Int
- createdAt: Instant

---

## Firestore

```
/posts/{postId}
  - authorId, authorName, authorAvatarUrl
  - title, content, imageUrls[], tags[]
  - upvotes, downvotes, shares, commentCount
  - createdAt, updatedAt

/tags/{tagId}
  - postCount
  - createdAt
```

---

## Indexes

1. `posts`: `votescore DESC, createdAt DESC`
2. `posts`: `createdAt DESC`
3. `posts`: `tags ARRAY_CONTAINS, votescore DESC`
4. `tags`: `postCount DESC`

---

## Error Handling

**PostError:**
- CreationFailure (client validation)
- SubmissionFailure: NotAuthorized, InvalidData (server rejection)
- RetrievalFailure: NotFound, NoResults, AccessDenied

Repository catches exceptions → emit `Result.Error(OperationError)`

---

## Key Constraints

- ✅ Tag-based search ONLY (no content search)
- ✅ No filter by author
- ✅ Tags = separate collection (like services)
- ✅ postCount updated manually/Cloud Functions
- ✅ Popular tags cached 30s, loaded on-demand
- ✅ voteScore computed client-side
- ✅ Post submission: ONE screen, NO wizard
- ✅ Comments: Later phase