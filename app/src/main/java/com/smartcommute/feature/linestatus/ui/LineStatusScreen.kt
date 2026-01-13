package com.smartcommute.feature.linestatus.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import com.smartcommute.feature.linestatus.ui.components.LineStatusItem
import com.smartcommute.feature.linestatus.ui.components.LoadingStateOverlay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineStatusScreen(
    onLineClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope,
    @Suppress("DEPRECATION")
    viewModel: LineStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tube_status_title)) },
                actions = {
                    IconButton(onClick = { viewModel.refreshLineStatuses() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_status)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is LineStatusUiState.Loading -> {
                    LoadingStateOverlay()
                }
                is LineStatusUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refreshLineStatuses() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (state.isOffline) {
                                Banner(
                                    message = stringResource(R.string.banner_no_connection),
                                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                    textColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (state.lastUpdated != null) {
                                LastUpdatedText(timestamp = state.lastUpdated)
                            }
                            LineStatusList(
                                lines = state.lines,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onLineClick = onLineClick
                            )
                        }
                    }
                }
                is LineStatusUiState.Error -> {
                    if (state.cachedLines.isNotEmpty()) {
                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = { viewModel.refreshLineStatuses() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Banner(
                                    message = state.message,
                                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                    textColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (state.lastUpdated != null) {
                                    LastUpdatedText(timestamp = state.lastUpdated)
                                }
                                LineStatusList(
                                    lines = state.cachedLines,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onLineClick = onLineClick
                                )
                            }
                        }
                    } else {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.fetchLineStatuses() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Banner(
    message: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LastUpdatedText(timestamp: Long) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(timestamp))

    Text(
        text = stringResource(R.string.last_updated_format, formattedTime),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.LineStatusList(
    lines: List<UndergroundLine>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLineClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = lines,
            key = { line -> line.id }
        ) { line ->
            val lineColor = getLineColor(line.id)
            LineStatusItem(
                line = line,
                lineColor = lineColor,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onLineClick(line.id) }
            )
            if (line != lines.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun getLineColor(lineId: String): androidx.compose.ui.graphics.Color {
    return when (lineId.lowercase()) {
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
        else -> MaterialTheme.colorScheme.primary
    }
}
