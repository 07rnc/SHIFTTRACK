package com.example.shifttrack.data.model

import com.google.gson.annotations.SerializedName

data class AppNotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String, // "SHIFT", "LEAVE", "ATTENDANCE", "SYSTEM"
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("actionRoute") val actionRoute: String? = null
)

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "ANDROID"
)
