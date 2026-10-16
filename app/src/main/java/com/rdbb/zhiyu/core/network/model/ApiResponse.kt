package com.rdbb.zhiyu.core.network.model

import kotlinx.serialization.Serializable

@Serializable
class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
){
    fun isSuccess(): Boolean = code == 200
}