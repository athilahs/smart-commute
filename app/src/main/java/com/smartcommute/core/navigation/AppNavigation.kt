package com.smartcommute.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smartcommute.feature.linestatus.ui.LineStatusScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = NavigationScreen.LineStatus.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationScreen.LineStatus.route) {
            LineStatusScreen()
        }

        // Future feature routes will be added here
        // composable(NavigationScreen.JourneyPlanner.route) {
        //     JourneyPlannerScreen()
        // }
    }
}
