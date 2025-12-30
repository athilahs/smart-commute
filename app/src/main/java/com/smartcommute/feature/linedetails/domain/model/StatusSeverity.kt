package com.smartcommute.feature.linedetails.domain.model

enum class StatusSeverity(val level: Int, val description: String) {
    GOOD_SERVICE(10, "Good Service"),
    MINOR_DELAYS(9, "Minor Delays"),
    MAJOR_DELAYS(7, "Major Delays"),
    SEVERE_DELAYS(4, "Severe Delays"),
    PART_CLOSURE(2, "Part Closure"),
    CLOSED(0, "Closed");

    companion object {
        fun fromLevel(level: Int): StatusSeverity {
            return when (level) {
                10 -> GOOD_SERVICE
                9 -> MINOR_DELAYS
                in 6..8 -> MAJOR_DELAYS
                in 2..5 -> SEVERE_DELAYS
                1 -> PART_CLOSURE
                0 -> CLOSED
                else -> GOOD_SERVICE
            }
        }
    }
}
