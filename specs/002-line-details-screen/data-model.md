# Data Model: Line Details Screen

**Feature**: 002-line-details-screen
**Date**: 2025-12-28
**Status**: Complete (Revised - Hybrid Approach)

## Overview

This document defines the data entities, relationships, and validation rules for the Line Details Screen feature. The data model uses a hybrid approach where the main `tube_lines` table (renamed from `line_status`) stores basic line information, with separate normalized tables for disruptions, closures, and crowding data.

## Architectural Decision: Hybrid Normalized Approach

**Rationale**: Combine the benefits of both approaches:
- Rename `line_status` to `tube_lines` for semantic clarity
- Add `headerImageRes` column to main table
- Create separate normalized tables for disruptions, closures, crowding
- Use foreign keys for referential integrity
- Both screens read from same `tube_lines` table
- Line Details Screen additionally queries related tables

**Benefits**:
- Proper normalization (3NF) for complex data
- Referential integrity with CASCADE deletes
- Efficient queries (indexed foreign keys)
- Easy to query disruptions/closures independently
- Type-safe at database level (no JSON parsing errors)

**Trade-offs**:
- Requires joins for complete line details
- More tables to maintain
- Slightly more complex migration

**Decision**: Best balance of data integrity, query performance, and maintainability

## Entity Definitions

### 1. TubeLineEntity (Room Database Entity)

**Purpose**: Stores basic line information for both status list and details views

**Table Name**: `tube_lines` (renamed from `line_status`)

**Schema**:

```kotlin
@Entity(tableName = "tube_lines")
data class TubeLineEntity(
    @PrimaryKey
    val id: String,                        // e.g., "bakerloo", "central"

    // Basic Information
    val name: String,                      // e.g., "Bakerloo Line"
    val modeName: String,                  // Always "tube" for this feature

    // Status Information
    val statusType: String,                // e.g., "Good Service", "Minor Delays"
    val statusDescription: String,         // e.g., "Service operating normally"
    val statusSeverity: Int,               // 0-20 scale (0=good, 20=closed)

    // Visual Assets
    val brandColor: String,                // e.g., "#B36305" (hex color)
    val headerImageRes: String,            // Resource name e.g., "line_header_bakerloo"

    // Metadata
    val lastUpdated: Long,                 // Unix timestamp (milliseconds)
    val cacheExpiry: Long                  // Unix timestamp for cache validity
)
```

