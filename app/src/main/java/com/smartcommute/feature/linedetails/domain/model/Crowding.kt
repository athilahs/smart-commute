package com.smartcommute.feature.linedetails.domain.model

enum class CrowdingLevel(val code: Int, val label: String) {
    QUIET(0, "Quiet"),
    MODERATE(1, "Moderate"),
    BUSY(2, "Busy"),
    VERY_BUSY(3, "Very Busy");

    companion object {
        fun fromCode(code: Int): CrowdingLevel {
            return entries.find { it.code == code } ?: MODERATE
        }

        fun fromLabel(label: String): CrowdingLevel {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: MODERATE
        }
    }
}

data class Crowding(
    val level: CrowdingLevel,
    val measurementTime: Long,
    val dataSource: String,
    val notes: String?
)
