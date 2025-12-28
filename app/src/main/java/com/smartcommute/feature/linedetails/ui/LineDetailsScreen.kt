package com.smartcommute.feature.linedetails.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartcommute.feature.linedetails.ui.components.ErrorState
import com.smartcommute.feature.linedetails.ui.components.LineDetailsHeader
import com.smartcommute.feature.linedetails.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailsScreen(
    onNavigateBack: () -> Unit,
    @Suppress("DEPRECATION")
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Line Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is LineDetailsUiState.Loading -> {
                    LoadingState()
                }

                is LineDetailsUiState.Success -> {
                    val lineDetails = state.lineDetails

                    // Header with image, icon, name, and status
                    LineDetailsHeader(
                        lineName = lineDetails.name,
                        statusDescription = lineDetails.status.description,
                        headerImageRes = lineDetails.headerImageRes
                    )

                    // Body section will be added in Phase 4 (User Story 2)
                    // For now, just show the header for MVP
                }

                is LineDetailsUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}
