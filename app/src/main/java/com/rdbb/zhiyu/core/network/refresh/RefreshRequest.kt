package com.rdbb.zhiyu.core.network.refresh

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RefreshRequest(
    @SerialName("refresh_token")
    val refreshToken: String,
)
