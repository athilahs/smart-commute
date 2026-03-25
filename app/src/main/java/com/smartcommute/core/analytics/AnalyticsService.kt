package com.smartcommute.core.analytics

interface AnalyticsService {
    fun log(event: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
}
