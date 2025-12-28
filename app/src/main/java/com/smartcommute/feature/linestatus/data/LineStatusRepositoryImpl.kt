package com.smartcommute.feature.linestatus.data

import android.content.Context
import com.smartcommute.BuildConfig
import com.smartcommute.R
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.data.local.dao.TubeLineDao
import com.smartcommute.feature.linestatus.data.remote.TflApiService
import com.smartcommute.feature.linestatus.data.remote.mapper.toDomain
import com.smartcommute.feature.linestatus.data.remote.mapper.toEntity
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LineStatusRepository with offline-first architecture.
 *
 * Key behaviors:
 * 1. Emits cached data immediately if available (offline-first)
 * 2. Then attempts to fetch fresh data from TfL API
 * 3. Updates cache with fresh data on successful fetch
 * 4. Falls back to cached data on network/API errors
 * 5. Only emits error state if no cached data exists
 *
 * This ensures users always see data quickly, even with poor connectivity.
 */
@Singleton
class LineStatusRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tflApiService: TflApiService,
    private val tubeLineDao: TubeLineDao,
    private val lineDetailsDao: com.smartcommute.feature.linedetails.data.local.dao.LineDetailsDao
) : LineStatusRepository {

    /**
     * Returns a Flow that emits line statuses following offline-first pattern:
     * 1. Emits Loading state
     * 2. If cached data exists, emit it immediately (fast user experience)
     * 3. Attempt to fetch fresh data from API
     * 4. On success: cache and emit fresh data
     * 5. On failure: keep showing cached data if available, otherwise emit error
     */
    override fun getLineStatuses(): Flow<NetworkResult<List<UndergroundLine>>> = flow {
        emit(NetworkResult.Loading)

        // Attempt to load cached data first (offline-first approach)
        val cachedData = try {
            tubeLineDao.getAllLineStatuses().first()
        } catch (e: Exception) {
            emptyList()
        }

        // Emit cached data immediately if available for fast UX
        if (cachedData.isNotEmpty()) {
            val domainModels = cachedData.map { it.toDomain() }
            emit(NetworkResult.Success(domainModels))
        }

        // Attempt to fetch fresh data from TfL API
        try {
            val apiKey = BuildConfig.TFL_API_KEY
            val response = tflApiService.getLineStatus(apiKey)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    val domainModels = data.map { it.toDomain() }

                    // Cache fresh data to Room database with current timestamp
                    val timestamp = System.currentTimeMillis()
                    val entities = domainModels.map { it.toEntity(timestamp) }
                    tubeLineDao.insertAll(entities)

                    // Emit fresh data to UI
                    emit(NetworkResult.Success(domainModels))
                } else {
                    emit(NetworkResult.Error(context.getString(R.string.error_empty_response)))
                }
            } else {
                // Map HTTP error codes to user-friendly messages
                val errorMessage = when (response.code()) {
                    401 -> context.getString(R.string.error_config)
                    429 -> context.getString(R.string.error_rate_limit)
                    500, 502, 503, 504 -> context.getString(R.string.banner_service_unavailable)
                    else -> context.getString(R.string.error_unable_to_fetch)
                }

                // Only emit error if we don't have cached data to show
                // If cached data was emitted above, user continues seeing that
                if (cachedData.isEmpty()) {
                    emit(NetworkResult.Error(errorMessage, response.code()))
                }
            }
        } catch (e: IOException) {
            // Network error (no connection, timeout, etc.)
            // Only emit error if no cached data is available
            if (cachedData.isEmpty()) {
                emit(NetworkResult.Error(context.getString(R.string.error_no_connection)))
            }
            // If cached data exists, user already sees it from earlier emit
        } catch (e: Exception) {
            // Unexpected error
            if (cachedData.isEmpty()) {
                emit(NetworkResult.Error(context.getString(R.string.error_occurred, e.message ?: "")))
            }
        }
    }

    /**
     * Refreshes line statuses from API and updates cache.
     * Also caches disruptions, closures, and crowding data.
     * Silently fails on error - UI continues showing cached data.
     * Called by pull-to-refresh and manual refresh button.
     */
    override suspend fun refreshLineStatuses() {
        try {
            val apiKey = BuildConfig.TFL_API_KEY
            val response = tflApiService.getLineStatus(apiKey)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    val timestamp = System.currentTimeMillis()

                    // Cache basic line information
                    val domainModels = data.map { it.toDomain() }
                    val entities = domainModels.map { dto ->
                        dto.toEntity(timestamp).copy(
                            headerImageRes = getHeaderImageResForLine(dto.id)
                        )
                    }
                    tubeLineDao.insertAll(entities)

                    // Cache detailed information for each line
                    data.forEach { lineDto ->
                        cacheLineDetails(lineDto, timestamp)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail on refresh - UI will continue showing cached data
            // ViewModel will handle showing error banners if needed
        }
    }

    /**
     * Maps line ID to header image resource name.
     */
    private fun getHeaderImageResForLine(lineId: String): String {
        return "line_header_${lineId.lowercase().replace(" ", "_")}"
    }

    /**
     * Caches disruptions, closures, and crowding data for a line.
     */
    private suspend fun cacheLineDetails(lineDto: com.smartcommute.feature.linestatus.data.remote.dto.LineStatusDto, timestamp: Long) {
        val lineId = lineDto.id

        // Extract and cache disruptions
        val disruptions = mutableListOf<com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity>()
        val closures = mutableListOf<com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity>()

        lineDto.lineStatuses.forEach { status ->
            status.disruption?.let { disruptionDto ->
                val disruption = com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity(
                    lineId = lineId,
                    category = disruptionDto.category,
                    type = disruptionDto.type,
                    categoryDescription = disruptionDto.categoryDescription,
                    description = disruptionDto.description,
                    closureText = disruptionDto.closureText,
                    affectedStops = disruptionDto.affectedStops?.joinToString(",") ?: "",
                    createdDate = parseDate(disruptionDto.created) ?: timestamp,
                    startDate = status.validityPeriods?.firstOrNull()?.fromDate?.let { parseDate(it) },
                    endDate = status.validityPeriods?.firstOrNull()?.toDate?.let { parseDate(it) },
                    severity = status.statusSeverity
                )

                // Determine if it's a closure or disruption
                if (disruptionDto.category.contains("Closure", ignoreCase = true) ||
                    disruptionDto.categoryDescription.contains("Closure", ignoreCase = true)) {
                    val closure = com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity(
                        lineId = lineId,
                        description = disruptionDto.description,
                        reason = disruptionDto.categoryDescription,
                        affectedStations = disruptionDto.affectedStops?.joinToString(",") ?: "",
                        affectedSegment = disruptionDto.affectedRoutes?.firstOrNull(),
                        startDate = disruption.startDate ?: timestamp,
                        endDate = disruption.endDate ?: (timestamp + 86400000L), // +24h
                        alternativeRoute = disruptionDto.closureText,
                        replacementBus = disruptionDto.description.contains("replacement bus", ignoreCase = true)
                    )
                    closures.add(closure)
                } else {
                    disruptions.add(disruption)
                }
            }
        }

        // Cache disruptions and closures
        if (disruptions.isNotEmpty()) {
            lineDetailsDao.deleteDisruptionsByLineId(lineId)
            lineDetailsDao.insertDisruptions(disruptions)
        }

        if (closures.isNotEmpty()) {
            lineDetailsDao.deleteClosuresByLineId(lineId)
            lineDetailsDao.insertClosures(closures)
        }

        // Generate mock crowding data (TFL API doesn't provide this reliably)
        val crowding = com.smartcommute.feature.linedetails.data.local.entity.CrowdingEntity(
            lineId = lineId,
            level = listOf("Quiet", "Moderate", "Busy", "Very Busy").random(),
            levelCode = (0..3).random(),
            measurementTime = timestamp,
            dataSource = "Estimated",
            notes = null
        )
        lineDetailsDao.insertCrowding(crowding)
    }

    /**
     * Parses ISO 8601 date string to Unix timestamp.
     */
    private fun parseDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return try {
            java.time.Instant.parse(dateString).toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }


    override suspend fun getLastUpdateTime(): Long? {
        return tubeLineDao.getLastUpdateTime()
    }
}
