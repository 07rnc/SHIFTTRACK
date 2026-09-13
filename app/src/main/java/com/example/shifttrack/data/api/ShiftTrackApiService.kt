package com.example.shifttrack.data.api

import com.example.shifttrack.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ShiftTrackApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<RefreshTokenResponse>>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("api/shifts/current")
    suspend fun getCurrentShift(): Response<ApiResponse<ShiftDto>>

    @GET("api/attendance/current-state")
    suspend fun getAttendanceState(): Response<ApiResponse<AttendanceStateResponse>>

    @POST("api/attendance/clock-in")
    suspend fun clockIn(@Body request: ClockInRequest): Response<ApiResponse<AttendanceDto>>

    @POST("api/attendance/clock-out")
    suspend fun clockOut(@Body request: ClockOutRequest): Response<ApiResponse<AttendanceDto>>

    @GET("api/attendance/history")
    suspend fun getAttendanceHistory(): Response<ApiResponse<List<AttendanceDto>>>

    @POST("api/leave/request")
    suspend fun submitLeave(@Body request: LeaveRequestDto): Response<ApiResponse<LeaveApplicationDto>>

    @GET("api/leave/history")
    suspend fun getLeaveHistory(): Response<ApiResponse<List<LeaveApplicationDto>>>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<AppNotificationDto>>>

    @POST("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("api/notifications/register-device")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<ApiResponse<Unit>>
}
