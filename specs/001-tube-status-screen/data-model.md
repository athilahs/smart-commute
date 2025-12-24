# Data Model: Tube Status Screen

**Feature**: 001-tube-status-screen
**Date**: 2025-12-24
**Purpose**: Define all data entities, their relationships, validation rules, and state transitions

## Domain Models

### UndergroundLine

**Purpose**: Represents a London Underground line with its current service status

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/domain/model/UndergroundLine.kt`

```kotlin
data class UndergroundLine(
    val id: String,              // e.g., "bakerloo", "central", "circle"
    val name: String,            // e.g., "Bakerloo", "Central", "Circle"
    val status: ServiceStatus,   // Current service status
    val modeName: String = "tube" // Always "tube" for Underground lines
)
```

**Fields**:
- `id`: Unique identifier for the line (matches TfL API line IDs)
  - Validation: Non-empty, lowercase, alphanumeric with hyphens
  - Examples: "bakerloo", "district", "hammersmith-city"
- `name`: Human-readable display name
  - Validation: Non-empty
  - Used as primary text in UI list items
- `status`: Current service status (see ServiceStatus model)
  - Always present (no null)
- `modeName`: Transport mode type
  - Fixed value "tube" for this feature
  - Future-proofing for other transport modes (out of scope for MVP)

**Relationships**:
- Has-one ServiceStatus (composition)
- No relationships to other lines

### ServiceStatus

**Purpose**: Represents the operational status of a transport line

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/domain/model/ServiceStatus.kt`

```kotlin
data class ServiceStatus(
    val type: StatusType,
    val description: String?,    // Optional detailed reason (e.g., "Minor delays due to signal failure")
    val severity: Int            // Numeric severity (0 = good, higher = worse)
)

enum class StatusType(val displayName: String, val severity: Int) {
    GOOD_SERVICE("Good Service", 0),
    MINOR_DELAYS("Minor Delays", 1),
    MAJOR_DELAYS("Major Delays", 2),
    SEVERE_DELAYS("Severe Delays", 3),
    PART_CLOSURE("Part Closure", 4),
    PLANNED_CLOSURE("Planned Closure", 4),
    CLOSURE("Closure", 5),
    SERVICE_DISRUPTION("Service Disruption", 2);  // Fallback for unknown statuses

    companion object {
        fun fromTflStatus(tflStatus: String): StatusType {
            return when (tflStatus.lowercase().replace(" ", "_")) {
                "good_service" -> GOOD_SERVICE
                "minor_delays" -> MINOR_DELAYS
                "major_delays" -> MAJOR_DELAYS
                "severe_delays" -> SEVERE_DELAYS
                "part_closure" -> PART_CLOSURE
                "planned_closure" -> PLANNED_CLOSURE
                "closure" -> CLOSURE
                else -> SERVICE_DISRUPTION  // Unknown status → Service Disruption (per clarification)
            }
        }
    }
}
```

**Fields**:
- `type`: Enum representing status category
  - Validation: Must be one of StatusType enum values
  - Used for UI color coding and icon selection
- `description`: Optional human-readable explanation
  - Validation: Null or non-empty string
  - Displayed in future detail view (out of scope for MVP)
  - Used for accessibility announcements
- `severity`: Numeric severity for sorting/filtering
  - Validation: 0-5 integer
  - Derived from StatusType

**State Transitions**:
- Status can change to any other status (no restrictions)
- Transitions detected by comparing cached vs. fresh API data
- State changes trigger UI updates via StateFlow

## Data Transfer Objects (DTOs)

### LineStatusResponseDto

**Purpose**: Parse TfL API response for line status endpoint

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/remote/dto/LineStatusResponseDto.kt`

```kotlin
data class LineStatusResponseDto(
    val id: String,
    val name: String,
    val modeName: String,
    val lineStatuses: List<LineStatusDto>
)
```

**Source**: TfL API `/Line/Mode/tube/Status` response
**Mapping**: Retrofit auto-parses JSON using Gson/Moshi

### LineStatusDto

**Purpose**: Parse individual status object from TfL API

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/remote/dto/LineStatusDto.kt`

