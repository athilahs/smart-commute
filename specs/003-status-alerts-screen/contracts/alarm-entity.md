# Contract: StatusAlert Room Entity

**Feature**: `003-status-alerts-screen` | **Type**: Data Layer | **Date**: 2025-12-29

## Purpose

Defines the Room database contract for persisting user-configured status alerts (alarms) locally.

---

## Entity Definition

### StatusAlertEntity

**Table Name**: `status_alerts`

```kotlin
package com.smartcommute.feature.statusalerts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_alerts")
data class StatusAlertEntity(
    @PrimaryKey val id: String, // UUID string
    val hour: Int, // 0-23
    val minute: Int, // 0-59
    val selectedDaysOfWeek: String, // Comma-separated: "MONDAY,WEDNESDAY" or "" for one-time
    val selectedTubeLines: String, // Comma-separated: "central,northern"
    val isEnabled: Boolean, // true = active, false = disabled
    val createdAt: Long, // Unix timestamp (milliseconds)
    val lastModifiedAt: Long // Unix timestamp (milliseconds)
)
```

---

## DAO Interface

### StatusAlertDao

```kotlin
package com.smartcommute.feature.statusalerts.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusAlertDao {
    /**
     * Observe all alarms, sorted by time (hour ASC, minute ASC)
     */
    @Query("SELECT * FROM status_alerts ORDER BY hour ASC, minute ASC")
    fun observeAll(): Flow<List<StatusAlertEntity>>

    /**
     * Get all enabled alarms (for alarm scheduling after reboot)
     */
    @Query("SELECT * FROM status_alerts WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getEnabledAlarms(): List<StatusAlertEntity>

    /**
     * Get a single alarm by ID
     */
    @Query("SELECT * FROM status_alerts WHERE id = :id")
    suspend fun getById(id: String): StatusAlertEntity?

    /**
     * Insert a new alarm
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alarm: StatusAlertEntity)

    /**
     * Update an existing alarm
     */
    @Update
    suspend fun update(alarm: StatusAlertEntity)

    /**
     * Delete an alarm by ID
     */
    @Query("DELETE FROM status_alerts WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Get total count of alarms (for enforcing 10-alarm limit)
     */
    @Query("SELECT COUNT(*) FROM status_alerts")
    suspend fun getCount(): Int

    /**
     * Enable an alarm (set isEnabled = 1)
     */
    @Query("UPDATE status_alerts SET isEnabled = 1 WHERE id = :id")
    suspend fun enable(id: String)

    /**
     * Disable an alarm (set isEnabled = 0)
     */
    @Query("UPDATE status_alerts SET isEnabled = 0 WHERE id = :id")
    suspend fun disable(id: String)

    /**
     * Delete all alarms (for testing/debugging only - NOT exposed to user)
     */
    @Query("DELETE FROM status_alerts")
    suspend fun deleteAll()
}
```

---

## Database Configuration

### StatusAlertsDatabase

```kotlin
package com.smartcommute.feature.statusalerts.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StatusAlertEntity::class],
    version = 1,
    exportSchema = true
)
abstract class StatusAlertsDatabase : RoomDatabase() {
    abstract fun statusAlertDao(): StatusAlertDao
}
```

**Database Name**: `smart_commute.db` (shared with other features)

---

## Field Specifications

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | String | PRIMARY KEY, NOT NULL | UUID generated on creation |
| `hour` | Int | NOT NULL, 0-23 | Hour of alarm time (24-hour format) |
| `minute` | Int | NOT NULL, 0-59 | Minute of alarm time |
| `selectedDaysOfWeek` | String | NOT NULL | Comma-separated day names (e.g., "MONDAY,FRIDAY") or empty string for one-time |
| `selectedTubeLines` | String | NOT NULL | Comma-separated tube line IDs (e.g., "central,northern") |
| `isEnabled` | Boolean | NOT NULL | true = alarm is active, false = disabled |
| `createdAt` | Long | NOT NULL | Unix timestamp (milliseconds) when alarm was created |
| `lastModifiedAt` | Long | NOT NULL | Unix timestamp (milliseconds) when alarm was last updated |

---

## Indexes

```sql
CREATE INDEX idx_status_alerts_enabled ON status_alerts(isEnabled);
CREATE INDEX idx_status_alerts_created_at ON status_alerts(createdAt);
```

**Rationale**:
- `isEnabled` index: Speeds up queries for enabled alarms (used during boot to reschedule)
- `createdAt` index: Future-proofing for potential sorting/filtering by creation date

---

## Example Data

### Sample Rows

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "hour": 7,
    "minute": 30,
    "selectedDaysOfWeek": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY",
    "selectedTubeLines": "central,northern",
    "isEnabled": true,
    "createdAt": 1704067200000,
    "lastModifiedAt": 1704067200000
  },
  {
    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "hour": 17,
    "minute": 0,
    "selectedDaysOfWeek": "",
    "selectedTubeLines": "victoria",
    "isEnabled": false,
    "createdAt": 1704153600000,
    "lastModifiedAt": 1704240000000
  }
]
```

---

## Validation Rules (Enforced by Repository)

- `selectedTubeLines` MUST NOT be empty
- Maximum 10 rows in table (enforced by `StatusAlertsRepository.createAlarm()`)
- `hour` range: 0-23 (validated before insert)
- `minute` range: 0-59 (validated before insert)

---

## Migration Path

**Current Version**: 1

**Future Migrations** (example):
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE status_alerts ADD COLUMN notificationSound TEXT DEFAULT NULL")
    }
}
```

---

## Testing Queries (Manual Verification)

### Insert Test Alarm
```sql
INSERT INTO status_alerts (id, hour, minute, selectedDaysOfWeek, selectedTubeLines, isEnabled, createdAt, lastModifiedAt)
VALUES ('test-123', 7, 30, 'MONDAY', 'central', 1, 1704067200000, 1704067200000);
```

### Query Enabled Alarms
```sql
SELECT * FROM status_alerts WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC;
```

### Update Alarm
```sql
UPDATE status_alerts SET isEnabled = 0 WHERE id = 'test-123';
```

### Delete Alarm
```sql
DELETE FROM status_alerts WHERE id = 'test-123';
```

---

## Contract Complete

Room entity, DAO, and database configuration fully specified for Status Alerts feature.
