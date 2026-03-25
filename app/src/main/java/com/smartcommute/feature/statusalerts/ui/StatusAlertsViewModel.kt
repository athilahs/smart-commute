package com.smartcommute.feature.statusalerts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcommute.core.analytics.StatusAlertsAnalytics
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusAlertsViewModel @Inject constructor(
    private val repository: StatusAlertsRepository,
    private val analytics: StatusAlertsAnalytics
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatusAlertsUiState>(StatusAlertsUiState.Loading)
    val uiState: StateFlow<StatusAlertsUiState> = _uiState.asStateFlow()

    init {
        observeAlarms()
    }

    private fun observeAlarms() {
        viewModelScope.launch {
            repository.observeAllAlarms()
                .catch { exception ->
                    _uiState.value = StatusAlertsUiState.Error(
                        exception.message ?: "Failed to load alarms"
                    )
                }
                .collect { alarms ->
                    val enabledCount = alarms.count { it.isEnabled }
                    _uiState.value = StatusAlertsUiState.Success(
                        alarms = alarms,
                        alarmCount = alarms.size,
                        canCreateMore = enabledCount < 10
                    )
                    analytics.setAlarmUserProperties(
                        alarmCount = alarms.size,
                        hasActiveAlarm = enabledCount > 0
                    )
                }
        }
    }

    fun createAlarm(alarm: StatusAlert) {
        viewModelScope.launch {
            val result = repository.createAlarm(alarm)
            result.onSuccess {
                analytics.logAlarmCreated(
                    lineCount = alarm.selectedTubeLines.size,
                    dayCount = alarm.selectedDays.size,
                    isRecurring = alarm.isRecurring,
                    hour = alarm.time.hour,
                    minute = alarm.time.minute
                )
            }
            result.onFailure { exception ->
                _uiState.value = StatusAlertsUiState.Error(
                    exception.message ?: "Failed to create alarm"
                )
            }
        }
    }

    fun updateAlarm(alarm: StatusAlert) {
        viewModelScope.launch {
            val result = repository.updateAlarm(alarm)
            result.onSuccess {
                analytics.logAlarmUpdated(
                    alarmId = alarm.id,
                    lineCount = alarm.selectedTubeLines.size,
                    dayCount = alarm.selectedDays.size,
                    isRecurring = alarm.isRecurring
                )
            }
            result.onFailure { exception ->
                _uiState.value = StatusAlertsUiState.Error(
                    exception.message ?: "Failed to update alarm"
                )
            }
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            val result = repository.deleteAlarm(alarmId)
            result.onSuccess {
                analytics.logAlarmDeleted(alarmId)
            }
            result.onFailure { exception ->
                _uiState.value = StatusAlertsUiState.Error(
                    exception.message ?: "Failed to delete alarm"
                )
            }
        }
    }

    fun toggleAlarmEnabled(alarmId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val result = if (isEnabled) {
                repository.enableAlarm(alarmId)
            } else {
                repository.disableAlarm(alarmId)
            }
            result.onSuccess {
                analytics.logAlarmToggled(alarmId, isEnabled)
            }
            result.onFailure { exception ->
                _uiState.value = StatusAlertsUiState.Error(
                    exception.message ?: "Failed to toggle alarm"
                )
            }
        }
    }

    fun onCreateAlarmTapped(currentAlarmCount: Int) {
        analytics.logCreateTapped(currentAlarmCount)
    }
}
