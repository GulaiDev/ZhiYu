package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.model.ApiException
import com.rdbb.zhiyu.core.model.ApiResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRepository(
    private val json: Json
) {
    protected suspend fun <T> safeCall(block: suspend () -> ApiResponse<T>): Result<T> =
        try {
            val response = block()
            val data = response.data
            if (response.code == 0 && data != null) {
                Result.success(data)
            } else {
                Result.failure(ApiException(response.code, response.message ?: "请求失败"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: HttpException) {
            val msg = parseMessage(e) ?: if (e.code() == 401) "登录已过期，请重新登录" else "请求失败（${e.code()}）"
            Result.failure(ApiException(e.code(), msg))
        } catch (e: IOException) {
            Result.failure(ApiException(-1, "网络连接失败，请检查网络或后端服务"))
        } catch (e: Exception) {
            Result.failure(ApiException(-1, e.message ?: "未知错误"))
        }

    private fun parseMessage(e: HttpException): String? = try {
        val raw = e.response()?.errorBody()?.string() ?: return null
        json.decodeFromString<ApiResponse<Unit>>(raw).message
    } catch (ignore: Exception) {
        null
    }
}
