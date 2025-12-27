package com.smartcommute.feature.linestatus.domain.repository

import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import kotlinx.coroutines.flow.Flow

interface LineStatusRepository {
    fun getLineStatuses(): Flow<NetworkResult<List<UndergroundLine>>>
    suspend fun refreshLineStatuses()
    suspend fun getLastUpdateTime(): Long?
}
