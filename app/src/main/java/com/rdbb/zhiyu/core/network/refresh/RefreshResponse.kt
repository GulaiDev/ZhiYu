package com.rdbb.zhiyu.core.network.refresh

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RefreshResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
)
