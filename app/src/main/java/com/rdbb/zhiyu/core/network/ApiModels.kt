package com.rdbb.zhiyu.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 后端统一响应包装 {code, message, data}，code == 0 为成功。 */
@Serializable
data class ApiResponse<T>(
    val code: Int = -1,
    val message: String? = null,
    val data: T? = null
)

/** 分页数据。 */
@Serializable
data class PageData<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 10
)

/** 业务/网络异常，message 可直接展示给用户。 */
class ApiException(val code: Int, message: String) : Exception(message)
