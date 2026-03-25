package com.smartcommute.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TubeStatusAnalytics @Inject constructor(
    private val analytics: AnalyticsService
) {
    fun logRefresh(trigger: String, result: String, errorType: String? = null) {
        val params = mutableMapOf<String, Any>(
            "trigger" to trigger,
            "result" to result
        )
        errorType?.let { params["error_type"] = it }
        analytics.log("status_refresh", params)
    }

    fun logLineSelected(lineId: String, lineName: String, statusType: String) {
        analytics.log("line_selected", mapOf(
            "line_id" to lineId,
            "line_name" to lineName,
            "status_type" to statusType
        ))
    }

    fun logLoaded(lineCount: Int, source: String, isOffline: Boolean) {
        analytics.log("status_loaded", mapOf(
            "line_count" to lineCount,
            "source" to source,
            "is_offline" to isOffline
        ))
    }

    fun logError(errorMessage: String) {
        analytics.log("status_error", mapOf("error_type" to classifyError(errorMessage)))
    }

    fun logRetryTapped() {
        analytics.log("retry_tapped", mapOf("screen" to "tube_status"))
    }

    private fun classifyError(message: String): String {
        return when {
            message.contains("connection", ignoreCase = true) || message.contains("network", ignoreCase = true) -> "no_connection"
            message.contains("rate limit", ignoreCase = true) || message.contains("429") -> "rate_limited"
            message.contains("500") || message.contains("server", ignoreCase = true) -> "server_error"
            message.contains("api key", ignoreCase = true) || message.contains("401") -> "invalid_api_key"
            else -> "unknown"
        }
    }
}
