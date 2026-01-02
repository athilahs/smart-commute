package com.smartcommute.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartcommute.R
import com.smartcommute.core.navigation.AppNavigation
import com.smartcommute.core.navigation.NavigationScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
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
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(NavigationScreen.LineStatus::class)
                    } == true,
                    onClick = {
                        navController.navigate(NavigationScreen.LineStatus.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
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
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(NavigationScreen.StatusAlerts::class)
                    } == true,
                    onClick = {
                        navController.navigate(NavigationScreen.StatusAlerts.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                // Future navigation items will be added here
                // NavigationBarItem(
                //     icon = { Icon(Icons.Default.Map, contentDescription = null) },
                //     label = { Text(stringResource(R.string.nav_journey_planner)) },
                //     selected = currentDestination?.hierarchy?.any {
                //         it.hasRoute(NavigationScreen.JourneyPlanner::class)
                //     } == true,
                //     onClick = { /* Navigate to Journey Planner */ }
                // )
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
