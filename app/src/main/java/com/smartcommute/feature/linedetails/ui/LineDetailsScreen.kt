package com.smartcommute.feature.linedetails.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linedetails.ui.components.CrowdingInformationCard
import com.smartcommute.feature.linedetails.ui.components.ErrorState
import com.smartcommute.feature.linedetails.ui.components.LineDetailsHeader
import com.smartcommute.feature.linedetails.ui.components.LoadingState
import com.smartcommute.feature.linedetails.ui.components.NightTubeCard
import com.smartcommute.feature.linedetails.ui.components.OperationHoursCard

private val BackgroundColor = Color(0xFFF9FAFB)
private val TextPrimary = Color(0xFF0A0A0A)
private val BorderColor = Color(0x1A000000)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsScreen(
    onNavigateBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    @Suppress("DEPRECATION")
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.line_details_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_navigate_back),
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
                HorizontalDivider(
                    thickness = 0.686.dp,
                    color = BorderColor
                )
            }
        },
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        when (val state = uiState) {
            is LineDetailsUiState.Loading -> {
                LoadingState()
            }

            is LineDetailsUiState.Success -> {
                val lineDetails = state.lineDetails

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
                        else -> Color.Gray
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
                ) {
                    // Hero section
                    item {
                        LineDetailsHeader(
                            lineDetails = lineDetails,
                            lineColor = lineColor,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }

                    // Operation Hours card — always shown
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        OperationHoursCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Night Tube card — shown when API reports night service for this line
                    if (lineDetails.hasNightTube) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            NightTubeCard(
                                description = stringResource(R.string.night_tube_description),
                                frequency = stringResource(R.string.night_tube_frequency),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Crowding Information card — conditional on data
                    if (lineDetails.crowding != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            CrowdingInformationCard(
                                crowding = lineDetails.crowding,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
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