**Validation Rules**:
- `id`: Must match TFL line identifier (non-empty, lowercase)
- `name`: Non-empty string, max 50 characters
- `brandColor`: Valid hex color (#RRGGBB format)
- `statusType`: Non-empty string
- `statusSeverity`: Integer in range [0, 20]
- `headerImageRes`: Valid drawable resource name or "placeholder"
- `lastUpdated`: Must be <= current time
- `cacheExpiry`: Must be > lastUpdated

**Relationships**:
- One-to-Many with `DisruptionEntity`
- One-to-Many with `ClosureEntity`
- One-to-One with `CrowdingEntity`

---

### 2. DisruptionEntity (Room Database Entity)

**Purpose**: Stores service disruption details for a specific line

**Table Name**: `disruptions`

**Schema**:

```kotlin
@Entity(
    tableName = "disruptions",
    foreignKeys = [
        ForeignKey(
            entity = TubeLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"])]
)
data class DisruptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineId: String,                    // Foreign key to tube_lines

    // Disruption Details
    val category: String,                  // e.g., "RealTime", "PlannedWork"
    val type: String,                      // e.g., "lineInfo", "lineStatus"
    val categoryDescription: String,       // e.g., "Minor Delays"
    val description: String,               // Full description of disruption
    val closureText: String?,              // Optional additional closure info

    // Affected Stations (stored as comma-separated string)
    val affectedStops: String,             // e.g., "King's Cross,Oxford Circus,Piccadilly Circus"

    // Timing
    val createdDate: Long,                 // Unix timestamp (milliseconds)
    val startDate: Long?,                  // Optional start time
    val endDate: Long?,                    // Optional end time

    // Severity
    val severity: Int                      // 0-20 scale matching statusSeverity
)
```

**Validation Rules**:
- `lineId`: Must reference existing TubeLineEntity
- `category`: Non-empty string
- `description`: Non-empty string, max 2000 characters
- `affectedStops`: Comma-separated station names
- `createdDate`: Must be <= current time
- `severity`: Integer in range [0, 20]
- `startDate` <= `endDate` (if both present)

---

### 3. ClosureEntity (Room Database Entity)

**Purpose**: Stores planned closure information for a specific line

**Table Name**: `closures`

**Schema**:

```kotlin
@Entity(
    tableName = "closures",
    foreignKeys = [
        ForeignKey(
            entity = TubeLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"])]
)
data class ClosureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineId: String,                    // Foreign key to tube_lines

    // Closure Details
    val description: String,               // e.g., "Weekend closure for engineering works"
    val reason: String,                    // e.g., "Planned Engineering Works"

    // Affected Stations (stored as comma-separated string)
    val affectedStations: String,          // e.g., "Edgware Road,Baker Street,Great Portland Street"
    val affectedSegment: String?,          // e.g., "Between Baker Street and King's Cross"

    // Schedule
    val startDate: Long,                   // Unix timestamp (milliseconds)
    val endDate: Long,                     // Unix timestamp (milliseconds)

    // Alternative Service
    val alternativeRoute: String?,         // Optional alternative service description
    val replacementBus: Boolean            // Whether replacement bus service available
)
```

**Validation Rules**:
- `lineId`: Must reference existing TubeLineEntity
- `description`: Non-empty string, max 500 characters
- `affectedStations`: Comma-separated station names (can be empty)
- `startDate`: Must be present
- `endDate`: Must be >= startDate
- `startDate` should be in future or recent past (within last 24 hours)

---

### 4. CrowdingEntity (Room Database Entity)

**Purpose**: Stores current crowding/passenger volume information for a line

**Table Name**: `crowding`

**Schema**:

```kotlin
@Entity(
    tableName = "crowding",
    foreignKeys = [
        ForeignKey(
            entity = TubeLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"], unique = true)]
)
data class CrowdingEntity(
    @PrimaryKey
    val lineId: String,                    // Foreign key to tube_lines (1:1 relationship)

    // Crowding Level
    val level: String,                     // "Quiet", "Moderate", "Busy", "Very Busy"
    val levelCode: Int,                    // 0-3 (0=quiet, 3=very busy)

    // Measurement
    val measurementTime: Long,             // Unix timestamp of measurement
    val dataSource: String,                // e.g., "TfL", "Estimated"

    // Additional Context
    val notes: String?                     // Optional additional info
)
```

**Validation Rules**:
- `lineId`: Must reference existing TubeLineEntity (unique constraint)
- `level`: Must be one of: "Quiet", "Moderate", "Busy", "Very Busy"
- `levelCode`: Integer in range [0, 3]
- `levelCode` must map correctly to `level`:
  - 0 = "Quiet"
  - 1 = "Moderate"
  - 2 = "Busy"
  - 3 = "Very Busy"
- `measurementTime`: Must be <= current time and >= (current time - 1 hour)

---

## Domain Models (UI Layer)

These models are used in the ViewModel and UI layer, mapped from database entities.

### UndergroundLineDetails

**Purpose**: Complete line information for UI display (Line Details Screen)

```kotlin
data class UndergroundLineDetails(
    val lineId: String,
    val lineName: String,
    val brandColor: Color,                 // Converted from hex to Compose Color
    val statusType: String,
    val statusDescription: String,
    val statusSeverity: StatusSeverity,    // Enum: GOOD_SERVICE, MINOR_DELAYS, etc.
    val headerImageRes: String,
    val disruptions: List<Disruption>,
    val closures: List<Closure>,
    val crowding: Crowding?,
    val lastUpdated: LocalDateTime         // Converted from timestamp
)
```

### UndergroundLine (Existing - for Tube Status Screen)

**Purpose**: Basic line information for status list (existing model, minimal changes)

```kotlin
data class UndergroundLine(
    val id: String,
    val name: String,
    val modeName: String,
    val statusType: String,
    val statusDescription: String,
    val statusSeverity: StatusSeverity,
    val lastUpdated: LocalDateTime
)
```

### Disruption

**Purpose**: Simplified disruption for UI display

```kotlin
data class Disruption(
    val id: Long,
    val category: String,
    val description: String,
    val affectedStops: List<String>,       // Parsed from comma-separated string
    val severity: StatusSeverity,
    val timing: DisruptionTiming?
)

data class DisruptionTiming(
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?
)
```

### Closure

**Purpose**: Simplified closure for UI display

```kotlin
data class Closure(
    val id: Long,
    val description: String,
    val reason: String,
    val affectedStations: List<String>,    // Parsed from comma-separated string
    val schedule: ClosureSchedule,
    val alternativeService: AlternativeService?
)

data class ClosureSchedule(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class AlternativeService(
    val route: String?,
    val hasReplacementBus: Boolean
)
```

### Crowding

**Purpose**: Simplified crowding data for UI display

```kotlin
data class Crowding(
    val level: CrowdingLevel,              // Enum: QUIET, MODERATE, BUSY, VERY_BUSY
    val measurementTime: LocalDateTime,
    val notes: String?
)

enum class CrowdingLevel(val displayName: String, val color: Color) {
    QUIET("Quiet", Color.Green),
    MODERATE("Moderate", Color.Yellow),
    BUSY("Busy", Color.Orange),
    VERY_BUSY("Very Busy", Color.Red)
}
```

---

## Enums & Constants

### StatusSeverity

```kotlin
enum class StatusSeverity(val value: Int, val displayName: String) {
    GOOD_SERVICE(0, "Good Service"),
    MINOR_DELAYS(5, "Minor Delays"),
    SEVERE_DELAYS(10, "Severe Delays"),
    PART_SUSPENDED(15, "Part Suspended"),
    SUSPENDED(18, "Suspended"),
    PART_CLOSURE(19, "Part Closure"),
    CLOSED(20, "Closed");

    companion object {
        fun fromValue(value: Int): StatusSeverity {
            return entries.find { it.value == value } ?: GOOD_SERVICE
        }
    }
}
```

---

## Data Mappers

### Entity to Domain Mapping

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/data/mapper/`

Required mapper functions:

```kotlin
// TubeLineMapper.kt (shared mapper)
fun TubeLineEntity.toBasicDomain(): UndergroundLine {
    // Used by Tube Status Screen - maps basic fields only
    return UndergroundLine(
        id = id,
        name = name,
        modeName = modeName,
        statusType = statusType,
        statusDescription = statusDescription,
        statusSeverity = StatusSeverity.fromValue(statusSeverity),
        lastUpdated = Instant.ofEpochMilli(lastUpdated)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}

fun CompleteLineDetails.toDetailedDomain(): UndergroundLineDetails {
    // Used by Line Details Screen - includes all related entities
    return UndergroundLineDetails(
        lineId = line.id,
        lineName = line.name,
        brandColor = parseHexColor(line.brandColor),
        statusType = line.statusType,
        statusDescription = line.statusDescription,
        statusSeverity = StatusSeverity.fromValue(line.statusSeverity),
        headerImageRes = line.headerImageRes,
        disruptions = disruptions.map { it.toDomain() },
        closures = closures.map { it.toDomain() },
        crowding = crowding?.toDomain(),
        lastUpdated = Instant.ofEpochMilli(line.lastUpdated)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )
}

private fun DisruptionEntity.toDomain(): Disruption {
    return Disruption(
        id = id,
        category = category,
        description = description,
        affectedStops = affectedStops.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        severity = StatusSeverity.fromValue(severity),
        timing = if (startDate != null || endDate != null) {
            DisruptionTiming(
                startDate = startDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() },
                endDate = endDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
            )
        } else null
    )
}

private fun ClosureEntity.toDomain(): Closure {
    return Closure(
        id = id,
        description = description,
        reason = reason,
        affectedStations = affectedStations.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        schedule = ClosureSchedule(
            startDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDateTime(),
            endDate = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
        ),
        alternativeService = if (alternativeRoute != null || replacementBus) {
            AlternativeService(route = alternativeRoute, hasReplacementBus = replacementBus)
        } else null
    )
}

private fun CrowdingEntity.toDomain(): Crowding {
    return Crowding(
        level = when (levelCode) {
            0 -> CrowdingLevel.QUIET
            1 -> CrowdingLevel.MODERATE
            2 -> CrowdingLevel.BUSY
            3 -> CrowdingLevel.VERY_BUSY
            else -> CrowdingLevel.MODERATE
        },
        measurementTime = Instant.ofEpochMilli(measurementTime).atZone(ZoneId.systemDefault()).toLocalDateTime(),
        notes = notes
    )
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        Color.Gray  // Fallback color
    }
}
```

---

## Database Access Layer

### TubeLineDao

**Location**: `app/src/main/java/com/smartcommute/feature/linestatus/data/local/dao/TubeLineDao.kt`
(Renamed from LineStatusDao)

```kotlin
@Dao
interface TubeLineDao {
    // Read Operations (used by both screens)
    @Query("SELECT * FROM tube_lines ORDER BY name ASC")
    fun getAllLines(): Flow<List<TubeLineEntity>>

