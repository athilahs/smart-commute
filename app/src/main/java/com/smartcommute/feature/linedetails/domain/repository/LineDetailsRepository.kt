package com.smartcommute.feature.linedetails.domain.repository

import com.smartcommute.feature.linedetails.domain.model.UndergroundLineDetails
import kotlinx.coroutines.flow.Flow

interface LineDetailsRepository {
    /**
     * Gets complete line details including disruptions, closures, and crowding.
     * Returns a Flow that emits data from local cache only (no network requests).
     */
    fun getLineDetails(lineId: String): Flow<UndergroundLineDetails?>
}
