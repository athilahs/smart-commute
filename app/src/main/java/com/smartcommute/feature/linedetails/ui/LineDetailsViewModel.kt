package com.smartcommute.feature.linedetails.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcommute.feature.linedetails.domain.repository.LineDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LineDetailsViewModel @Inject constructor(
    private val repository: LineDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lineId: String = checkNotNull(savedStateHandle["lineId"]) {
        "LineDetailsViewModel requires lineId argument"
    }

    private val _uiState = MutableStateFlow<LineDetailsUiState>(LineDetailsUiState.Loading)
    val uiState: StateFlow<LineDetailsUiState> = _uiState.asStateFlow()

    init {
        loadLineDetails()
    }

    private fun loadLineDetails() {
        viewModelScope.launch {
            _uiState.value = LineDetailsUiState.Loading

            repository.getLineDetails(lineId).collect { lineDetails ->
                _uiState.value = if (lineDetails != null) {
                    LineDetailsUiState.Success(lineDetails)
                } else {
                    LineDetailsUiState.Error("Line details not found. Please refresh the tube status screen first.")
                }
            }
        }
    }

    fun toggleDisruptionExpansion(disruptionId: Long) {
        val currentState = _uiState.value
        if (currentState is LineDetailsUiState.Success) {
            val expandedDisruptions = currentState.expandedDisruptions.toMutableSet()
            if (expandedDisruptions.contains(disruptionId)) {
                expandedDisruptions.remove(disruptionId)
            } else {
                expandedDisruptions.add(disruptionId)
            }
            _uiState.value = currentState.copy(expandedDisruptions = expandedDisruptions)
        }
    }

    fun toggleClosureExpansion(closureId: Long) {
        val currentState = _uiState.value
        if (currentState is LineDetailsUiState.Success) {
            val expandedClosures = currentState.expandedClosures.toMutableSet()
            if (expandedClosures.contains(closureId)) {
                expandedClosures.remove(closureId)
            } else {
                expandedClosures.add(closureId)
            }
            _uiState.value = currentState.copy(expandedClosures = expandedClosures)
        }
    }

    fun retry() {
        loadLineDetails()
    }
}
