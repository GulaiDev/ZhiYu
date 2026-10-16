package com.rdbb.zhiyu.core.network.refresh

import com.rdbb.zhiyu.core.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): ApiResponse<RefreshResponse>
}