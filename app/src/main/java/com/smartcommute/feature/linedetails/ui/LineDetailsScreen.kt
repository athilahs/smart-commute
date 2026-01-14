package com.smartcommute.feature.linedetails.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
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
import com.smartcommute.feature.linedetails.ui.components.LineInfoCard
import com.smartcommute.feature.linedetails.ui.components.LoadingState
import com.smartcommute.feature.linedetails.ui.components.StatusSummaryCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsScreen(
    onNavigateBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    @Suppress("DEPRECATION")
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content layer - draws from absolute top
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (val state = uiState) {
                    is LineDetailsUiState.Loading -> {
                        LoadingState()
                    }

                    is LineDetailsUiState.Success -> {
                        val lineDetails = state.lineDetails

                        // Track which items should be visible for staggered animation
                        var itemsVisible by remember { mutableStateOf(0) }

                        // Start showing items after shared transition completes (600ms delay)
                        LaunchedEffect(Unit) {
                            delay(600) // Wait for shared transitions
                            repeat(20) { // Enough for all possible items
                                delay(150) // Stagger delay between items (150ms for clear sequential effect)
                                itemsVisible++
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
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

                            // Calculate alpha based on scroll state
                            val density = LocalDensity.current
                            val fadeDistancePx = with(density) { 160.dp.toPx() }

                            val contentAlpha by remember {
                                derivedStateOf {
                                    if (listState.firstVisibleItemIndex == 0) {
                                        (1f - listState.firstVisibleItemScrollOffset / fadeDistancePx).coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    }
                                }
                            }

                            LineDetailsHeader(
                                lineId = lineDetails.id,
                                lineName = lineDetails.name,
                                statusShortText = getShortStatusText(lineDetails.status.type.name),
                                lineColor = lineColor,
                                animatedVisibilityScope = animatedVisibilityScope,
                                contentAlpha = contentAlpha,
                                modifier = Modifier.padding(top = 0.dp) // Remove padding to let it go behind status bar if needed
                            )
                        }

                        // Status Summary Card (with full description)
                        item {
                            AnimatedVisibility(
                                visible = itemsVisible >= 1,
                                enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                        slideInVertically(
                                            animationSpec = tween(durationMillis = 400),
                                            initialOffsetY = { it / 3 }
                                        )
                            ) {
                                StatusSummaryCard(
                                    status = lineDetails.status,
                                    lastUpdated = lineDetails.lastUpdated
                                )
                            }
                        }

                        // Line Info Card (Service Hours)
                        item {
                            AnimatedVisibility(
                                visible = itemsVisible >= 2,
                                enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                        slideInVertically(
                                            animationSpec = tween(durationMillis = 400),
                                            initialOffsetY = { it / 3 }
                                        )
                            ) {
                                // Determine if this is the last item
                                val isLastItem = lineDetails.disruptions.isEmpty() &&
                                                lineDetails.closures.isEmpty() &&
                                                lineDetails.crowding == null
                                LineInfoCard(
                                    lineId = lineDetails.id,
                                    showDivider = !isLastItem
                                )
                            }
                        }

                        // Disruptions Section - only show if there are disruptions
                        if (lineDetails.disruptions.isNotEmpty()) {
                            itemsIndexed(lineDetails.disruptions) { index, disruption ->
                                AnimatedVisibility(
                                    visible = itemsVisible >= (3 + index),
                                    enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                            slideInVertically(
                                                animationSpec = tween(durationMillis = 400),
                                                initialOffsetY = { it / 3 }
                                            )
                                ) {
                                    // Determine if this is the last disruption and there are no closures/crowding after
                                    val isLastDisruption = index == lineDetails.disruptions.lastIndex
                                    val isLastItem = isLastDisruption &&
                                                    lineDetails.closures.isEmpty() &&
                                                    lineDetails.crowding == null
                                    DisruptionCard(
                                        disruption = disruption,
                                        isExpanded = state.expandedDisruptions.contains(disruption.id),
                                        onToggleExpand = { viewModel.toggleDisruptionExpansion(disruption.id) },
                                        showDivider = !isLastItem
                                    )
                                }
                            }
                        }

                        // Closures Section - only show if there are closures
                        val closuresStartIndex = 3 + lineDetails.disruptions.size
                        if (lineDetails.closures.isNotEmpty()) {
                            itemsIndexed(lineDetails.closures) { index, closure ->
                                AnimatedVisibility(
                                    visible = itemsVisible >= (closuresStartIndex + index),
                                    enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                            slideInVertically(
                                                animationSpec = tween(durationMillis = 400),
                                                initialOffsetY = { it / 3 }
                                            )
                                ) {
                                    // Determine if this is the last closure and there's no crowding after
                                    val isLastClosure = index == lineDetails.closures.lastIndex
                                    val isLastItem = isLastClosure && lineDetails.crowding == null
                                    ClosureCard(
                                        closure = closure,
                                        isExpanded = state.expandedClosures.contains(closure.id),
                                        onToggleExpand = { viewModel.toggleClosureExpansion(closure.id) },
                                        showDivider = !isLastItem
                                    )
                                }
                            }
                        }

                        // Crowding Section - only show if there is crowding data
                        val crowdingIndex = closuresStartIndex + lineDetails.closures.size
                        if (lineDetails.crowding != null) {
                            item {
                                AnimatedVisibility(
                                    visible = itemsVisible >= crowdingIndex,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                            slideInVertically(
                                                animationSpec = tween(durationMillis = 400),
                                                initialOffsetY = { it / 3 }
                                            )
                                ) {
                                    // Crowding is always the last item if it exists
                                    CrowdingCard(
                                        crowding = lineDetails.crowding,
                                        showDivider = false
                                    )
                                }
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

        // TopAppBar overlay - floats on top of content
        val density = LocalDensity.current
        val fadeDistancePx = with(density) { 160.dp.toPx() }

        val isCollapsed by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > fadeDistancePx
            }
        }

        val topBarContainerColor by animateColorAsState(
            targetValue = if (isCollapsed) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
            label = "topBarContainerColor"
        )

        val topBarContentColor by animateColorAsState(
            targetValue = if (isCollapsed) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.White,
            label = "topBarContentColor"
        )

        TopAppBar(
            title = {
                AnimatedVisibility(
                    visible = isCollapsed,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (uiState is LineDetailsUiState.Success) {
                        Text((uiState as LineDetailsUiState.Success).lineDetails.name)
                    } else {
                        Text(stringResource(R.string.line_details_title))
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = topBarContentColor
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = topBarContainerColor,
                scrolledContainerColor = topBarContainerColor,
                titleContentColor = topBarContentColor,
                navigationIconContentColor = topBarContentColor,
                actionIconContentColor = topBarContentColor
            ),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
        }
    }
}
