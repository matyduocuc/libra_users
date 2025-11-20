package com.empresa.libra_users.data.remote.dto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ReportApi {
    
    @GET("api/reports/dashboard")
    suspend fun getDashboardStatistics(
        @Header("Authorization") token: String
    ): Response<DashboardStatisticsDto>
}

