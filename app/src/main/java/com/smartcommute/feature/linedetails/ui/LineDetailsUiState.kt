package com.smartcommute.feature.linedetails.ui

import com.smartcommute.feature.linedetails.domain.model.UndergroundLineDetails

sealed interface LineDetailsUiState {
    data object Loading : LineDetailsUiState
    data class Success(
        val lineDetails: UndergroundLineDetails,
        val expandedDisruptions: Set<Long> = emptySet(),
        val expandedClosures: Set<Long> = emptySet()
    ) : LineDetailsUiState
    data class Error(val message: String) : LineDetailsUiState
}
