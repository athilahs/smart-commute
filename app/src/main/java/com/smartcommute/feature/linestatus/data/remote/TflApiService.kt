package com.smartcommute.feature.linestatus.data.remote

import com.smartcommute.feature.linestatus.data.remote.dto.LineStatusDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TflApiService {
    @GET("Line/Mode/tube/Status")
    suspend fun getLineStatus(
        @Query("app_key") apiKey: String
    ): Response<List<LineStatusDto>>
}
