package com.example.shifttrack.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceToken") val deviceToken: String? = null
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: UserDto
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("employeeCode") val employeeCode: String,
    @SerializedName("role") val role: String = "EMPLOYEE",
    @SerializedName("department") val department: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)