```kotlin
data class LineStatusDto(
    val statusSeverity: Int,
    val statusSeverityDescription: String,
    val reason: String?
)
```

**Fields**:
- `statusSeverity`: TfL's numeric severity code
- `statusSeverityDescription`: TfL's status text (e.g., "Good Service")
- `reason`: Optional description of disruption cause

## Database Entities

### LineStatusEntity

**Purpose**: Room database entity for caching line status data

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/local/entity/LineStatusEntity.kt`

```kotlin
@Entity(tableName = "line_status")
data class LineStatusEntity(
    @PrimaryKey
    val lineId: String,

    val lineName: String,

    val statusType: String,      // StatusType enum name as string

    val statusDescription: String?,

    val statusSeverity: Int,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long,       // Unix timestamp (milliseconds)

    @ColumnInfo(name = "mode_name")
    val modeName: String = "tube"
)
```

**Table**: `line_status`
**Primary Key**: `lineId` (String)
**Indexes**: None required (small dataset, PK sufficient)

**Fields**:
- `lineId`: Unique line identifier (PK)
- `lineName`: Display name
- `statusType`: Enum name stored as string (e.g., "GOOD_SERVICE")
  - Validation: Must match StatusType enum name
- `statusDescription`: Optional disruption reason
- `statusSeverity`: Numeric severity (0-5)
- `lastUpdated`: Timestamp when data was fetched (for "Last updated" UI)
  - Format: Epoch milliseconds (System.currentTimeMillis())
- `modeName`: Transport mode (always "tube")

**Constraints**:
- Primary key ensures no duplicate lines
- `lastUpdated` updated on each successful API fetch

## UI State Models

### LineStatusUiState

**Purpose**: Represent complete UI state for Line Status Screen

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusUiState.kt`

```kotlin
sealed class LineStatusUiState {
    /**
     * Initial loading state (no cached data exists)
     * Displays full-screen loading spinner overlay
     */
    object Loading : LineStatusUiState()

    /**
     * Success state with line data
     * @param lines List of all tube lines with statuses (always 11 lines for London Underground)
     * @param lastUpdated Timestamp of last successful API fetch (null if never fetched)
     * @param isOffline True if currently offline and showing cached data
     * @param isRefreshing True if refresh operation in progress (shows small top indicator)
     * @param apiError True if API temporarily unavailable (shows banner)
     */
    data class Success(
        val lines: List<UndergroundLine>,
        val lastUpdated: Long?,
        val isOffline: Boolean = false,
        val isRefreshing: Boolean = false,
        val apiError: Boolean = false
    ) : LineStatusUiState()

    /**
     * Error state (no cached data available)
     * Displays error message with retry button
     * @param message User-facing error message
     */
    data class Error(
        val message: String
    ) : LineStatusUiState()
}
```

**State Transitions**:
1. **App Launch (no cache)**:
   - `Loading` → API success → `Success(lines, timestamp, isOffline=false)`
   - `Loading` → API failure → `Error(message)`

2. **App Launch (cache exists)**:
   - Immediately emit `Success(cachedLines, cachedTimestamp, isOffline=true, isRefreshing=true)`
   - Background API call → success → `Success(freshLines, newTimestamp, isOffline=false, isRefreshing=false)`
   - Background API call → failure → `Success(cachedLines, cachedTimestamp, isOffline=true, isRefreshing=false, apiError=true)`

3. **Manual Refresh (pull-to-refresh)**:
   - Current state → update to `isRefreshing=true`
   - API success → `Success(freshLines, newTimestamp, isOffline=false, isRefreshing=false, apiError=false)`
   - API failure → Keep previous lines, set `isRefreshing=false, apiError=true or isOffline=true`

## Data Mappers

### LineStatusMapper

