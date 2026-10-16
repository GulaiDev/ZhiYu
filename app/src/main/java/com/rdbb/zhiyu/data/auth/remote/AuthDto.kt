package com.rdbb.zhiyu.data.auth.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String,
)

@Serializable
class LoginRequest(
    val username: String,
    val password: String,
)


@Serializable
class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("token_type")
    val tokenType: String,
    val user: User,
)

@Serializable
class User(
    val id: Long,
    val username: String,
    val nickname: String,
    val avatar: String,
    val bio: String,
    val gender: Int,
    @SerialName("is_followed")
    val isFollowed: Boolean,
)
