package com.smartcommute.feature.linedetails.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartcommute.feature.linedetails.ui.components.ClosureCard
import com.smartcommute.feature.linedetails.ui.components.CrowdingCard
import com.smartcommute.feature.linedetails.ui.components.DisruptionCard
import com.smartcommute.feature.linedetails.ui.components.EmptyStateCard
import com.smartcommute.feature.linedetails.ui.components.ErrorState
import com.smartcommute.feature.linedetails.ui.components.LineDetailsHeader
import com.smartcommute.feature.linedetails.ui.components.LoadingState
import com.smartcommute.feature.linedetails.ui.components.StatusSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailsScreen(
    onNavigateBack: () -> Unit,
    @Suppress("DEPRECATION")
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    when (val state = uiState) {
        is LineDetailsUiState.Loading -> {
            Scaffold(
                topBar = {
                    LargeTopAppBar(
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LoadingState()
                }
            }
        }

        is LineDetailsUiState.Success -> {
            val lineDetails = state.lineDetails

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text(lineDetails.name)
                                Text(
                                    text = getShortStatusText(lineDetails.status.type.name),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate back"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with image, icon, name, and status
                    item {
                        LineDetailsHeader(
                            lineName = lineDetails.name,
                            statusShortText = getShortStatusText(lineDetails.status.type.name),
                            headerImageRes = lineDetails.headerImageRes
                        )
                    }

                    // Status Summary Card (with full description)
                    item {
                        StatusSummaryCard(
                            status = lineDetails.status,
                            lastUpdated = lineDetails.lastUpdated,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Disruptions Section
                    if (lineDetails.disruptions.isNotEmpty()) {
                        items(lineDetails.disruptions) { disruption ->
                            DisruptionCard(
                                disruption = disruption,
                                isExpanded = state.expandedDisruptions.contains(disruption.id),
                                onToggleExpand = { viewModel.toggleDisruptionExpansion(disruption.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        item {
                            EmptyStateCard(
                                message = "No disruptions reported",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Closures Section
                    if (lineDetails.closures.isNotEmpty()) {
                        items(lineDetails.closures) { closure ->
                            ClosureCard(
                                closure = closure,
                                isExpanded = state.expandedClosures.contains(closure.id),
                                onToggleExpand = { viewModel.toggleClosureExpansion(closure.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        item {
                            EmptyStateCard(
                                message = "No planned closures",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Crowding Section
                    item {
                        if (lineDetails.crowding != null) {
                            CrowdingCard(
                                crowding = lineDetails.crowding,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            EmptyStateCard(
                                message = "No crowding information available",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        is LineDetailsUiState.Error -> {
            Scaffold(
                topBar = {
                    LargeTopAppBar(
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

/**
 * Converts status type name to short user-friendly text
 */
private fun getShortStatusText(statusTypeName: String): String {
    return when (statusTypeName) {
        "GOOD_SERVICE" -> "Good Service"
        "MINOR_DELAYS" -> "Minor Delays"
        "MAJOR_DELAYS" -> "Major Delays"
        "SEVERE_DELAYS" -> "Severe Delays"
        "CLOSURE" -> "Closed"
        "PART_CLOSURE" -> "Part Closure"
        "SERVICE_DISRUPTION" -> "Service Disruption"
        else -> statusTypeName.replace("_", " ")
    }
}
