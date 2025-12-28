# Repository Contract: Line Details

**Feature**: 002-line-details-screen
**Date**: 2025-12-28 (Revised - Single Table Approach)
**Type**: Internal Repository Interface

## Overview

This contract defines the interface between the LineDetailsViewModel and the LineDetailsRepository for accessing cached line information. Since this feature operates in offline-only mode (no network requests per FR-007), this contract specifies data access patterns for local database queries only.

**Architectural Note**: Both Tube Status Screen and Line Details Screen read from the same `tube_lines` table. The Line Details Repository adds JSON parsing to extract disruptions, closures, and crowding data from JSON columns.

---

## Repository Interface

### LineDetailsRepository

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/domain/repository/LineDetailsRepository.kt`

```kotlin
interface LineDetailsRepository {
    /**
     * Retrieves complete line details from local cache
     *
     * @param lineId Unique line identifier (e.g., "bakerloo", "central")
     * @return Flow emitting NetworkResult states (Loading, Success, Error)
     *
     * Behavior:
     * - Emits Loading immediately
     * - Queries Room database for cached line details
     * - If found: Emits Success with complete UndergroundLineDetails
     * - If not found: Emits Error("Line details not available offline")
     * - If database error: Emits Error with exception message
     *
     * Performance:
     * - Must complete within 2 seconds (SC-001 requirement)
     * - Uses single transaction for all related data (line, disruptions, closures, crowding)
     * - Returns Flow for reactive updates (future-proof for auto-refresh)
     *
     * Error Cases:
     * - lineId not in database → Error state
     * - Database connection failure → Error state
     * - Corrupted data → Error state with graceful degradation
     */
    fun getLineDetails(lineId: String): Flow<NetworkResult<UndergroundLineDetails>>
}
```

---

## Data Types

### NetworkResult<T>

**Purpose**: Sealed class for representing async operation states

```kotlin
sealed class NetworkResult<out T> {
    data object Loading : NetworkResult<Nothing>()

    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = true  // Always true for this feature
    ) : NetworkResult<T>()

    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()
}
```

### UndergroundLineDetails

**Purpose**: Complete domain model for line information (see data-model.md for full specification)

```kotlin
data class UndergroundLineDetails(
    val lineId: String,
    val lineName: String,
    val brandColor: Color,
    val statusType: String,
    val statusDescription: String,
    val statusSeverity: StatusSeverity,
    val headerImageRes: String,
    val disruptions: List<Disruption>,
    val closures: List<Closure>,
    val crowding: Crowding?,
    val lastUpdated: LocalDateTime
)
```

---

## Implementation Contract

### LineDetailsRepositoryImpl

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/data/repository/LineDetailsRepositoryImpl.kt`

**Dependencies** (injected via Hilt):
- `LineDetailsDao` - Database access
- `LineDetailsMapper` - Entity-to-domain conversion

**Implementation Requirements**:

```kotlin
@Singleton
class LineDetailsRepositoryImpl @Inject constructor(
    private val dao: TubeLineDao,          // Shared DAO (renamed from LineStatusDao)
    private val mapper: TubeLineMapper      // Shared mapper with two functions
) : LineDetailsRepository {

    override fun getLineDetails(lineId: String): Flow<NetworkResult<UndergroundLineDetails>> = flow {
        // 1. Emit Loading state immediately
        emit(NetworkResult.Loading)

        try {
            // 2. Query database for line entity
            val lineEntity = dao.getLineById(lineId)

            // 3. Handle result
            if (lineEntity != null) {
                // Map entity to detailed domain model (includes JSON parsing)
                val domainModel = mapper.toDetailedDomain(lineEntity)

                // Emit Success
                emit(NetworkResult.Success(
                    data = domainModel,
                    isFromCache = true
                ))
            } else {
                // No data found
                emit(NetworkResult.Error(
                    message = "Line details not available offline. Please refresh from tube status screen."
                ))
            }
        } catch (e: Exception) {
            // Database or mapping error (includes JSON parse failures)
            emit(NetworkResult.Error(
                message = "Unable to load line details: ${e.localizedMessage}",
                exception = e
            ))
        }
    }.flowOn(Dispatchers.IO)  // Run on IO dispatcher
}
```

