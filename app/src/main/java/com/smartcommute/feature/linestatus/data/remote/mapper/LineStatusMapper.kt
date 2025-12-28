package com.smartcommute.feature.linestatus.data.remote.mapper

import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusDto
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusResponseDto
import com.smartcommute.feature.linestatus.domain.model.ServiceStatus
import com.smartcommute.feature.linestatus.domain.model.StatusType
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

/**
 * Extension functions for mapping data between different layers of the application:
 * - DTO (Data Transfer Objects) from TfL API responses
 * - Domain models used in business logic
 * - Entity objects stored in Room database
 */

/**
 * Converts TfL API DTO to domain model.
 * Takes the first status from the lineStatuses array as TfL typically returns one status per line.
 * Falls back to SERVICE_DISRUPTION if no status is available.
 */
fun LineStatusDto.toDomain(): UndergroundLine {
    val statusDto = lineStatuses.firstOrNull()
    val serviceStatus = statusDto?.toServiceStatus() ?: ServiceStatus(type = StatusType.SERVICE_DISRUPTION, description = "No status available")

    return UndergroundLine(
        id = id,
        name = name,
        modeName = modeName,
        status = serviceStatus
    )
}

/**
 * Converts domain model to database entity.
 * Includes timestamp parameter to track when the data was cached.
 */
fun UndergroundLine.toEntity(timestamp: Long): TubeLineEntity {
    return TubeLineEntity(
        id = id,
        name = name,
        modeName = modeName,
        statusType = status.type.name,
        statusDescription = status.description,
        statusSeverity = status.severity,
        brandColor = "#000000", // Default color, will be updated by Phase 6 integration
        headerImageRes = "placeholder", // Will be set based on lineId in Phase 6
        lastUpdated = timestamp,
        cacheExpiry = timestamp + 600000L // 10 minutes cache
    )
}

/**
 * Converts database entity back to domain model.
 * Falls back to SERVICE_DISRUPTION if the stored StatusType enum value is invalid.
 */
fun TubeLineEntity.toDomain(): UndergroundLine {
    val statusType = try {
        StatusType.valueOf(statusType)
    } catch (e: IllegalArgumentException) {
        StatusType.SERVICE_DISRUPTION
    }

    return UndergroundLine(
        id = id,
        name = name,
        modeName = modeName,
        status = ServiceStatus(
            type = statusType,
            description = statusDescription,
            severity = statusSeverity
        )
    )
}

/**
 * Maps TfL API severity codes to app's StatusType enum.
 *
 * TfL severity scale (from API documentation):
 * - 10 = Good Service (normal operations)
 * - 9 = Minor Delays (slight disruptions)
 * - 8, 7, 6 = Major Delays (significant disruptions)
 * - 5, 4, 3, 2 = Severe Delays (major disruptions)
 * - 1, 0 = Closure (line not operating)
 *
 * Any unrecognized severity code maps to SERVICE_DISRUPTION as a safe default.
 */
private fun LineStatusResponseDto.toServiceStatus(): ServiceStatus {
    val type = when (statusSeverity) {
        10 -> StatusType.GOOD_SERVICE
        9 -> StatusType.MINOR_DELAYS
        6, 7, 8 -> StatusType.MAJOR_DELAYS
        5, 4, 3, 2 -> StatusType.SEVERE_DELAYS
        1, 0 -> StatusType.CLOSURE
        else -> StatusType.SERVICE_DISRUPTION
    }

    return ServiceStatus(
        type = type,
        description = reason ?: statusSeverityDescription,
        severity = statusSeverity
    )
}
