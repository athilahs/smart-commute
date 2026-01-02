package com.smartcommute.feature.statusalerts.ui

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert

sealed class StatusAlertsUiState {
    data object Loading : StatusAlertsUiState()

    data class Success(
        val alarms: List<StatusAlert>,
        val alarmCount: Int = alarms.size,
        val canCreateMore: Boolean = alarmCount < 10
    ) : StatusAlertsUiState()

    data class Error(
        val message: String
    ) : StatusAlertsUiState()
}
