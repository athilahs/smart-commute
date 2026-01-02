package com.smartcommute.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationScreen(val route: String) {
    @Serializable
    data object LineStatus : NavigationScreen("line_status")

    @Serializable
    data class LineDetails(val lineId: String) : NavigationScreen("line_details/{lineId}") {
        companion object {
            const val ROUTE_TEMPLATE = "line_details/{lineId}"

            fun createRoute(lineId: String): String = "line_details/$lineId"
        }
    }

    @Serializable
    data object StatusAlerts : NavigationScreen("status_alerts")

    // Placeholder for future features
    // @Serializable
    // data object JourneyPlanner : NavigationScreen("journey_planner")
    // @Serializable
    // data object SavedRoutes : NavigationScreen("saved_routes")
}
