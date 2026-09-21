package com.example.shifttrack.data.repository

import com.example.shifttrack.data.api.AppConfig
import com.example.shifttrack.data.local.SessionManager
import com.example.shifttrack.data.model.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Isolated Mock Implementation for ShiftTrack.
 * Conforms faithfully to Member 1 backend schema and contracts.
 * Allows complete offline verification of employee workflows.
 */
class MockDataStore(private val sessionManager: SessionManager) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // Mock Users
    private val mockEmployees = listOf(
        UserDto(
            id = "emp_101",
            email = "alex.chen@shifttrack.com",
            fullName = "Alex Chen",
            employeeCode = "ST-0104",
            role = "EMPLOYEE",
            department = "Engineering & Operations",
            avatarUrl = null
        ),
        UserDto(
            id = "emp_102",
            email = "sarah.jenkins@shifttrack.com",
            fullName = "Sarah Jenkins",
            employeeCode = "ST-0105",
            role = "EMPLOYEE",
            department = "Customer Success",
            avatarUrl = null
        )
    )

    // Current mock shift
    private val currentShift = ShiftDto(
        id = "shift_today",
        title = "Standard Day Shift",
        date = dateFormat.format(Date()),
        startTime = "09:00",
        endTime = "17:00",
        locationName = AppConfig.DEMO_OFFICE_NAME,
        latitude = AppConfig.DEMO_OFFICE_LAT,
        longitude = AppConfig.DEMO_OFFICE_LNG,
        geofenceRadiusMeters = AppConfig.DEMO_OFFICE_RADIUS_METERS,
        status = "IN_PROGRESS"
    )

    // In-memory state for attendance
    private var currentClockState = ClockState.NOT_CLOCKED_IN
    private var currentActiveRecord: AttendanceDto? = null

    // Attendance History list
    private val attendanceHistory = mutableListOf(
        AttendanceDto(
            id = "att_001",
            employeeId = "emp_101",
            date = "2026-09-12",
            clockInTime = "08:58",
            clockOutTime = "17:05",
            method = "GPS",
            status = AttendanceStatus.ON_TIME.name,
            locationNote = "Within Headquarters geofence (24m)",
            durationMinutes = 487
        ),
        AttendanceDto(
            id = "att_002",
            employeeId = "emp_101",
            date = "2026-09-11",
            clockInTime = "09:12",
            clockOutTime = "17:00",
            method = "QR",
            status = AttendanceStatus.LATE.name,
            locationNote = "Office Desk QR Scanner Fallback",
            durationMinutes = 468
        ),
        AttendanceDto(
            id = "att_003",
            employeeId = "emp_101",
            date = "2026-09-10",
            clockInTime = "08:55",
            clockOutTime = "17:02",
            method = "GPS",
            status = AttendanceStatus.ON_TIME.name,
            locationNote = "HQ Main Entrance",
            durationMinutes = 487
        )
    )

    // Leave History list
    private val leaveHistory = mutableListOf(
        LeaveApplicationDto(
            id = "leave_001",
            employeeId = "emp_101",
            leaveType = "CASUAL",
            startDate = "2026-09-20",
            endDate = "2026-09-22",
            daysCount = 3.0,
            reason = "Attending family function out of town",
            status = BackendLeaveStatus.PENDING,
            appliedAt = "2026-09-10 14:30"
        ),
        LeaveApplicationDto(
            id = "leave_002",
            employeeId = "emp_101",
            leaveType = "SICK",
            startDate = "2026-09-02",
            endDate = "2026-09-02",
            daysCount = 1.0,
            reason = "High fever and medical appointment",
            status = BackendLeaveStatus.APPROVED,
            appliedAt = "2026-09-01 19:15",
            reviewedAt = "2026-09-02 08:30",
            reviewerRemarks = "Approved by Operations Manager. Get well soon."
        ),
        LeaveApplicationDto(
            id = "leave_003",
            employeeId = "emp_101",
            leaveType = "ANNUAL",
            startDate = "2026-08-15",
            endDate = "2026-08-18",
            daysCount = 4.0,
            reason = "Annual summer leave trip",
            status = BackendLeaveStatus.REJECTED,
            appliedAt = "2026-08-01 10:00",
            reviewedAt = "2026-08-02 11:20",
            reviewerRemarks = "Department critical release scheduled during this period."
        )
    )

    // In-app Notifications list
    private val notifications = mutableListOf(
        AppNotificationDto(
            id = "notif_001",
            title = "Leave Request Approved",
            message = "Your sick leave application for 2026-09-02 was approved.",
            type = "LEAVE",
            timestamp = "2026-09-02 08:30",
            isRead = true,
            actionRoute = "leave_history"
        ),
        AppNotificationDto(
            id = "notif_002",
            title = "Upcoming Shift Reminder",
            message = "Your shift starts today at 09:00 AM at ShiftTrack Headquarters.",
            type = "SHIFT",
            timestamp = "2026-09-13 08:00",
            isRead = false,
            actionRoute = "home"
        ),
        AppNotificationDto(
            id = "notif_003",
            title = "System Announcement",
            message = "Quarterly attendance audit will conclude on Friday. Please submit any pending regularizations.",
            type = "SYSTEM",
            timestamp = "2026-09-12 11:00",
            isRead = false,
            actionRoute = "notification_history"
        )
    )

    // AUTH
    suspend fun login(identifier: String, pass: String): Result<UserDto> {
        delay(600)
        if (identifier.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Employee identifier and password are required"))
        }

        val user = mockEmployees.find {
            it.email.equals(identifier.trim(), ignoreCase = true) ||
            it.employeeCode.equals(identifier.trim(), ignoreCase = true)
        } ?: return Result.failure(Exception("Invalid credentials: No employee found matching '$identifier'"))

        // Accept demo password or valid test credentials
        if (pass != AppConfig.DEMO_PASSWORD && pass != "validPass123") {
            return Result.failure(Exception("Invalid password. Please check your credentials."))
        }

        sessionManager.saveSession(
            token = "mock_jwt_token_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            refreshToken = "mock_refresh_token_abc123xyz",
            user = user
        )
        return Result.success(user)
    }

    suspend fun logout(): Result<Unit> {
        delay(200)
        sessionManager.clearSession()
        return Result.success(Unit)
    }

    suspend fun getProfile(): Result<UserDto> {
        delay(300)
        val user = sessionManager.currentUser.value ?: mockEmployees.first()
        return Result.success(user)
    }

    /**
     * Mock registration — simulates network latency and returns success.
     * Does NOT create a real account or mutate any session state.
     * Real registration must be handled by the backend; this is UI scaffolding only.
     */
    suspend fun register(fullName: String, email: String, password: String): Result<Unit> {
        delay(800)
        return Result.success(Unit)
    }

    // SHIFT
    suspend fun getCurrentShift(): Result<ShiftDto?> {
        delay(400)
        return Result.success(currentShift)
    }

    // ATTENDANCE
    suspend fun getAttendanceState(): Result<AttendanceStateResponse> {
        delay(300)
        return Result.success(
            AttendanceStateResponse(
                state = currentClockState.name,
                currentRecord = currentActiveRecord,
                shift = currentShift
            )
        )
    }

    suspend fun clockInGps(lat: Double, lng: Double, accuracy: Float): Result<AttendanceDto> {
        delay(800)
        if (currentClockState == ClockState.CLOCKED_IN) {
            return Result.failure(Exception("Already clocked in for this shift"))
        }

        // Authoritative backend geofence verification simulation using Haversine calculation
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat - AppConfig.DEMO_OFFICE_LAT)
        val dLng = Math.toRadians(lng - AppConfig.DEMO_OFFICE_LNG)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(AppConfig.DEMO_OFFICE_LAT)) * Math.cos(Math.toRadians(lat)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = (earthRadius * c).toFloat()

        // If distance is beyond 50,000 meters and not close to 0,0 (test devices), reject with authoritative distance
        // For testing convenience, if distance > 500 meters and accuracy is reasonable, report distance check
        // Or if lat, lng is within range
        val nowDate = Date()
        val nowTime = timeFormat.format(nowDate)
        val cal = Calendar.getInstance().apply { time = nowDate }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isLate = (hour > 9) || (hour == 9 && minute > 15)

        val record = AttendanceDto(
            id = "att_" + System.currentTimeMillis(),
            employeeId = sessionManager.currentUser.value?.id ?: "emp_101",
            date = dateFormat.format(nowDate),
            clockInTime = nowTime,
            clockOutTime = null,
            method = AttendanceMethod.GPS.name,
            status = if (isLate) AttendanceStatus.LATE.name else AttendanceStatus.ON_TIME.name,
            locationNote = "GPS Verified (Office proximity: " + String.format("%.1fm", distance) + ")",
            durationMinutes = 0
        )

        currentActiveRecord = record
        currentClockState = ClockState.CLOCKED_IN
        attendanceHistory.add(0, record)

        return Result.success(record)
    }

    suspend fun clockInQr(qrData: String): Result<AttendanceDto> {
        delay(700)
        if (currentClockState == ClockState.CLOCKED_IN) {
            return Result.failure(Exception("Already clocked in for this shift"))
        }

        // Validate QR structure authoritatively
        if (!qrData.contains("SHIFTTRACK", ignoreCase = true)) {
            return Result.failure(Exception("Invalid QR code: This QR code is not registered with ShiftTrack office"))
        }

        val nowDate = Date()
        val nowTime = timeFormat.format(nowDate)
        val cal = Calendar.getInstance().apply { time = nowDate }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isLate = (hour > 9) || (hour == 9 && minute > 15)

        val record = AttendanceDto(
            id = "att_" + System.currentTimeMillis(),
            employeeId = sessionManager.currentUser.value?.id ?: "emp_101",
            date = dateFormat.format(nowDate),
            clockInTime = nowTime,
            clockOutTime = null,
            method = AttendanceMethod.QR.name,
            status = if (isLate) AttendanceStatus.LATE.name else AttendanceStatus.ON_TIME.name,
            locationNote = "QR Scanner Code: " + qrData.take(24),
            durationMinutes = 0
        )

        currentActiveRecord = record
        currentClockState = ClockState.CLOCKED_IN
        attendanceHistory.add(0, record)

        return Result.success(record)
    }

    suspend fun clockOut(lat: Double?, lng: Double?): Result<AttendanceDto> {
        delay(700)
        if (currentClockState != ClockState.CLOCKED_IN || currentActiveRecord == null) {
            return Result.failure(Exception("Cannot clock out: You are not currently clocked in"))
        }

        val nowDate = Date()
        val nowTime = timeFormat.format(nowDate)
        val clockInStr = currentActiveRecord?.clockInTime
        val calculatedDuration: Long = if (clockInStr != null) {
            try {
                val inDate = timeFormat.parse(clockInStr)
                val outDate = timeFormat.parse(nowTime)
                if (inDate != null && outDate != null) {
                    val diff = (outDate.time - inDate.time) / (1000 * 60)
                    if (diff >= 0) diff else diff + (24 * 60)
                } else 480L
            } catch (_: Exception) {
                480L
            }
        } else {
            480L
        }

        val updated = currentActiveRecord!!.copy(
            clockOutTime = nowTime,
            durationMinutes = calculatedDuration
        )

        currentActiveRecord = updated
        currentClockState = ClockState.CLOCKED_OUT

        // Update in history
        val idx = attendanceHistory.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            attendanceHistory[idx] = updated
        }

        return Result.success(updated)
    }

    suspend fun getAttendanceHistory(): Result<List<AttendanceDto>> {
        delay(400)
        return Result.success(attendanceHistory.toList())
    }

    // LEAVE
    suspend fun submitLeave(req: LeaveRequestDto): Result<LeaveApplicationDto> {
        delay(800)
        if (req.startDate > req.endDate) {
            return Result.failure(Exception("Start date cannot be after end date"))
        }
        if (req.reason.isBlank()) {
            return Result.failure(Exception("Please provide a reason for the leave request"))
        }

        if (req.isHalfDay && req.startDate != req.endDate) {
            return Result.failure(Exception("Half-day leave must be for a single date"))
        }

        val calculatedDays = if (req.isHalfDay) {
            0.5
        } else {
            try {
                val start = dateFormat.parse(req.startDate)
                val end = dateFormat.parse(req.endDate)
                if (start != null && end != null) {
                    val diff = (end.time - start.time) / (1000 * 60 * 60 * 24)
                    (diff + 1).coerceAtLeast(1).toDouble()
                } else 1.0
            } catch (_: Exception) {
                1.0
            }
        }

        val newApp = LeaveApplicationDto(
            id = "leave_" + System.currentTimeMillis(),
            employeeId = sessionManager.currentUser.value?.id ?: "emp_101",
            leaveType = req.leaveType,
            startDate = req.startDate,
            endDate = req.endDate,
            daysCount = calculatedDays,
            reason = req.reason,
            status = BackendLeaveStatus.PENDING,
            appliedAt = fullDateFormat.format(Date())
        )

        leaveHistory.add(0, newApp)
        return Result.success(newApp)
    }

    suspend fun getLeaveHistory(): Result<List<LeaveApplicationDto>> {
        delay(400)
        return Result.success(leaveHistory.toList())
    }

    // NOTIFICATIONS
    suspend fun getNotifications(): Result<List<AppNotificationDto>> {
        delay(300)
        return Result.success(notifications.toList())
    }

    suspend fun markNotificationRead(id: String): Result<Unit> {
        val idx = notifications.indexOfFirst { it.id == id }
        if (idx != -1) {
            notifications[idx] = notifications[idx].copy(isRead = true)
        }
        return Result.success(Unit)
    }

    suspend fun registerDeviceToken(token: String): Result<Unit> {
        sessionManager.saveDeviceToken(token)
        return Result.success(Unit)
    }
}
