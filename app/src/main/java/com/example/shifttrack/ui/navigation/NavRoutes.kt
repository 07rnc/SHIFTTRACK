package com.example.shifttrack.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object GpsAttendance : Screen("gps_attendance")
    object QrAttendance : Screen("qr_attendance")
    object AttendanceHistory : Screen("attendance_history")
    object LeaveRequest : Screen("leave_request")
    object LeaveHistory : Screen("leave_history")
    object NotificationHistory : Screen("notification_history")
    object Profile : Screen("profile")
}
