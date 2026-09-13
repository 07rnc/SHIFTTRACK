package com.example.shifttrack.data.model

import com.google.gson.annotations.SerializedName

// Authoritative Backend Leave Statuses - Must NEVER be renamed or reinterpreted
object BackendLeaveStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val DENIED = "DENIED"
    const val CANCELLED = "CANCELLED"
}

data class LeaveRequestDto(
    @SerializedName("leaveType") val leaveType: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("isHalfDay") val isHalfDay: Boolean = false,
    @SerializedName("halfDayType") val halfDayType: String? = null
)

data class LeaveApplicationDto(
    @SerializedName("id") val id: String,
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("leaveType") val leaveType: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("daysCount") val daysCount: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String, // Authoritative status from backend
    @SerializedName("appliedAt") val appliedAt: String,
    @SerializedName("reviewedAt") val reviewedAt: String? = null,
    @SerializedName("reviewerRemarks") val reviewerRemarks: String? = null
)