**Constraints**:
- NO network calls allowed (enforced by no Retrofit service injection)
- Must complete within 2 seconds (enforced by simple query + JSON parsing)
- Single source of truth: `tube_lines` table only
- Thread-safe via Flow on IO dispatcher
- JSON parsing errors handled gracefully (returns empty lists/null)

---

## DAO Contract

### TubeLineDao Interface (Shared by both features)

**Location**: `app/src/main/java/com/smartcommute/feature/linestatus/data/local/dao/TubeLineDao.kt`
(Renamed from LineStatusDao)

```kotlin
@Dao
interface TubeLineDao {
    /**
     * Retrieves a single line by ID (used by Line Details Screen)
     *
     * @param lineId Line identifier
     * @return TubeLineEntity or null if not found
     *
     * Performance: Single row query with index on id
     */
    @Query("SELECT * FROM tube_lines WHERE id = :lineId")
    suspend fun getLineById(lineId: String): TubeLineEntity?

    /**
     * Retrieves all lines (used by Tube Status Screen)
     *
     * @return Flow of all lines ordered by name
     */
    @Query("SELECT * FROM tube_lines ORDER BY name ASC")
    fun getAllLines(): Flow<List<TubeLineEntity>>

    /**
     * Insert or update lines (used by Tube Status Screen for caching)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<TubeLineEntity>)
}
```

---

## Mapper Contract

### TubeLineMapper (Shared mapper with two mapping functions)

**Location**: `app/src/main/java/com/smartcommute/feature/linestatus/data/mapper/TubeLineMapper.kt`
(Can be placed in shared location or linestatus feature)

```kotlin
class TubeLineMapper @Inject constructor() {
    /**
     * Maps entity to basic domain model (used by Tube Status Screen)
     * Ignores JSON fields - only maps basic status information
     */
    fun toBasicDomain(entity: TubeLineEntity): UndergroundLine {
        return UndergroundLine(
            id = entity.id,
            name = entity.name,
            modeName = entity.modeName,
            statusType = entity.statusType,
            statusDescription = entity.statusDescription,
            statusSeverity = StatusSeverity.fromValue(entity.statusSeverity),
            lastUpdated = Instant.ofEpochMilli(entity.lastUpdated)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        )
    }

    /**
     * Maps entity to detailed domain model (used by Line Details Screen)
     * Parses JSON fields and includes all information
     */
    fun toDetailedDomain(entity: TubeLineEntity): UndergroundLineDetails {
        return UndergroundLineDetails(
            lineId = entity.id,
            lineName = entity.name,
            brandColor = parseHexColor(entity.brandColor),
            statusType = entity.statusType,
            statusDescription = entity.statusDescription,
            statusSeverity = StatusSeverity.fromValue(entity.statusSeverity),
            headerImageRes = entity.headerImageRes,
            disruptions = parseDisruptionsJson(entity.disruptionsJson),
            closures = parseClosuresJson(entity.closuresJson),
            crowding = parseCrowdingJson(entity.crowdingJson),
            lastUpdated = Instant.ofEpochMilli(entity.lastUpdated)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        )
    }

    private fun parseDisruptionsJson(json: String?): List<Disruption> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val dtos = Json.decodeFromString<List<DisruptionDto>>(json)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseClosuresJson(json: String?): List<Closure> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val dtos = Json.decodeFromString<List<ClosureDto>>(json)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseCrowdingJson(json: String?): Crowding? {
        if (json.isNullOrEmpty()) return null
        return try {
            val dto = Json.decodeFromString<CrowdingDto>(json)
            dto.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseHexColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: IllegalArgumentException) {
            Color.Gray
        }
    }

    // Helper mapping functions for DTOs
    private fun DisruptionDto.toDomain(): Disruption { /* See data-model.md */ }
    private fun ClosureDto.toDomain(): Closure { /* See data-model.md */ }
    private fun CrowdingDto.toDomain(): Crowding { /* See data-model.md */ }
}
```

