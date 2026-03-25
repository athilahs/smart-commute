package com.smartcommute.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineDetailsAnalytics @Inject constructor(
    private val analytics: AnalyticsService
) {
    fun logLoaded(lineId: String, lineName: String, statusType: String, hasCrowding: Boolean, hasNightTube: Boolean) {
        analytics.log("line_details_loaded", mapOf(
            "line_id" to lineId,
            "line_name" to lineName,
            "status_type" to statusType,
            "has_crowding" to hasCrowding,
            "has_night_tube" to hasNightTube
        ))
    }

    fun logError(lineId: String, errorType: String) {
        analytics.log("line_details_error", mapOf(
            "line_id" to lineId,
            "error_type" to errorType
        ))
    }

    fun logRetryTapped() {
        analytics.log("retry_tapped", mapOf("screen" to "line_details"))
    }
}
