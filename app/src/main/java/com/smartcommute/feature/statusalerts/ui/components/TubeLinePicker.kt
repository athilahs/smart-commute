package com.smartcommute.feature.statusalerts.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Tube line picker component with checkboxes for multi-select.
 * Fetches available lines from LineStatusRepository.
 */
@Composable
fun TubeLinePicker(
    selectedLineIds: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TubeLinePickerViewModel = hiltViewModel()
) {
    val linesState by viewModel.lines.collectAsState(initial = TubeLinePickerUiState.Loading)

    Column(modifier = modifier.fillMaxWidth()) {
        when (val state = linesState) {
            is TubeLinePickerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TubeLinePickerUiState.Success -> {
                if (state.lines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tube lines available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(state.lines) { line ->
                            TubeLineCheckboxItem(
                                line = line,
                                isSelected = selectedLineIds.contains(line.id),
                                onCheckedChange = { isChecked ->
                                    val newSelection = if (isChecked) {
                                        selectedLineIds + line.id
                                    } else {
                                        selectedLineIds - line.id
                                    }
                                    onSelectionChanged(newSelection)
                                }
                            )
                        }
                    }
                }
            }

            is TubeLinePickerUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading tube lines: ${state.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TubeLineCheckboxItem(
    line: UndergroundLine,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = line.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * ViewModel for TubeLinePicker to fetch and manage available tube lines.
 */
@dagger.hilt.android.lifecycle.HiltViewModel
class TubeLinePickerViewModel @Inject constructor(
    private val lineStatusRepository: LineStatusRepository
) : ViewModel() {

    val lines: Flow<TubeLinePickerUiState> = lineStatusRepository.getLineStatuses()
        .map { result ->
            when (result) {
                is NetworkResult.Success -> {
                    TubeLinePickerUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    TubeLinePickerUiState.Error(result.message ?: "Failed to load tube lines")
                }
                is NetworkResult.Loading -> {
                    TubeLinePickerUiState.Loading
                }
            }
        }
}

/**
 * UI state for TubeLinePicker.
 */
sealed interface TubeLinePickerUiState {
    data object Loading : TubeLinePickerUiState
    data class Success(val lines: List<UndergroundLine>) : TubeLinePickerUiState
    data class Error(val message: String) : TubeLinePickerUiState
}