    @Query("SELECT * FROM tube_lines WHERE id = :lineId")
    suspend fun getLineById(lineId: String): TubeLineEntity?

    @Query("SELECT * FROM tube_lines WHERE id = :lineId")
    fun observeLineById(lineId: String): Flow<TubeLineEntity?>

    // Write Operations (called by Tube Status Screen repository)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: TubeLineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<TubeLineEntity>)

    @Query("DELETE FROM tube_lines WHERE id = :lineId")
    suspend fun deleteLine(lineId: String)

    @Query("DELETE FROM tube_lines")
    suspend fun deleteAllLines()

    @Query("SELECT MAX(lastUpdated) FROM tube_lines")
    suspend fun getLastUpdateTime(): Long?
}
```

### LineDetailsDao

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/data/local/dao/LineDetailsDao.kt`

```kotlin
@Dao
interface LineDetailsDao {
    // Disruption queries
    @Query("SELECT * FROM disruptions WHERE lineId = :lineId ORDER BY severity DESC")
    suspend fun getDisruptions(lineId: String): List<DisruptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisruptions(disruptions: List<DisruptionEntity>)

    @Query("DELETE FROM disruptions WHERE lineId = :lineId")
    suspend fun deleteDisruptions(lineId: String)

    // Closure queries
    @Query("SELECT * FROM closures WHERE lineId = :lineId ORDER BY startDate ASC")
    suspend fun getClosures(lineId: String): List<ClosureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosures(closures: List<ClosureEntity>)

    @Query("DELETE FROM closures WHERE lineId = :lineId")
    suspend fun deleteClosures(lineId: String)

    // Crowding queries
    @Query("SELECT * FROM crowding WHERE lineId = :lineId")
    suspend fun getCrowding(lineId: String): CrowdingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrowding(crowding: CrowdingEntity)

    @Query("DELETE FROM crowding WHERE lineId = :lineId")
    suspend fun deleteCrowding(lineId: String)

    // Composite query for complete line details
    @Transaction
    suspend fun getCompleteLineDetails(lineId: String): CompleteLineDetails? {
        val line = tubeLineDao.getLineById(lineId) ?: return null
        return CompleteLineDetails(
            line = line,
            disruptions = getDisruptions(lineId),
            closures = getClosures(lineId),
            crowding = getCrowding(lineId)
        )
    }

    // Injected via constructor
    var tubeLineDao: TubeLineDao
}

/**
 * Data class holding complete line information
 */
data class CompleteLineDetails(
    val line: TubeLineEntity,
    val disruptions: List<DisruptionEntity>,
    val closures: List<ClosureEntity>,
    val crowding: CrowdingEntity?
)
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│ Tube Status Screen                                              │
│  - Fetches TFL API                                              │
│  - Caches to tube_lines + related tables                        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          │ Room Database (Normalized)
                          │
┌─────────────────────────┴───────────────────────────────────────┐
│ Room Database Tables                                            │
│  ├── tube_lines (TubeLineEntity) - Main line info               │
│  ├── disruptions (DisruptionEntity) [FK: lineId]                │
│  ├── closures (ClosureEntity) [FK: lineId]                      │
│  └── crowding (CrowdingEntity) [FK: lineId]                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                    ┌─────┴─────┐
                    │           │
         ┌──────────┴──┐    ┌───┴───────────┐
         │ Line Status │    │ Line Details  │
         │ Repository  │    │ Repository    │
         │             │    │ (+ DAO joins) │
         └──────────┬──┘    └───┬───────────┘
                    │           │
         Maps to    │           │   Maps to
         UndergroundLine        │   UndergroundLineDetails
         (basic)                │   (complete with relations)
                    │           │
         ┌──────────┴──┐    ┌───┴───────────┐
         │ Line Status │    │ Line Details  │
         │ ViewModel   │    │ ViewModel     │
         └──────────┬──┘    └───┬───────────┘
                    │           │
         ┌──────────┴──┐    ┌───┴───────────┐
         │ Line Status │    │ Line Details  │
         │ Screen      │    │ Screen        │
         └─────────────┘    └───────────────┘
```

