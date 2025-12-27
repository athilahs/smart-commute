package com.smartcommute.feature.linestatus.data

import android.content.Context
import com.smartcommute.BuildConfig
import com.smartcommute.R
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.data.local.dao.LineStatusDao
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
    private val lineStatusDao: LineStatusDao
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
            lineStatusDao.getAllLineStatuses().first()
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
                    lineStatusDao.insertAll(entities)

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
                    val domainModels = data.map { it.toDomain() }
                    val timestamp = System.currentTimeMillis()
                    val entities = domainModels.map { it.toEntity(timestamp) }
                    lineStatusDao.insertAll(entities)
                }
            }
        } catch (e: Exception) {
            // Silently fail on refresh - UI will continue showing cached data
            // ViewModel will handle showing error banners if needed
        }
    }

    override suspend fun getLastUpdateTime(): Long? {
        return lineStatusDao.getLastUpdateTime()
    }
}
