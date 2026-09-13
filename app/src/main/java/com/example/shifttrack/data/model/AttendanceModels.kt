package com.example.shifttrack.data.model

import com.google.gson.annotations.SerializedName

enum class AttendanceMethod {
    GPS,
    QR
}

enum class AttendanceStatus {
    ON_TIME,
    LATE,
    EARLY_DEPARTURE,
    ABSENT,
    PRESENT
}

enum class ClockState {
    NOT_CLOCKED_IN,
    CLOCKED_IN,
    CLOCKED_OUT
}

data class AttendanceDto(
    @SerializedName("id") val id: String,
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("date") val date: String,
    @SerializedName("clockInTime") val clockInTime: String?,
    @SerializedName("clockOutTime") val clockOutTime: String?,
    @SerializedName("method") val method: String,
    @SerializedName("status") val status: String,
    @SerializedName("locationNote") val locationNote: String? = null,
    @SerializedName("durationMinutes") val durationMinutes: Long? = null
)

data class ClockInRequest(
    @SerializedName("method") val method: String,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("accuracy") val accuracy: Float? = null,
    @SerializedName("qrData") val qrData: String? = null,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

data class ClockOutRequest(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

data class AttendanceStateResponse(
    @SerializedName("state") val state: String,
    @SerializedName("currentRecord") val currentRecord: AttendanceDto? = null,
    @SerializedName("shift") val shift: ShiftDto? = null
)
