package com.smartcommute.feature.statusalerts.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusAlertsScreen(
    viewModel: StatusAlertsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
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

    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                alarmManager?.canScheduleExactAlarms() ?: false
            } else {
                true // No permission needed on Android < 12
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionDeniedDialog = true
        } else {
            // After granting notification permission, check exact alarm permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    showExactAlarmDialog = true
                }
            }
        }
    }

    // Request permission on first composition if needed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // If notification permission is already granted, check exact alarm permission
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                showExactAlarmDialog = true
            }
        }
    }

    // Re-check exact alarm permission when returning to the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val callback = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    hasExactAlarmPermission = alarmManager?.canScheduleExactAlarms() ?: false
                }
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(callback)
        onDispose {
            lifecycle.removeObserver(callback)
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

    // Exact alarm permission dialog
    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text("Alarms & Reminders Permission Required") },
            text = {
                Text(
                    "Status alerts need permission to schedule exact alarms so they can notify you " +
                    "at the precise time you set. Please enable 'Alarms & reminders' permission in settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    // Open exact alarm settings (Android 12+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Alarm") },
            text = {
                Text("Are you sure you want to delete this alarm? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    alarmConfiguration.alarmId?.let { id ->
                        viewModel.deleteAlarm(id)
                    }
                    showDeleteConfirmationDialog = false
                    showBottomSheet = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
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
                            hasPermission = hasPermission && hasExactAlarmPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission) {
                                    showExactAlarmDialog = true
                                }
                            }
                        )
                    } else {
                        // Alarm list with AlarmListItem
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.alarms) { alarm ->
                                AlarmListItem(
                                    alarm = alarm,
                                    onToggleEnabled = { isEnabled ->
                                        viewModel.toggleAlarmEnabled(alarm.id, isEnabled)
                                    },
                                    onClick = {
                                        // Open bottom sheet for editing (will be implemented in Phase 9)
                                        alarmConfiguration = AlarmConfigurationState.fromStatusAlert(alarm)
                                        showBottomSheet = true
                                    }
                                )
                            }
                        }
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
                if (alarmConfiguration.isEditMode) {
                    viewModel.updateAlarm(alarmConfiguration.toStatusAlert())
                } else {
                    viewModel.createAlarm(alarmConfiguration.toStatusAlert())
                }
                showBottomSheet = false
            },
            onDelete = if (alarmConfiguration.isEditMode) {
                { showDeleteConfirmationDialog = true }
            } else null,
            onDismiss = { showBottomSheet = false }
        )
    }
}