---

## Migration Strategy

**Database Version**: Increment to 2 (from current version 1)

**Migration Path**:
- Rename `line_status` table to `tube_lines`
- Add `headerImageRes` and `cacheExpiry` columns to `tube_lines`
- Create new tables: `disruptions`, `closures`, `crowding`
- Establish foreign key relationships with CASCADE delete

**Migration SQL**:

```sql
-- Rename main table
ALTER TABLE line_status RENAME TO tube_lines;

-- Add new columns
ALTER TABLE tube_lines ADD COLUMN headerImageRes TEXT NOT NULL DEFAULT 'placeholder';
ALTER TABLE tube_lines ADD COLUMN cacheExpiry INTEGER NOT NULL DEFAULT 0;

-- Create disruptions table
CREATE TABLE IF NOT EXISTS disruptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    lineId TEXT NOT NULL,
    category TEXT NOT NULL,
    type TEXT NOT NULL,
    categoryDescription TEXT NOT NULL,
    description TEXT NOT NULL,
    closureText TEXT,
    affectedStops TEXT NOT NULL,
    createdDate INTEGER NOT NULL,
    startDate INTEGER,
    endDate INTEGER,
    severity INTEGER NOT NULL,
    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_disruptions_lineId ON disruptions(lineId);

-- Create closures table
CREATE TABLE IF NOT EXISTS closures (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    lineId TEXT NOT NULL,
    description TEXT NOT NULL,
    reason TEXT NOT NULL,
    affectedStations TEXT NOT NULL,
    affectedSegment TEXT,
    startDate INTEGER NOT NULL,
    endDate INTEGER NOT NULL,
    alternativeRoute TEXT,
    replacementBus INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_closures_lineId ON closures(lineId);

-- Create crowding table
CREATE TABLE IF NOT EXISTS crowding (
    lineId TEXT PRIMARY KEY NOT NULL,
    level TEXT NOT NULL,
    levelCode INTEGER NOT NULL,
    measurementTime INTEGER NOT NULL,
    dataSource TEXT NOT NULL,
    notes TEXT,
    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
);
```

