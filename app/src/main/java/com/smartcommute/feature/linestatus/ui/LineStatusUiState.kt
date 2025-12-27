package com.smartcommute.feature.linestatus.ui

import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

sealed class LineStatusUiState {
    data object Loading : LineStatusUiState()
    data class Success(
        val lines: List<UndergroundLine>,
        val lastUpdated: Long? = null,
        val isOffline: Boolean = false,
        val isRefreshing: Boolean = false
    ) : LineStatusUiState()
    data class Error(
        val message: String,
        val cachedLines: List<UndergroundLine> = emptyList(),
        val lastUpdated: Long? = null
    ) : LineStatusUiState()
}
