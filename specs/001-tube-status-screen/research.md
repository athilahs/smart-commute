# Research: Tube Status Screen

**Feature**: 001-tube-status-screen
**Date**: 2025-12-24
**Purpose**: Research and document technical decisions for implementing London Underground line status monitoring Android app

## TfL API Integration

### Decision: Use TfL Unified API - Line Status Endpoint

**Rationale**:
- Official Transport for London API provides real-time line status data
- Free tier available with API key registration (sufficient for MVP single-user app)
- RESTful API with JSON responses - well-suited for Retrofit integration
- Documented at: https://api.tfl.gov.uk/
- Endpoint: `GET /Line/Mode/tube/Status` returns all tube line statuses

**API Details**:
- Base URL: `https://api.tfl.gov.uk`
- Authentication: API key via query parameter `app_key` (register at TfL Developer Portal)
- Rate Limits: Free tier allows 500 requests/minute (manual refresh only means well within limits)
- Response format: JSON array of line objects with status information

**Alternatives Considered**:
- Mock/hardcoded data: Rejected - spec specifies real TfL API integration
- Custom backend proxy: Rejected - adds unnecessary complexity for MVP; direct API access simpler
- Web scraping: Rejected - unstable, against TOS, official API available

**Implementation Notes**:
- Store API key in `local.properties` → inject via BuildConfig
- Handle network errors gracefully per spec clarifications
- Cache responses in Room for offline viewing

## Local Data Persistence

### Decision: Room Database with Single Table

**Rationale**:
- Constitution requires Room for structured data persistence
- Simple data model (~11 lines with status) suits single-table approach
- Room provides compile-time SQL verification and lifecycle-aware queries
- Natural Flow/LiveData integration for reactive UI updates

**Schema Design**:
```kotlin
@Entity(tableName = "line_status")
data class LineStatusEntity(
    @PrimaryKey val lineId: String,
    val lineName: String,
    val statusType: String,
    val statusDescription: String?,
    val lastUpdated: Long  // timestamp
)
```

**Alternatives Considered**:
- DataStore: Rejected - designed for key-value preferences, not structured data
- SharedPreferences: Rejected - deprecated for complex data; constitution forbids
- File storage (JSON): Rejected - no query capability, manual serialization overhead

## Jetpack Compose UI Components

### Decision: Material Design 3 with LazyColumn

**Rationale**:
- Constitution requires Material Design 3 and Jetpack Compose
- LazyColumn provides efficient scrolling for list of ~11 items (constitution: lazy loading for lists)
- M3 components (`Card`, `ListItem`, `IconButton`) provide accessible, themed UI out-of-box
- Dark mode support automatic with M3 theme setup

**Key Components**:
- `Scaffold` with `TopAppBar` and `BottomNavigation`
- `PullRefreshIndicator` for manual refresh gesture
- `LazyColumn` with `items()` for line list
- Custom `StatusIndicator` composable for status-specific colors/icons

**Alternatives Considered**:
- RecyclerView: Rejected - Compose preferred per constitution unless legacy constraints
- Custom scroll implementation: Rejected - LazyColumn handles virtualization, accessibility

## Image Loading for Line Logos

### Decision: Glide with Compose Integration

**Rationale**:
- Constitution specifies Glide for efficient image loading/caching
- Line logos are static assets (drawable resources, not remote URLs for MVP)
- Glide's compose integration (`rememberAsyncImagePainter`) provides seamless Compose support
- Automatic memory/disk caching reduces repeated decode overhead

**Logo Source**:
- Tube line logos sourced from TfL brand guidelines or public repositories
- Stored in `app/src/main/res/drawable/` as vector drawables (SVG → Android Vector Drawable)
- Named: `ic_line_{lineid}.xml` (e.g., `ic_line_bakerloo.xml`)

**Alternatives Considered**:
- Coil: Rejected - constitution explicitly specifies Glide
- Manual BitmapFactory: Rejected - no caching, memory management complexity

## Dependency Injection with Hilt

### Decision: Hilt Modules for Network, Database, Repository

**Rationale**:
- Constitution requires Hilt for dependency injection
- Hilt provides compile-time DI with Android lifecycle awareness
- Module organization: NetworkModule (Retrofit, OkHttp), DatabaseModule (Room), RepositoryModule (bindings)
- `@HiltViewModel` for ViewModel injection simplifies testing surface (even though no automated tests)

