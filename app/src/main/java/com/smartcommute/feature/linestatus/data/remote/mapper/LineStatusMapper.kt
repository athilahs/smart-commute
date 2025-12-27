package com.smartcommute.feature.linestatus.data.remote.mapper

import com.smartcommute.feature.linestatus.data.local.entity.LineStatusEntity
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusDto
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusResponseDto
import com.smartcommute.feature.linestatus.domain.model.ServiceStatus
import com.smartcommute.feature.linestatus.domain.model.StatusType
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

/**
 * Maps data between different layers of the application:
 * - DTO (Data Transfer Objects) from TfL API responses
 * - Domain models used in business logic
 * - Entity objects stored in Room database
 */
object LineStatusMapper {

    /**
     * Converts TfL API DTO to domain model.
     * Takes the first status from the lineStatuses array as TfL typically returns one status per line.
     * Falls back to SERVICE_DISRUPTION if no status is available.
     */
    fun dtoToDomain(dto: LineStatusDto): UndergroundLine {
        val statusDto = dto.lineStatuses.firstOrNull()
        val serviceStatus = if (statusDto != null) {
            mapToServiceStatus(statusDto)
        } else {
            ServiceStatus(type = StatusType.SERVICE_DISRUPTION, description = "No status available")
        }

        return UndergroundLine(
            id = dto.id,
            name = dto.name,
            modeName = dto.modeName,
            status = serviceStatus
        )
    }

    /**
     * Converts domain model to database entity.
     * Includes timestamp parameter to track when the data was cached.
     */
    fun domainToEntity(domain: UndergroundLine, timestamp: Long): LineStatusEntity {
        return LineStatusEntity(
            id = domain.id,
            name = domain.name,
            modeName = domain.modeName,
            statusType = domain.status.type.name,
            statusDescription = domain.status.description,
            statusSeverity = domain.status.severity,
            lastUpdated = timestamp
        )
    }

    /**
     * Converts database entity back to domain model.
     * Falls back to SERVICE_DISRUPTION if the stored StatusType enum value is invalid.
     */
    fun entityToDomain(entity: LineStatusEntity): UndergroundLine {
        val statusType = try {
            StatusType.valueOf(entity.statusType)
        } catch (e: IllegalArgumentException) {
            StatusType.SERVICE_DISRUPTION
        }

        return UndergroundLine(
            id = entity.id,
            name = entity.name,
            modeName = entity.modeName,
            status = ServiceStatus(
                type = statusType,
                description = entity.statusDescription,
                severity = entity.statusSeverity
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
    private fun mapToServiceStatus(dto: LineStatusResponseDto): ServiceStatus {
        val type = when (dto.statusSeverity) {
            10 -> StatusType.GOOD_SERVICE
            9 -> StatusType.MINOR_DELAYS
            6, 7, 8 -> StatusType.MAJOR_DELAYS
            5, 4, 3, 2 -> StatusType.SEVERE_DELAYS
            1, 0 -> StatusType.CLOSURE
            else -> StatusType.SERVICE_DISRUPTION
        }

        return ServiceStatus(
            type = type,
            description = dto.reason ?: dto.statusSeverityDescription,
            severity = dto.statusSeverity
        )
    }
}
