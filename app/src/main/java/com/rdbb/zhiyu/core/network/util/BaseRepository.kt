package com.rdbb.zhiyu.core.network.util

import android.util.Log.e
import com.rdbb.zhiyu.core.network.model.ApiResponse
import kotlinx.coroutines.CancellationException

abstract class BaseRepository {
    protected suspend fun <T> safeCall(
        block: suspend () -> ApiResponse<T>
    ): ApiResponse<T> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        }
}