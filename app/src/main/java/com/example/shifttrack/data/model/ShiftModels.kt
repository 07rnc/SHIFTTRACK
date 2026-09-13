package com.example.shifttrack.data.model

import com.google.gson.annotations.SerializedName

data class ShiftDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("locationName") val locationName: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("geofenceRadiusMeters") val geofenceRadiusMeters: Double = 100.0,
    @SerializedName("status") val status: String = "SCHEDULED"
)
