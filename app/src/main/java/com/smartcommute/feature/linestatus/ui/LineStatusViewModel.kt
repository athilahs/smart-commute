package com.smartcommute.feature.linestatus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: LineStatusRepository
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
                        _uiState.value = LineStatusUiState.Success(
                            lines = result.data,
                            lastUpdated = lastUpdated,
                            isOffline = false,
                            isRefreshing = false
                        )
                    }
                    is NetworkResult.Error -> {
                        val currentState = _uiState.value
                        if (currentState is LineStatusUiState.Success) {
                            // Keep showing data but update error state
                            _uiState.value = LineStatusUiState.Error(
                                message = result.message,
                                cachedLines = currentState.lines,
                                lastUpdated = currentState.lastUpdated
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

    fun refreshLineStatuses() {
        val currentState = _uiState.value
        if (currentState is LineStatusUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            repository.refreshLineStatuses()
            fetchLineStatuses()
        }
    }
}
