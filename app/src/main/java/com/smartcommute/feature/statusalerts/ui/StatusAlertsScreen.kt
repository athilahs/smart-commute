package com.smartcommute.feature.statusalerts.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusAlertsScreen(
    viewModel: StatusAlertsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var alarmConfiguration by remember {
        mutableStateOf(
            AlarmConfigurationState()
        )
    }
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PermissionChecker.PERMISSION_GRANTED
            } else {
                true // Permission granted by default on Android < 13
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionDeniedDialog = true
        }
    }

    // Request permission on first composition if needed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Notification Permission Required") },
            text = {
                Text(
                    "Status alerts require notification permission to notify you about tube line statuses. " +
                    "Please enable notifications in settings to receive alerts."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Alerts") }
            )
        },
        floatingActionButton = {
            // Show FAB only if we have permission and can create more alarms
            if (hasPermission && uiState is StatusAlertsUiState.Success) {
                val successState = uiState as StatusAlertsUiState.Success
                if (successState.canCreateMore) {
                    FloatingActionButton(
                        onClick = {
                            alarmConfiguration = AlarmConfigurationState()
                            showBottomSheet = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create alarm")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is StatusAlertsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StatusAlertsUiState.Success -> {
                    if (state.alarms.isEmpty()) {
                        EmptyStateView(
                            hasPermission = hasPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    } else {
                        // Alarm list - will be implemented in Phase 5 (T033-T034)
                        Text(
                            text = "Alarms list will be displayed here (${state.alarmCount} alarms)",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }

                is StatusAlertsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    // Alarm configuration bottom sheet
    if (showBottomSheet) {
        AlarmBottomSheet(
            alarmConfiguration = alarmConfiguration,
            onConfigurationChanged = { alarmConfiguration = it },
            onSave = {
                viewModel.createAlarm(alarmConfiguration.toStatusAlert())
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}
