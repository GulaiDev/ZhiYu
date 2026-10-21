package com.rdbb.zhiyu.core.network.api

import com.rdbb.zhiyu.core.model.ApiResponse
import com.rdbb.zhiyu.core.model.LoginRequest
import com.rdbb.zhiyu.core.model.RefreshRequest
import com.rdbb.zhiyu.core.model.RefreshResponse
import com.rdbb.zhiyu.core.model.RegisterRequest
import com.rdbb.zhiyu.core.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    // ---------- 认证 ----------
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<TokenResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<TokenResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiResponse<RefreshResponse>
}