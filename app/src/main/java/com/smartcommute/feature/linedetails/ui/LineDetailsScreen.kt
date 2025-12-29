package com.smartcommute.feature.linedetails.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linedetails.ui.components.ClosureCard
import com.smartcommute.feature.linedetails.ui.components.CrowdingCard
import com.smartcommute.feature.linedetails.ui.components.DisruptionCard
import com.smartcommute.feature.linedetails.ui.components.EmptyStateCard
import com.smartcommute.feature.linedetails.ui.components.ErrorState
import com.smartcommute.feature.linedetails.ui.components.LineDetailsHeader
import com.smartcommute.feature.linedetails.ui.components.LoadingState
import com.smartcommute.feature.linedetails.ui.components.StatusSummaryCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsScreen(
    onNavigateBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    @Suppress("DEPRECATION")
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * Converts status type name to short user-friendly text
     */
    fun getShortStatusText(statusTypeName: String): String {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.line_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
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

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header with image, icon, name, and status with shared element transitions
                        item {
                            val lineColor = remember(lineDetails.id) {
                                when (lineDetails.id.lowercase()) {
                                    "bakerloo" -> line_bakerloo
                                    "central" -> line_central
                                    "circle" -> line_circle
                                    "district" -> line_district
                                    "hammersmith-city" -> line_hammersmith_city
                                    "jubilee" -> line_jubilee
                                    "metropolitan" -> line_metropolitan
                                    "northern" -> line_northern
                                    "piccadilly" -> line_piccadilly
                                    "victoria" -> line_victoria
                                    "waterloo-city" -> line_waterloo_city
                                    else -> androidx.compose.ui.graphics.Color.Gray
                                }
                            }
                            LineDetailsHeader(
                                lineId = lineDetails.id,
                                lineName = lineDetails.name,
                                statusShortText = getShortStatusText(lineDetails.status.type.name),
                                lineColor = lineColor,
                                animatedVisibilityScope = animatedVisibilityScope
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
                                message = stringResource(R.string.empty_state_no_disruptions),
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
                                message = stringResource(R.string.empty_state_no_closures),
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
                                message = stringResource(R.string.empty_state_no_crowding),
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
