package com.smartcommute.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartcommute.feature.linedetails.ui.LineDetailsScreen
import com.smartcommute.feature.linestatus.ui.LineStatusScreen
import com.smartcommute.feature.statusalerts.ui.StatusAlertsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = NavigationScreen.LineStatus.route
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(NavigationScreen.LineStatus.route) {
                LineStatusScreen(
                    onLineClick = { lineId ->
                        navController.navigate(NavigationScreen.LineDetails.createRoute(lineId))
                    },
                    animatedVisibilityScope = this
                )
            }

            composable(
                route = NavigationScreen.LineDetails.ROUTE_TEMPLATE,
                arguments = listOf(
                    navArgument("lineId") {
                        type = NavType.StringType
                    }
                )
            ) {
                LineDetailsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    animatedVisibilityScope = this
                )
            }

            composable(NavigationScreen.StatusAlerts.route) {
                StatusAlertsScreen()
            }

            // Future feature routes will be added here
            // composable(NavigationScreen.JourneyPlanner.route) {
            //     JourneyPlannerScreen()
            // }
        }
    }
}
