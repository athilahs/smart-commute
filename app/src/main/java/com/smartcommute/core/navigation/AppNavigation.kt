package com.smartcommute.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartcommute.feature.linedetails.ui.LineDetailsScreen
import com.smartcommute.feature.linestatus.ui.LineStatusScreen

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
                        navController.navigate("line_details/$lineId")
                    },
                    animatedVisibilityScope = this
                )
            }

            composable(
                route = "line_details/{lineId}",
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

            // Future feature routes will be added here
            // composable(NavigationScreen.JourneyPlanner.route) {
            //     JourneyPlannerScreen()
            // }
        }
    }
}
