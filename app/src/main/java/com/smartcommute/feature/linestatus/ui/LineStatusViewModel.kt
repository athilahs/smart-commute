package com.smartcommute.feature.linestatus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcommute.core.analytics.TubeStatusAnalytics
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LineStatusViewModel @Inject constructor(
    private val repository: LineStatusRepository,
    private val analytics: TubeStatusAnalytics
) : ViewModel() {

    private val _uiState = MutableStateFlow<LineStatusUiState>(LineStatusUiState.Loading)
    val uiState: StateFlow<LineStatusUiState> = _uiState.asStateFlow()

    init {
        fetchLineStatuses()
    }

    fun fetchLineStatuses() {
        viewModelScope.launch {
            repository.getLineStatuses().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        if (_uiState.value !is LineStatusUiState.Success) {
                            _uiState.value = LineStatusUiState.Loading
                        }
                    }
                    is NetworkResult.Success -> {
                        val lastUpdated = repository.getLastUpdateTime()
                        val previousState = _uiState.value
                        val trigger = (previousState as? LineStatusUiState.Success)?.refreshTrigger

                        _uiState.value = LineStatusUiState.Success(
                            lines = result.data,
                            lastUpdated = lastUpdated,
                            isOffline = false,
                            isRefreshing = false
                        )

                        analytics.logLoaded(
                            lineCount = result.data.size,
                            source = "network",
                            isOffline = false
                        )
                        if (trigger != null) {
                            analytics.logRefresh(trigger = trigger, result = "success")
                        }
                    }
                    is NetworkResult.Error -> {
                        val previousState = _uiState.value
                        val trigger = (previousState as? LineStatusUiState.Success)?.refreshTrigger

                        analytics.logError(result.message)
                        if (trigger != null) {
                            analytics.logRefresh(trigger = trigger, result = "error")
                        }

                        if (previousState is LineStatusUiState.Success) {
                            _uiState.value = LineStatusUiState.Error(
                                message = result.message,
                                cachedLines = previousState.lines,
                                lastUpdated = previousState.lastUpdated
                            )
                        } else {
                            _uiState.value = LineStatusUiState.Error(
                                message = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun refreshLineStatuses(trigger: String) {
        val currentState = _uiState.value
        if (currentState is LineStatusUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true, refreshTrigger = trigger)
        }

        viewModelScope.launch {
            repository.refreshLineStatuses()
            fetchLineStatuses()
        }
    }

    fun onLineSelected(lineId: String, lineName: String, statusType: String) {
        analytics.logLineSelected(lineId, lineName, statusType)
    }

    fun onRetryTapped() {
        analytics.logRetryTapped()
        fetchLineStatuses()
    }
}
