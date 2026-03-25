package com.smartcommute.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartcommute.R
import com.smartcommute.core.analytics.AppAnalytics
import com.smartcommute.core.navigation.AppNavigation
import com.smartcommute.core.navigation.NavigationScreen
import com.smartcommute.feature.statusalerts.notification.StatusAlertsNotificationManager

@Composable
fun MainScreen(
    intent: Intent? = null,
    analytics: AppAnalytics
) {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf("status") }

    // Handle notification click navigation
    LaunchedEffect(intent) {
        intent?.let {
            val navigateTo = it.getStringExtra(StatusAlertsNotificationManager.EXTRA_NAVIGATE_TO)
            when (navigateTo) {
                StatusAlertsNotificationManager.NAVIGATE_TO_LINE_DETAILS -> {
                    val lineId = it.getStringExtra(StatusAlertsNotificationManager.EXTRA_LINE_ID)
                    lineId?.let { id ->
                        analytics.logNotificationTapped("line_details", id)
                        navController.navigate(NavigationScreen.LineDetails.createRoute(id))
                    }
                }
                StatusAlertsNotificationManager.NAVIGATE_TO_STATUS -> {
                    analytics.logNotificationTapped("tube_status")
                    navController.navigate(NavigationScreen.LineStatus.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(stringResource(R.string.nav_status))
                    },
                    selected = currentDestination?.route?.let { route ->
                        route == NavigationScreen.LineStatus.route ||
                        route.startsWith("line_details/")
                    } ?: false,
                    onClick = {
                        val previousTab = currentTab
                        currentTab = "status"
                        if (previousTab != "status") {
                            analytics.logTabSwitched(previousTab, "status")
                        }
                        navController.navigate(NavigationScreen.LineStatus.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(stringResource(R.string.nav_alerts))
                    },
                    selected = currentDestination?.route == NavigationScreen.StatusAlerts.route,
                    onClick = {
                        val previousTab = currentTab
                        currentTab = "alerts"
                        if (previousTab != "alerts") {
                            analytics.logTabSwitched(previousTab, "alerts")
                        }
                        navController.navigate(NavigationScreen.StatusAlerts.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
            AppNavigation(
                navController = navController
            )
        }
    }
}
