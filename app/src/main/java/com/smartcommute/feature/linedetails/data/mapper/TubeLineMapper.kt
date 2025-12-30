package com.smartcommute.feature.linedetails.data.mapper

import com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity
import com.smartcommute.feature.linedetails.data.local.entity.CrowdingEntity
import com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity
import com.smartcommute.feature.linedetails.domain.model.Closure
import com.smartcommute.feature.linedetails.domain.model.Crowding
import com.smartcommute.feature.linedetails.domain.model.CrowdingLevel
import com.smartcommute.feature.linedetails.domain.model.Disruption
import com.smartcommute.feature.linedetails.domain.model.UndergroundLineDetails
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity
import com.smartcommute.feature.linestatus.domain.model.ServiceStatus
import com.smartcommute.feature.linestatus.domain.model.StatusType

/**
 * Converts TubeLineEntity to basic domain model (for list view).
 */
fun TubeLineEntity.toBasicDomain(): UndergroundLineDetails {
    val statusType = try {
        StatusType.valueOf(statusType)
    } catch (e: IllegalArgumentException) {
        StatusType.SERVICE_DISRUPTION
    }

    return UndergroundLineDetails(
        id = id,
        name = name,
        modeName = modeName,
        status = ServiceStatus(
            type = statusType,
            description = statusDescription,
            severity = statusSeverity
        ),
        brandColor = brandColor,
        lastUpdated = lastUpdated,
        disruptions = emptyList(),
        closures = emptyList(),
        crowding = null
    )
}

/**
 * Converts TubeLineEntity with related entities to detailed domain model (for details view).
 */
fun TubeLineEntity.toDetailedDomain(
    disruptions: List<DisruptionEntity>,
    closures: List<ClosureEntity>,
    crowding: CrowdingEntity?
): UndergroundLineDetails {
    val statusType = try {
        StatusType.valueOf(statusType)
    } catch (e: IllegalArgumentException) {
        StatusType.SERVICE_DISRUPTION
    }

    return UndergroundLineDetails(
        id = id,
        name = name,
        modeName = modeName,
        status = ServiceStatus(
            type = statusType,
            description = statusDescription,
            severity = statusSeverity
        ),
        brandColor = brandColor,
        lastUpdated = lastUpdated,
        disruptions = disruptions.map { it.toDomain() },
        closures = closures.map { it.toDomain() },
        crowding = crowding?.toDomain()
    )
}

/**
 * Converts DisruptionEntity to domain model.
 */
fun DisruptionEntity.toDomain(): Disruption {
    return Disruption(
        id = id,
        category = category,
        type = type,
        categoryDescription = categoryDescription,
        description = description,
        closureText = closureText,
        affectedStops = affectedStops.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        createdDate = createdDate,
        startDate = startDate,
        endDate = endDate,
        severity = severity
    )
}

/**
 * Converts ClosureEntity to domain model.
 */
fun ClosureEntity.toDomain(): Closure {
    return Closure(
        id = id,
        description = description,
        reason = reason,
        affectedStations = affectedStations.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        affectedSegment = affectedSegment,
        startDate = startDate,
        endDate = endDate,
        alternativeRoute = alternativeRoute,
        replacementBus = replacementBus
    )
}

/**
 * Converts CrowdingEntity to domain model.
 */
fun CrowdingEntity.toDomain(): Crowding {
    return Crowding(
        level = CrowdingLevel.fromLabel(level),
        measurementTime = measurementTime,
        dataSource = dataSource,
        notes = notes
    )
}