**Purpose**: Convert between DTOs, Entities, and Domain Models

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/remote/mapper/LineStatusMapper.kt`

```kotlin
object LineStatusMapper {
    // DTO → Domain
    fun LineStatusResponseDto.toDomainModel(): UndergroundLine {
        val firstStatus = lineStatuses.firstOrNull() ?: throw IllegalStateException("No status found")
        return UndergroundLine(
            id = id,
            name = name,
            status = ServiceStatus(
                type = StatusType.fromTflStatus(firstStatus.statusSeverityDescription),
                description = firstStatus.reason,
                severity = firstStatus.statusSeverity
            ),
            modeName = modeName
        )
    }

    // Domain → Entity
    fun UndergroundLine.toEntity(timestamp: Long): LineStatusEntity {
        return LineStatusEntity(
            lineId = id,
            lineName = name,
            statusType = status.type.name,
            statusDescription = status.description,
            statusSeverity = status.severity,
            lastUpdated = timestamp,
            modeName = modeName
        )
    }

    // Entity → Domain
    fun LineStatusEntity.toDomainModel(): UndergroundLine {
        return UndergroundLine(
            id = lineId,
            name = lineName,
            status = ServiceStatus(
                type = StatusType.valueOf(statusType),
                description = statusDescription,
                severity = statusSeverity
            ),
            modeName = modeName
        )
    }
}
```

**Mapping Rules**:
- DTO → Domain: Take first status from TfL's `lineStatuses` array (typically contains one status)
- Unknown TfL status → Maps to `SERVICE_DISRUPTION` (per spec clarification)
- Domain ↔ Entity: Bidirectional conversion, StatusType stored as string name in database
- Timestamp: Injected during Entity creation, not part of domain model

## Validation Rules Summary

| Entity | Field | Validation Rule |
|--------|-------|----------------|
| UndergroundLine | id | Non-empty, lowercase alphanumeric with hyphens |
| UndergroundLine | name | Non-empty string |
| UndergroundLine | status | Non-null ServiceStatus |
| ServiceStatus | type | Must be valid StatusType enum |
| ServiceStatus | description | Null or non-empty string |
| ServiceStatus | severity | Integer 0-5 |
| LineStatusEntity | lineId | Non-empty (PK constraint) |
| LineStatusEntity | statusType | Must match StatusType enum name exactly |
| LineStatusEntity | lastUpdated | Positive long (timestamp) |

## Data Flow

```
TfL API → LineStatusResponseDto → LineStatusMapper → UndergroundLine → UI
                                         ↓
                                   LineStatusEntity → Room Database

Room Database → LineStatusEntity → LineStatusMapper → UndergroundLine → UI (offline)
```

**Fetch Flow**:
1. Repository calls TflApiService.getLineStatus()
2. Retrofit returns `List<LineStatusResponseDto>`
3. Mapper converts DTO → Domain model
4. Repository saves Domain → Entity to Room
5. Repository emits Domain model via Flow
6. ViewModel transforms to UiState
7. UI observes StateFlow and renders

**Offline Flow**:
1. Repository detects network unavailable (IOException)
2. Repository queries Room for cached entities
3. Mapper converts Entity → Domain model
4. Repository emits Domain models with offline flag
5. ViewModel creates UiState with `isOffline=true`
6. UI displays cached data with "No connection" banner

## Database Schema SQL

```sql
CREATE TABLE line_status (
    lineId TEXT PRIMARY KEY NOT NULL,
    lineName TEXT NOT NULL,
    statusType TEXT NOT NULL,
    statusDescription TEXT,
    statusSeverity INTEGER NOT NULL,
    last_updated INTEGER NOT NULL,
    mode_name TEXT NOT NULL DEFAULT 'tube'
);
```

**Notes**:
- Room auto-generates this schema from `LineStatusEntity` annotations
- SQLite stores timestamps as INTEGER (Long)
- No indexes needed for 11-row table with PK lookups

This data model supports all functional requirements (FR-001 through FR-023) and enables offline-first architecture with graceful error handling as specified in clarifications.
