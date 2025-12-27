package com.smartcommute.feature.linestatus.data

import com.smartcommute.BuildConfig
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.data.local.dao.LineStatusDao
import com.smartcommute.feature.linestatus.data.remote.TflApiService
import com.smartcommute.feature.linestatus.data.remote.mapper.LineStatusMapper
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineStatusRepositoryImpl @Inject constructor(
    private val tflApiService: TflApiService,
    private val lineStatusDao: LineStatusDao
) : LineStatusRepository {

    override fun getLineStatuses(): Flow<NetworkResult<List<UndergroundLine>>> = flow {
        emit(NetworkResult.Loading)

        // First, try to load cached data
        val cachedData = try {
            lineStatusDao.getAllLineStatuses().first()
        } catch (e: Exception) {
            emptyList()
        }

        // If we have cached data, emit it immediately
        if (cachedData.isNotEmpty()) {
            val domainModels = cachedData.map { LineStatusMapper.entityToDomain(it) }
            emit(NetworkResult.Success(domainModels))
        }

        // Then try to fetch fresh data
        try {
            val apiKey = BuildConfig.TFL_API_KEY
            val response = tflApiService.getLineStatus(apiKey)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    val domainModels = data.map { LineStatusMapper.dtoToDomain(it) }

                    // Cache to database
                    val timestamp = System.currentTimeMillis()
                    val entities = domainModels.map { LineStatusMapper.domainToEntity(it, timestamp) }
                    lineStatusDao.insertAll(entities)

                    emit(NetworkResult.Success(domainModels))
                } else {
                    emit(NetworkResult.Error("Empty response from server"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Configuration error: Invalid API key"
                    429 -> "Service temporarily unavailable: Rate limit exceeded"
                    500, 502, 503, 504 -> "Service temporarily unavailable"
                    else -> "Unable to fetch status (${response.code()})"
                }

                // If we had cached data, it was already emitted above
                if (cachedData.isEmpty()) {
                    emit(NetworkResult.Error(errorMessage, response.code()))
                }
            }
        } catch (e: IOException) {
            // Network error (no connection)
            if (cachedData.isEmpty()) {
                emit(NetworkResult.Error("No connection. Please check your internet connection."))
            }
            // If we had cached data, it was already emitted above
        } catch (e: Exception) {
            if (cachedData.isEmpty()) {
                emit(NetworkResult.Error("An error occurred: ${e.message}"))
            }
        }
    }

    override suspend fun refreshLineStatuses() {
        try {
            val apiKey = BuildConfig.TFL_API_KEY
            val response = tflApiService.getLineStatus(apiKey)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    val domainModels = data.map { LineStatusMapper.dtoToDomain(it) }
                    val timestamp = System.currentTimeMillis()
                    val entities = domainModels.map { LineStatusMapper.domainToEntity(it, timestamp) }
                    lineStatusDao.insertAll(entities)
                }
            }
        } catch (e: Exception) {
            // Silently fail on refresh - the UI will continue showing cached data
        }
    }

    override suspend fun getLastUpdateTime(): Long? {
        return lineStatusDao.getLastUpdateTime()
    }
}
