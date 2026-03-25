package com.smartcommute.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAnalytics @Inject constructor(
    private val analytics: AnalyticsService
) {
    fun logTabSwitched(fromTab: String, toTab: String) {
        analytics.log("tab_switched", mapOf(
            "from_tab" to fromTab,
            "to_tab" to toTab
        ))
    }

    fun logNotificationTapped(destination: String, lineId: String? = null) {
        val params = mutableMapOf<String, Any>("destination" to destination)
        lineId?.let { params["line_id"] = it }
        analytics.log("notification_tapped", params)
    }
}
