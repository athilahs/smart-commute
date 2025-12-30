# Contract: StatusAlertsRepository

**Feature**: `003-status-alerts-screen` | **Type**: Repository Layer | **Date**: 2025-12-29

## Purpose

Defines the repository interface and implementation for Status Alerts CRUD operations, integrating Room database and AlarmManager scheduling.

---

## Repository Interface

### StatusAlertsRepository

```kotlin
package com.smartcommute.feature.statusalerts.data.repository

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import kotlinx.coroutines.flow.Flow

interface StatusAlertsRepository {
    /**
     * Observe all alarms (enabled + disabled), sorted by time (earliest first)
     * Emits updates whenever alarms are added, updated, or deleted
     */
    fun observeAllAlarms(): Flow<List<StatusAlert>>

    /**
     * Get all enabled alarms (for alarm scheduling after reboot)
     */
    suspend fun getEnabledAlarms(): List<StatusAlert>

    /**
     * Get a single alarm by ID
     * @return StatusAlert if found, null otherwise
     */
    suspend fun getAlarmById(id: String): StatusAlert?

    /**
     * Create a new alarm
     * @throws IllegalStateException if alarm limit (10) is reached
     * @return Result.success(alarm) if created, Result.failure(exception) otherwise
     */
    suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Update an existing alarm
     * @return Result.success(alarm) if updated, Result.failure(exception) if not found
     */
    suspend fun updateAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Delete an alarm by ID
     * @return Result.success(Unit) if deleted, Result.failure(exception) if not found
     */
    suspend fun deleteAlarm(id: String): Result<Unit>

    /**
     * Enable an alarm (sets isEnabled = true and schedules with AlarmManager)
     * @return Result.success(Unit) if enabled, Result.failure(exception) if not found
     */
    suspend fun enableAlarm(id: String): Result<Unit>

    /**
     * Disable an alarm (sets isEnabled = false and cancels from AlarmManager)
     * @return Result.success(Unit) if disabled, Result.failure(exception) if not found
     */
    suspend fun disableAlarm(id: String): Result<Unit>

    /**
     * Get current alarm count (for enforcing 10-alarm limit in UI)
     */
    suspend fun getAlarmCount(): Int
}
```

---

## Repository Implementation

### StatusAlertsRepositoryImpl

```kotlin
package com.smartcommute.feature.statusalerts.data.repository

import android.content.Context
import com.smartcommute.feature.statusalerts.data.local.StatusAlertDao
import com.smartcommute.feature.statusalerts.data.local.toDomain
import com.smartcommute.feature.statusalerts.data.local.toEntity
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatusAlertsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: StatusAlertDao,
    private val alarmScheduler: AlarmScheduler
) : StatusAlertsRepository {

    override fun observeAllAlarms(): Flow<List<StatusAlert>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEnabledAlarms(): List<StatusAlert> {
        return dao.getEnabledAlarms().map { it.toDomain() }
    }

    override suspend fun getAlarmById(id: String): StatusAlert? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert> {
        return try {
            // Enforce 10-alarm limit
            val currentCount = dao.getCount()
            if (currentCount >= 10) {
                return Result.failure(
                    IllegalStateException("Maximum 10 alarms reached. Delete an alarm to create a new one.")
                )
            }

            // Validate alarm
            if (alarm.selectedTubeLines.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException("At least one tube line must be selected")
                )
            }

            // Insert into database
            dao.insert(alarm.toEntity())

            // Schedule if enabled
            if (alarm.isEnabled) {
                alarmScheduler.scheduleAlarm(context, alarm)
            }

            Result.success(alarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAlarm(alarm: StatusAlert): Result<StatusAlert> {
        return try {
            val existing = dao.getById(alarm.id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Validate alarm
            if (alarm.selectedTubeLines.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException("At least one tube line must be selected")
                )
            }

            // Update database
            dao.update(alarm.copy(lastModifiedAt = System.currentTimeMillis()).toEntity())

            // Reschedule if enabled
            if (alarm.isEnabled) {
                alarmScheduler.rescheduleAlarm(context, alarm)
            } else {
                alarmScheduler.cancelAlarm(context, alarm.id)
            }

            Result.success(alarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Cancel scheduled alarm
            alarmScheduler.cancelAlarm(context, id)

            // Delete from database
            dao.deleteById(id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enableAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)?.toDomain()
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Update database
            dao.enable(id)

            // Schedule alarm
            alarmScheduler.scheduleAlarm(context, alarm.copy(isEnabled = true))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disableAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Update database
            dao.disable(id)

            // Cancel scheduled alarm
            alarmScheduler.cancelAlarm(context, id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarmCount(): Int {
        return dao.getCount()
    }
}
```

