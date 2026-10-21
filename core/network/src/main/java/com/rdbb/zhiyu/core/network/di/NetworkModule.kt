package com.rdbb.zhiyu.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rdbb.zhiyu.core.datastore.UserManager
import com.rdbb.zhiyu.core.network.BuildConfig
import com.rdbb.zhiyu.core.network.api.ApiService
import com.rdbb.zhiyu.core.network.api.AuthApiService
import com.rdbb.zhiyu.core.network.interceptor.AuthInterceptor
import com.rdbb.zhiyu.core.network.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.java


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** kotlinx.serialization JSON 解析器配置 */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true   // 忽略后端多返回的字段，避免模型未声明时解析失败
        coerceInputValues = true  // JSON 中的 null 转为属性默认值
        explicitNulls = false     // 序列化时不输出值为 null 的字段
        encodeDefaults = true     // 序列化时包含使用默认值的属性
    }

    /** OkHttp 客户端：认证拦截器 + 日志拦截器 + 401 自动刷新认证器 + 超时配置 */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        // 日志拦截器：debug 打印请求/响应的完整内容，release 关闭日志避免泄露敏感信息
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // access_token 过期返回 401 时，自动用 refresh_token 换新并重试一次
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)// 连接超时时间
            .readTimeout(60, TimeUnit.SECONDS)// 读取超时时间
            .writeTimeout(60, TimeUnit.SECONDS)// 写入超时时间
            .callTimeout(45, TimeUnit.SECONDS)// 调用超时时间
            .build()
    }

    /**
     * 未登录状态下的 OkHttp 客户端。
     * 不能添加普通的 TokenAuthenticator 或 AuthInterceptor。
     */
    @Provides
    @Singleton
    @Named("NoAuth")
    fun provideNoAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .authenticator(Authenticator.NONE)
            .build()
    }

    /** Retrofit 实例：绑定 baseUrl、OkHttp 客户端和 kotlinx.serialization 转换器 */
    @Provides
    @Singleton
    fun provideRetrofit(
         mainHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(mainHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /** 刷新 Token 专用 Retrofit 实例 */
    @Provides
    @Singleton
    @Named("NoAuth")
    fun provideNoAuthRetrofit(
        @Named("NoAuth") client: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /** ApiService 实例：绑定 Retrofit 实例 */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // 刷新用的 AuthApiService：TokenAuthenticator 按 @Named("NoAuth") 注入
    @Provides
    @Singleton
    @Named("NoAuth")
    fun provideNoAuthAuthApi(@Named("NoAuth") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}