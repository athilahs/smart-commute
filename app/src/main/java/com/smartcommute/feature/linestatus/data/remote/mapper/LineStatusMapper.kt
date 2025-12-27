package com.smartcommute.feature.linestatus.data.remote.mapper

import com.smartcommute.feature.linestatus.data.local.entity.LineStatusEntity
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusDto
import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusResponseDto
import com.smartcommute.feature.linestatus.domain.model.ServiceStatus
import com.smartcommute.feature.linestatus.domain.model.StatusType
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

object LineStatusMapper {

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

    private fun mapToServiceStatus(dto: LineStatusResponseDto): ServiceStatus {
        // TfL severity mapping: https://api.tfl.gov.uk/Line/Meta/Severity
        // 10 = Good Service, 9 = Minor Delays, 6 = Severe Delays, etc.
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