---

## Hilt Module

### StatusAlertsRepositoryModule

```kotlin
package com.smartcommute.feature.statusalerts.di

import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatusAlertsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStatusAlertsRepository(
        impl: StatusAlertsRepositoryImpl
    ): StatusAlertsRepository
}
```

---

## Error Handling

### Error Types

| Error | Thrown By | HTTP Status Equivalent | User-Facing Message |
|-------|-----------|------------------------|---------------------|
| `IllegalStateException("Maximum 10 alarms reached")` | `createAlarm()` | 400 Bad Request | "You've reached the maximum of 10 alarms. Delete an alarm to create a new one." |
| `IllegalArgumentException("At least one tube line must be selected")` | `createAlarm()`, `updateAlarm()` | 400 Bad Request | "Please select at least one tube line" |
| `IllegalArgumentException("Alarm not found")` | `updateAlarm()`, `deleteAlarm()`, `enableAlarm()`, `disableAlarm()` | 404 Not Found | "Alarm not found" |
| `Exception` (generic) | Any method | 500 Internal Server Error | "An error occurred. Please try again." |

---

## Usage Examples

### Create Alarm

```kotlin
val alarm = StatusAlert(
    id = UUID.randomUUID().toString(),
    time = LocalTime.of(7, 30),
    selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
    selectedTubeLines = setOf("central", "northern"),
    isEnabled = true
)

val result = repository.createAlarm(alarm)
result.onSuccess {
    // Alarm created and scheduled
}.onFailure { exception ->
    // Handle error (e.g., show toast with exception.message)
}
```

### Update Alarm

```kotlin
val updatedAlarm = existingAlarm.copy(
    time = LocalTime.of(8, 0),
    lastModifiedAt = System.currentTimeMillis()
)

val result = repository.updateAlarm(updatedAlarm)
result.onSuccess {
    // Alarm updated and rescheduled
}.onFailure { exception ->
    // Handle error
}
```

### Delete Alarm

```kotlin
val result = repository.deleteAlarm(alarmId)
result.onSuccess {
    // Alarm deleted and canceled
}.onFailure { exception ->
    // Handle error
}
```

### Toggle Alarm

```kotlin
// Enable
repository.enableAlarm(alarmId)

// Disable
repository.disableAlarm(alarmId)
```

### Observe Alarms (LiveData/Flow)

```kotlin
// In ViewModel
val alarms: StateFlow<List<StatusAlert>> = repository.observeAllAlarms()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

---

## Testing Strategy (Manual Verification)

### Test Create Alarm
1. Create alarm via UI
2. Verify alarm appears in list
3. Verify alarm is scheduled (`adb shell dumpsys alarm | grep smartcommute`)

### Test Update Alarm
1. Update alarm time
2. Verify changes persist after app restart
3. Verify alarm is rescheduled

### Test Delete Alarm
1. Delete alarm
2. Verify alarm is removed from list
3. Verify alarm is canceled (`dumpsys alarm` shows no entry)

### Test Enable/Disable
1. Disable alarm
2. Verify alarm is not triggered at scheduled time
3. Enable alarm
4. Verify alarm triggers at scheduled time

### Test 10-Alarm Limit
1. Create 10 alarms
2. Attempt to create 11th alarm
3. Verify error message is shown
4. Delete 1 alarm
5. Verify FAB reappears

---

## Contract Complete

Repository interface, implementation, error handling, and usage patterns fully specified.