---

## Error Handling

### Error Scenarios & Responses

| Scenario | Repository Behavior | ViewModel Behavior | UI Display |
|----------|---------------------|--------------------| ------------|
| Line not in cache | Emit Error("Line details not available offline") | Set state to Error | Show error message with "Go back" button |
| Database unavailable | Emit Error("Unable to load line details") | Set state to Error | Show generic error message |
| Corrupted data | Emit Error with exception details | Set state to Error | Show error with technical details (debug builds only) |
| Missing optional fields | Emit Success with null values | Set state to Success | Show "Information not available" for missing sections |
| JSON parse failure | Log error, skip corrupted items | Set state to Success (partial) | Show available data, hide corrupted sections |

---

## Performance Requirements

| Operation | Requirement | Enforcement |
|-----------|-------------|-------------|
| getLineDetails() call | Complete in <2 seconds | Database indexing, optimized queries |
| Database query | Single transaction | @Transaction annotation |
| Thread safety | No main thread blocking | flowOn(Dispatchers.IO) |
| Memory usage | <10MB for largest line | Efficient data structures, no bitmap loading |

---

## Testing Verification Points (Manual)

Since automated tests are not created (per constitution), manual verification checklist:

- [ ] Call `getLineDetails()` with valid lineId → Success state with data
- [ ] Call `getLineDetails()` with invalid lineId → Error state with message
- [ ] Check response time <2 seconds for largest dataset
- [ ] Verify Loading state emitted first
- [ ] Verify database queries run on background thread (no ANR)
- [ ] Test with missing optional fields (crowding) → Success with null
- [ ] Test with no disruptions → Success with empty list
- [ ] Test with corrupted JSON in affectedStops → Graceful degradation
- [ ] Rotate device during load → StateFlow preserves state

---

## Integration Points

### Upstream: Tube Status Screen

**Responsibility**: Populates database with line details including JSON serialization

**Contract**: Must serialize disruptions, closures, and crowding data to JSON and save to `tube_lines` table

**Interface** (extends existing LineStatusRepository):
```kotlin
interface LineStatusRepository {
    // Existing method
    suspend fun refreshLineStatuses(): NetworkResult<List<UndergroundLine>>

    // Implementation note: When caching lines, must also:
    // 1. Serialize disruptions list to JSON string → disruptionsJson
    // 2. Serialize closures list to JSON string → closuresJson
    // 3. Serialize crowding object to JSON string → crowdingJson
    // 4. Set headerImageRes based on line ID (e.g., "line_header_bakerloo")
}
```

**Data Flow**:
1. Tube Status Screen fetches TFL API
2. Maps API response to `TubeLineEntity`
3. Serializes disruptions/closures/crowding to JSON strings using `Json.encodeToString()`
4. Calls `dao.insertLines()` with populated entities including JSON fields
5. Database populated, Line Details Screen can now read and parse JSON

**JSON Serialization Example**:
```kotlin
// In LineStatusRepositoryImpl
private fun mapApiToEntity(apiResponse: LineStatusDto): TubeLineEntity {
    return TubeLineEntity(
        id = apiResponse.id,
        name = apiResponse.name,
        // ... basic fields ...
        disruptionsJson = if (apiResponse.disruptions.isNotEmpty()) {
            Json.encodeToString(apiResponse.disruptions.map { it.toDto() })
        } else null,
        closuresJson = if (apiResponse.closures.isNotEmpty()) {
            Json.encodeToString(apiResponse.closures.map { it.toDto() })
        } else null,
        crowdingJson = apiResponse.crowding?.let {
            Json.encodeToString(it.toDto())
        },
        headerImageRes = "line_header_${apiResponse.id}",  // e.g., "line_header_bakerloo"
        lastUpdated = System.currentTimeMillis(),
        cacheExpiry = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
    )
}
```

### Downstream: LineDetailsViewModel

