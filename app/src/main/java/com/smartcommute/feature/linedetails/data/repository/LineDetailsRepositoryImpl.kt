package com.smartcommute.feature.linedetails.data.repository

import com.smartcommute.feature.linedetails.data.local.dao.LineDetailsDao
import com.smartcommute.feature.linedetails.data.mapper.toDetailedDomain
import com.smartcommute.feature.linedetails.domain.model.UndergroundLineDetails
import com.smartcommute.feature.linedetails.domain.repository.LineDetailsRepository
import com.smartcommute.feature.linestatus.data.local.dao.TubeLineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LineDetailsRepository with offline-only access.
 * NO network requests are made in this repository (per FR-007 requirement).
 * All data is read from local Room database cache.
 */
@Singleton
class LineDetailsRepositoryImpl @Inject constructor(
    private val tubeLineDao: TubeLineDao,
    private val lineDetailsDao: LineDetailsDao
) : LineDetailsRepository {

    override fun getLineDetails(lineId: String): Flow<UndergroundLineDetails?> = flow {
        // Fetch line entity from database
        val lineEntity = tubeLineDao.getLineStatusById(lineId)

        if (lineEntity != null) {
            // Fetch related details
            val disruptions = lineDetailsDao.getDisruptionsByLineId(lineId)
            val closures = lineDetailsDao.getClosuresByLineId(lineId)
            val crowding = lineDetailsDao.getCrowdingByLineId(lineId)

            // Map to domain model
            val lineDetails = lineEntity.toDetailedDomain(
                disruptions = disruptions,
                closures = closures,
                crowding = crowding
            )

            emit(lineDetails)
        } else {
            // Line not found in cache
            emit(null)
        }
    }.flowOn(Dispatchers.IO)
}