---

## Validation & Constraints Summary

| Field | Constraint | Enforcement |
|-------|------------|-------------|
| lineId | Non-empty, lowercase | Database NOT NULL + FK |
| name | Max 50 chars | Application layer |
| brandColor | Valid hex (#RRGGBB) | Application layer + regex |
| statusSeverity | Range [0, 20] | Application layer |
| affectedStops/Stations | Comma-separated strings | Application layer |
| Timestamps | <= current time | Application layer |
| Date ranges | start <= end | Application layer |
| Foreign keys | Referential integrity | Database CASCADE |

---

## Testing Considerations (Manual Verification)

Since automated tests are not used (per constitution), manual verification checklist:

- [ ] All entities can be inserted into database
- [ ] Foreign key cascades work correctly (delete line → delete associated data)
- [ ] Comma-separated station parsing handles empty strings and whitespace
- [ ] Timestamp conversions maintain correct timezone
- [ ] Enum mappings handle all possible values from TFL API
- [ ] Mapper functions handle null/optional fields correctly
- [ ] UI displays placeholder when data missing
- [ ] No ANR during database queries (async operations with @Transaction)
- [ ] JOIN queries perform well (verify with Android Profiler)

---

## Summary

This normalized data model provides:
- ✅ Single source of truth (`tube_lines` table for basic info)
- ✅ Proper normalization with separate related tables
- ✅ Referential integrity with CASCADE deletes
- ✅ Type-safe database schema (no JSON parsing)
- ✅ Efficient queries with indexed foreign keys
- ✅ Clear separation between basic and detailed data
- ✅ Supports all requirements from specification (FR-001 through FR-014)
- ✅ Easy to query specific aspects (e.g., all disruptions for analysis)