**Responsibility**: Consumes repository Flow and exposes UI state

**Contract**: Must collect from `getLineDetails()` and map NetworkResult to UiState

```kotlin
@HiltViewModel
class LineDetailsViewModel @Inject constructor(
    private val repository: LineDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lineId: String = checkNotNull(savedStateHandle["lineId"])

    private val _uiState = MutableStateFlow<LineDetailsUiState>(LineDetailsUiState.Loading)
    val uiState: StateFlow<LineDetailsUiState> = _uiState.asStateFlow()

    init {
        loadLineDetails()
    }

    private fun loadLineDetails() {
        viewModelScope.launch {
            repository.getLineDetails(lineId).collect { result ->
                _uiState.value = when (result) {
                    is NetworkResult.Loading -> LineDetailsUiState.Loading
                    is NetworkResult.Success -> LineDetailsUiState.Success(result.data)
                    is NetworkResult.Error -> LineDetailsUiState.Error(result.message)
                }
            }
        }
    }
}
```

---

## Navigation Contract

### Navigation Arguments

**Route Definition**:
```kotlin
@Serializable
data class LineDetailsRoute(val lineId: String)
```

**Usage**:
```kotlin
// From Tube Status Screen:
navController.navigate(LineDetailsRoute(lineId = line.id))

// In Navigation Graph:
composable<LineDetailsRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<LineDetailsRoute>()
    LineDetailsScreen(lineId = route.lineId)
}
```

**Contract**:
- `lineId` must be non-empty string
- `lineId` should match valid TFL line identifier
- `lineId` is passed to ViewModel via SavedStateHandle
- ViewModel retrieves `lineId` with `savedStateHandle["lineId"]`

---

## Dependency Injection Contract

### Hilt Bindings

**Module**: `LineDetailsModule`

**Location**: `app/src/main/java/com/smartcommute/core/di/LineDetailsModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class LineDetailsModule {

    @Binds
    @Singleton
    abstract fun bindLineDetailsRepository(
        impl: LineDetailsRepositoryImpl
    ): LineDetailsRepository
}
```

**Provided Dependencies**:
- `LineDetailsDao` - From DatabaseModule (existing)
- `LineDetailsMapper` - Constructor injection (no binding needed)
- `LineDetailsRepository` - Bound to implementation

---

## Versioning & Compatibility

**Database Schema Version**: 2 (incremented from 1)

**Backward Compatibility**:
- Existing `line_status` table renamed to `tube_lines`
- New JSON columns added (nullable for backward compatibility)
- Tube Status Screen uses same table with mapper that ignores JSON
- App can function with schema v1 (Line Details Screen shows error)
- Graceful degradation if JSON fields are null or malformed

**Migration Path**:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Rename table
        database.execSQL("ALTER TABLE line_status RENAME TO tube_lines")

        // Add new columns for Line Details Screen
        database.execSQL("""
            ALTER TABLE tube_lines
            ADD COLUMN headerImageRes TEXT NOT NULL DEFAULT 'placeholder'
        """)

        database.execSQL("""
            ALTER TABLE tube_lines ADD COLUMN disruptionsJson TEXT
        """)

        database.execSQL("""
            ALTER TABLE tube_lines ADD COLUMN closuresJson TEXT
        """)

        database.execSQL("""
            ALTER TABLE tube_lines ADD COLUMN crowdingJson TEXT
        """)

        database.execSQL("""
            ALTER TABLE tube_lines
            ADD COLUMN cacheExpiry INTEGER NOT NULL DEFAULT 0
        """)

        // Update indices
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_tube_lines_id ON tube_lines(id)
        """)
    }
}
```

---

## Summary

This contract defines:
- ✅ Clear repository interface for offline data access
- ✅ Strict no-network constraint enforcement
- ✅ Performance guarantees (<2 seconds)
- ✅ Error handling patterns
- ✅ Integration points with Tube Status Screen
- ✅ Navigation argument passing
- ✅ Dependency injection setup
- ✅ Database migration strategy
- ✅ Manual testing verification points