**Module Structure**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(): Retrofit { ... }

    @Provides @Singleton
    fun provideTflApi(retrofit: Retrofit): TflApiService { ... }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LineStatusDatabase { ... }

    @Provides
    fun provideLineStatusDao(database: LineStatusDatabase): LineStatusDao { ... }
}
```

**Alternatives Considered**:
- Manual DI: Rejected - constitution requires Hilt
- Koin: Rejected - Hilt is constitution-mandated choice

## Navigation Architecture

### Decision: Navigation Compose with Single Activity

**Rationale**:
- Constitution recommends Single Activity architecture
- Navigation Compose provides type-safe navigation for Compose UI
- Bottom navigation with single tab for MVP, easily extensible for future features
- `NavHost` in MainActivity, bottom nav controls destination

**Implementation**:
```kotlin
// NavigationScreen.kt - sealed class for type safety
sealed class NavigationScreen(val route: String) {
    object LineStatus : NavigationScreen("line_status")
    // Future screens added here
}
```

**Alternatives Considered**:
- Multiple activities: Rejected - constitution prefers single activity
- Fragment-based navigation: Rejected - Compose uses Navigation Compose, not fragments

## State Management Pattern

### Decision: StateFlow + Sealed Class UiState

**Rationale**:
- Constitution requires StateFlow for exposing UI state (not LiveData)
- Sealed class pattern for UI state provides type-safe state representation
- Covers loading, success, error states per spec clarifications

**UiState Design**:
```kotlin
sealed class LineStatusUiState {
    object Loading : LineStatusUiState()
    data class Success(
        val lines: List<UndergroundLine>,
        val lastUpdated: Long?,
        val isOffline: Boolean,
        val isRefreshing: Boolean
    ) : LineStatusUiState()
    data class Error(
        val message: String,
        val hasCachedData: Boolean
    ) : LineStatusUiState()
}
```

**Alternatives Considered**:
- LiveData: Rejected - constitution specifies StateFlow
- Multiple separate StateFlows: Rejected - single UiState simpler, atomic updates

## Error Handling Strategy

### Decision: NetworkResult Wrapper + Graceful Degradation

**Rationale**:
- Spec clarifications define specific behaviors for offline/API errors
- `sealed class NetworkResult<T>` wraps API responses with Success/Error/Loading
- Repository layer handles error mapping and cache fallback logic
- ViewModel translates NetworkResult → UiState for presentation

**NetworkResult Design**:
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
```

**Error Behaviors** (from spec clarifications):
- No internet + cache exists → Show cached data with "No connection" banner
- API error + cache exists → Show cached data with "Service temporarily unavailable" banner
- API error + no cache (first launch) → Show error message with retry button

## Build Configuration

### Decision: Gradle Version Catalogs + BuildConfig for API Keys

**Rationale**:
- Constitution requires Gradle version catalogs for dependency management
- Centralized dependency versions in `gradle/libs.versions.toml`
- API key stored in `local.properties` (gitignored), injected via BuildConfig
- Build variants: debug (logging enabled) vs release (R8 obfuscation, no logs)

**Build Variants**:
- Debug: Debuggable, no obfuscation, verbose logging
- Release: R8 obfuscation, ProGuard rules, HTTPS certificate pinning (if needed)

**Alternatives Considered**:
- Hardcoded dependencies in build.gradle: Rejected - version catalogs required by constitution
- API key in source code: Rejected - security vulnerability, constitution forbids

## Accessibility Implementation

### Decision: Content Descriptions + Semantic Properties

**Rationale**:
- Constitution requires TalkBack support and accessibility compliance
- Compose semantics provide built-in accessibility tree
- Each line item needs contentDescription for line name + status
- Status indicators use semantic role annotations

**Implementation Approach**:
```kotlin
// Each list item
Modifier.semantics {
    contentDescription = "${line.name}: ${line.status.type}"
    role = Role.Button  // if tappable for future detail view
}
```

**Alternatives Considered**:
- Manual accessibility events: Rejected - Compose semantics handle automatically
- Skipping accessibility: Rejected - constitution requirement

## Summary of Technical Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Kotlin 1.9+ | Modern Android standard, coroutines support |
| UI Framework | Jetpack Compose | Constitution requirement, declarative UI |
| Architecture | MVVM + Repository | Constitution requirement, clear separation |
| DI | Hilt | Constitution requirement |
| Networking | Retrofit + OkHttp | Industry standard for REST APIs |
| Local DB | Room | Constitution requirement for structured data |
| Async | Coroutines + Flow | Constitution requirement for reactive streams |
| Image Loading | Glide | Constitution requirement |
| Navigation | Navigation Compose | Single Activity pattern |
| State | StateFlow | Constitution requirement (not LiveData) |
| Design System | Material Design 3 | Constitution requirement |

All decisions align with SmartCommute Constitution v1.0.0 and feature specification requirements.
