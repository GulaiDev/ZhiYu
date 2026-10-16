package com.rdbb.zhiyu.core.di

import com.rdbb.zhiyu.BuildConfig
import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.network.ApiResponse
import com.rdbb.zhiyu.core.network.ApiService
import com.rdbb.zhiyu.core.network.RefreshRequest
import com.rdbb.zhiyu.core.network.RefreshResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(session: SessionManager, json: Json): OkHttpClient {
        // 专用刷新客户端，避免 Authenticator 递归
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val authenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (responseCount(response) >= 2) return null
                val refreshToken = session.refreshToken ?: return null
                return try {
                    val body = refreshRetrofit
                        .create(RefreshApi::class.java)
                        .refresh(RefreshRequest(refreshToken))
                        .execute()
                        .body()
                    val newToken = body?.data?.accessToken
                    if (body?.code == 0 && newToken != null) {
                        session.applyRefreshedAccessToken(newToken)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    } else {
                        session.forceExpired()
                        null
                    }
                } catch (e: Exception) {
                    session.forceExpired()
                    null
                }
            }

            private fun responseCount(response: Response): Int {
                var count = 1
                var prior = response.priorResponse
                while (prior != null) {
                    count++
                    prior = prior.priorResponse
                }
                return count
            }
        }

        val authInterceptor = { chain: okhttp3.Interceptor.Chain ->
            val builder = chain.request().newBuilder()
            session.accessToken?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .authenticator(authenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    /** 仅用于 401 后静默刷新 token。 */
    private interface RefreshApi {
        @retrofit2.http.POST("api/v1/auth/refresh")
        fun refresh(
            @retrofit2.http.Body body: RefreshRequest
        ): retrofit2.Call<ApiResponse<RefreshResponse>>
    }
}
